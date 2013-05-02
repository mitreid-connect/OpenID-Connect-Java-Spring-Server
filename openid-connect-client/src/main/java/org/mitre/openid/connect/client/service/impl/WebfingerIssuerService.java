/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.IssuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Use Webfinger to discover the appropriate issuer for a user-given input string.
 * @author jricher
 *
 */
public class WebfingerIssuerService implements IssuerService {

	private static Logger logger = LoggerFactory.getLogger(WebfingerIssuerService.class);

	// pattern used to parse user input; we can't use the built-in java URI parser
	private static final Pattern pattern = Pattern.compile("(https://|acct:|http://|mailto:)?(([^@]+)@)?([^\\?]+)(\\?([^#]+))?(#(.*))?");

	// map of user input -> issuer, loaded dynamically from webfinger discover
	private LoadingCache<NormalizedURI, String> issuers;

	/**
	 * Name of the incoming parameter to check for discovery purposes.
	 */
	private String parameterName = "identifier";

	/**
	 * URL of the page to forward to if no identifier is given.
	 */
	private String loginPageUrl;

	public WebfingerIssuerService() {
		issuers = CacheBuilder.newBuilder().build(new WebfingerIssuerFetcher());
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.IssuerService#getIssuer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public IssuerServiceResponse getIssuer(HttpServletRequest request) {

		String identifier = request.getParameter(parameterName);
		if (!Strings.isNullOrEmpty(identifier)) {
			try {
				String issuer = issuers.get(normalizeResource(identifier));
				return new IssuerServiceResponse(issuer, null, null);
			} catch (ExecutionException e) {
				logger.warn("Issue fetching issuer for user input: " + identifier, e);
				return null;
			}

		} else {
			logger.warn("No user input given, directing to login page: " + loginPageUrl);
			return new IssuerServiceResponse(loginPageUrl);
		}
	}

	/**
	 * Normalize the resource string as per OIDC Discovery.
	 * @param identifier
	 * @return the normalized string, or null if the string can't be normalized
	 */
	private NormalizedURI normalizeResource(String identifier) {
		// try to parse the URI
		// NOTE: we can't use the Java built-in URI class because it doesn't split the parts appropriately

		if (Strings.isNullOrEmpty(identifier)) {
			logger.warn("Can't normalize null or empty URI: " + identifier);
			return null; // nothing we can do
		} else {

			NormalizedURI n = new NormalizedURI();
			Matcher m = pattern.matcher(identifier);

			if (m.matches()) {
				n.scheme = m.group(1); // includes colon and maybe initial slashes
				n.user = m.group(2); // includes at sign
				n.hostportpath = m.group(4);
				n.query = m.group(5); // includes question mark
				n.hash = m.group(7); // includes hash mark

				// normalize scheme portion
				if (Strings.isNullOrEmpty(n.scheme)) {
					if (!Strings.isNullOrEmpty(n.user)) {
						// no scheme, but have a user, assume acct:
						n.scheme = "acct:";
					} else {
						// no scheme, no user, assume https://
						n.scheme = "https://";
					}
				}

				n.source = Strings.nullToEmpty(n.scheme) +
						Strings.nullToEmpty(n.user) +
						Strings.nullToEmpty(n.hostportpath) +
						Strings.nullToEmpty(n.query); // note: leave fragment off

				return n;
			} else {
				logger.warn("Parser couldn't match input: " + identifier);
				return null;
			}

		}


	}


	/**
	 * @return the parameterName
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * @param parameterName the parameterName to set
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}


	/**
	 * @return the loginPageUrl
	 */
	public String getLoginPageUrl() {
		return loginPageUrl;
	}

	/**
	 * @param loginPageUrl the loginPageUrl to set
	 */
	public void setLoginPageUrl(String loginPageUrl) {
		this.loginPageUrl = loginPageUrl;
	}


	/**
	 * @author jricher
	 *
	 */
	private class WebfingerIssuerFetcher extends CacheLoader<NormalizedURI, String> {
		private HttpClient httpClient = new DefaultHttpClient();
		private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		private JsonParser parser = new JsonParser();

		@Override
		public String load(NormalizedURI key) throws Exception {

			RestTemplate restTemplate = new RestTemplate(httpFactory);
			// construct the URL to go to

			//String url = "https://" + key.hostportpath + "/.well-known/webfinger?resource="
			String scheme = key.scheme;
			if (!Strings.isNullOrEmpty(scheme) && !scheme.startsWith("http")) {
				// do discovery on http or https URLs
				scheme = "https://";
			}
			URIBuilder builder = new URIBuilder(scheme + key.hostportpath + "/.well-known/webfinger" + Strings.nullToEmpty(key.query));
			builder.addParameter("resource", key.source);
			builder.addParameter("rel", "http://openid.net/specs/connect/1.0/issuer");

			// do the fetch
			logger.info("Loading: " + builder.toString());
			String webfingerResponse = restTemplate.getForObject(builder.build(), String.class);

			// TODO: catch and handle HTTP errors

			JsonElement json = parser.parse(webfingerResponse);

			// TODO: catch and handle JSON errors

			if (json != null && json.isJsonObject()) {
				// find the issuer
				JsonArray links = json.getAsJsonObject().get("links").getAsJsonArray();
				for (JsonElement link : links) {
					if (link.isJsonObject()) {
						JsonObject linkObj = link.getAsJsonObject();
						if (linkObj.has("href")
								&& linkObj.has("rel")
								&& linkObj.get("rel").getAsString().equals("http://openid.net/specs/connect/1.0/issuer")) {

							// we found the issuer, return it
							return linkObj.get("href").getAsString();
						}
					}
				}
			}

			// we couldn't find it
			logger.warn("Couldn't find issuer");
			return null;
		}

	}


	/**
	 * Simple data shuttle class to represent the parsed components of a URI.
	 * 
	 * @author jricher
	 *
	 */
	private class NormalizedURI {

		public String scheme;
		public String user;
		public String hostportpath;
		public String query;
		public String hash;
		public String source;


	}


}
