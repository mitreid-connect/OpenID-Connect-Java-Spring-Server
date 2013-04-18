/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.IssuerService;
import org.mitre.openid.connect.config.ServerConfiguration;
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

	
	private String parameterName = "identifier";

	public WebfingerIssuerService() {
		issuers = CacheBuilder.newBuilder().build(new WebfingerIssuerFetcher());
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.IssuerService#getIssuer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public IssuerServiceResponse getIssuer(HttpServletRequest request) {
		
		String login = request.getParameter(parameterName);
		if (!Strings.isNullOrEmpty(login)) {
            try {
            	String issuer = issuers.get(normalizeResource(login));
            	return new IssuerServiceResponse(issuer, null, null);
            } catch (ExecutionException e) {
            	logger.warn("Issue fetching issuer for user input: " + login, e);
	            return null;
            }
            
		} else {
			logger.warn("No user input given.");
			return null;
		}
	}
	
	/**
	 * Normalize the resource string as per OIDC Discovery.
	 * @param resource
	 * @return the normalized string, or null if the string can't be normalized
	 */
	private NormalizedURI normalizeResource(String resource) {
		// try to parse the URI		
		// NOTE: we can't use the Java built-in URI class because it doesn't split the parts appropriately
		
		if (Strings.isNullOrEmpty(resource)) {
			logger.warn("Can't normalize null or empty URI: " + resource);
			return null; // nothing we can do
		} else {
				
    		NormalizedURI n = new NormalizedURI();    		
    		Matcher m = pattern.matcher(resource);
		
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
				logger.warn("Parser couldn't match input: " + resource);
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
     * @author jricher
     *
     */
    private class WebfingerIssuerFetcher extends CacheLoader<NormalizedURI, String> {
    	private HttpClient httpClient = new DefaultHttpClient();
    	private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		/* (non-Javadoc)
		 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
		 */
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
        	logger.info("Loading: " + builder.toString());
        	String webfingerResponse = restTemplate.getForObject(builder.build(), String.class);
        	
        	JsonElement json = new JsonParser().parse(webfingerResponse);
        	if (json != null && json.isJsonObject()) {
        		// find the issuer
        		JsonArray links = json.getAsJsonObject().get("links").getAsJsonArray();
        		for (JsonElement link : links) {
	                if (link.isJsonObject()) {
	                	JsonObject linkObj = link.getAsJsonObject();
	                	if (linkObj.has("href") 
	                			&& linkObj.has("rel") 
	                			&& linkObj.get("rel").getAsString().equals("http://openid.net/specs/connect/1.0/issuer")) {
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
