package org.mitre.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.util.Utility;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * The OpenID Connect Authentication Filter
 * 
 * @author nemonik
 * 
 */
public class OpenIDConnectAuthenticationFilter extends
		AbstractAuthenticationProcessingFilter {

	private static Log logger = LogFactory
			.getLog(OpenIDConnectAuthenticationFilter.class);

	private final static String SCOPE = "openid";
	private final static int KEY_SIZE = 1024;
	private final static String SIGNING_ALGORITHM = "SHA256withRSA";
	private final static String NONCE_SIGNATURE_COOKIE_NAME = "nonce";

	/**
	 * Return the URL w/ GET parameters
	 * 
	 * @param baseURI
	 * @param params
	 * @return
	 */
	public static String buildURL(String baseURI,
			Map<String, String> urlVariables) {

		StringBuilder URLBuilder = new StringBuilder(baseURI);

		char appendChar = '?';

		for (Map.Entry<String, String> param : urlVariables.entrySet()) {
			try {
				URLBuilder.append(appendChar).append(param.getKey())
						.append('=')
						.append(URLEncoder.encode(param.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException uee) {
				throw new IllegalStateException(uee);
			}
			appendChar = '&';
		}

		return URLBuilder.toString();
	}

	/**
	 * Returns the signature text for the byte array of data
	 * 
	 * @return
	 */
	public static String sign(Signature signer, PrivateKey privateKey,
			byte[] data) {
		String signature;

		try {
			signer.initSign(privateKey);
			signer.update(data);

			byte[] sigBytes = signer.sign();

			signature = (new String(Base64.encodeBase64URLSafe(sigBytes)))
					.replace("=", "");

		} catch (GeneralSecurityException generalSecurityException) {
			// generalSecurityException.printStackTrace();
			throw new IllegalStateException(generalSecurityException);
		}

		return signature;
	}

	/**
	 * Verifies the signature text against the data
	 * 
	 * @param data
	 * @param sigText
	 * @return
	 */
	public static boolean verify(Signature signer, PublicKey publicKey,
			String data, String sigText) {

		try {
			signer.initVerify(publicKey);
			signer.update(data.getBytes("UTF-8"));

			byte[] sigBytes = Base64.decodeBase64(sigText);

			return signer.verify(sigBytes);

		} catch (GeneralSecurityException generalSecurityException) {
			// generalSecurityException.printStackTrace();
			throw new IllegalStateException(generalSecurityException);
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			// unsupportedEncodingException.printStackTrace();
			throw new IllegalStateException(unsupportedEncodingException);
		}
	}

	private final String errorRedirectURI;
	private final String authorizationEndpointURI;
	private final String tokenEndpointURI;
	private final String checkIDEndpointURI;
	private final String clientSecret;
	private final String clientId;
	private String scope;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private Signature signer;

	/**
	 * @param defaultFilterProcessesUrl
	 * @param authorizationEndpointURI
	 * @param tokenEndpointURI
	 * @param checkIDEndpointURI
	 * @param clientId
	 * @param scope
	 */
	protected OpenIDConnectAuthenticationFilter(String errorRedirectURI,
			String clientSecret, String defaultFilterProcessesUrl,
			String authorizationEndpointURI, String tokenEndpointURI,
			String checkIDEndpointURI, String clientId, String scope,
			String privateModulus, String privateExponent,
			String publicModulus, String publicExponent) {
		super(defaultFilterProcessesUrl);

		this.clientSecret = clientSecret;
		this.errorRedirectURI = errorRedirectURI;
		this.authorizationEndpointURI = authorizationEndpointURI;
		this.tokenEndpointURI = tokenEndpointURI;
		this.checkIDEndpointURI = checkIDEndpointURI;
		this.clientId = clientId;
		this.scope = SCOPE + " " + scope;

		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(KEY_SIZE);
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();

			signer = Signature.getInstance(SIGNING_ALGORITHM);
		} catch (GeneralSecurityException generalSecurityException) {
			// generalSecurityException.printStackTrace();
			throw new IllegalStateException(generalSecurityException);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.web.authentication.
	 * AbstractAuthenticationProcessingFilter#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (errorRedirectURI == null) {
			throw new IllegalArgumentException(
					"An Error Redirect URI must be supplied");
		}

		if (authorizationEndpointURI == null) {
			throw new IllegalArgumentException(
					"An Authorization Endpoint URI must be supplied");
		}

		if (tokenEndpointURI == null) {
			throw new IllegalArgumentException(
					"A Token ID Endpoint URI must be supplied");
		}

		if (checkIDEndpointURI == null) {
			throw new IllegalArgumentException(
					"A Check ID Endpoint URI must be supplied");
		}

		if (clientId == null) {
			throw new IllegalArgumentException("A Client ID must be supplied");
		}

		if (clientSecret == null) {
			throw new IllegalArgumentException(
					"A Client Secret must be supplied");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.web.authentication.
	 * AbstractAuthenticationProcessingFilter
	 * #attemptAuthentication(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {

		if (request.getParameter("error") != null) {

			// Handle Authorization Endpoint error

			String error = request.getParameter("error");
			String errorDescription = request.getParameter("error_description");
			String errorURI = request.getParameter("error_uri");

			@SuppressWarnings("unused")
			String state = request.getParameter("state"); // required by
															// specification.
															// doesn't say what
															// to do w/

			Map<String, String> requestParams = new HashMap<String, String>();

			requestParams.put("error", error);

			if (errorDescription != null) {
				requestParams.put("error_description", errorDescription);
			}

			if (errorURI != null) {
				requestParams.put("error_uri", errorURI);
			}

			response.sendRedirect(buildURL(errorRedirectURI, requestParams));

		} else {

			// Handle Authorization Endpoint redirect response

			String code = request.getParameter("code");

			if (code != null) {

				// Handle Token Endpoint interaction

				HttpClient httpClient = new DefaultHttpClient();
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
						clientId, clientSecret);
				((DefaultHttpClient) httpClient).getCredentialsProvider()
						.setCredentials(AuthScope.ANY, credentials);

				HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
						httpClient);

				RestTemplate restTemplate = new RestTemplate(factory);

				MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
				form.add("grant_type", "authorization_code");
				form.add("code", code);
				form.add("redirect_uri", Utility.findBaseUrl(request));

				String jsonString = null;

				try {
					jsonString = restTemplate.postForObject(tokenEndpointURI,
							form, String.class);
				} catch (HttpClientErrorException httpClientErrorException) {

					logger.error("Token Endpoint error response:  "
							+ httpClientErrorException.getStatusText() + " : "
							+ httpClientErrorException.getMessage());

					return null;
				}

				JsonElement jsonRoot = new JsonParser().parse(jsonString);

				if (jsonRoot.getAsJsonObject().get("error") != null) {
					// Handle error

					String error = jsonRoot.getAsJsonObject().get("error")
							.getAsString();

					logger.error("Token Endpoint returned: " + error);

					return null;

				} else {

					// Handle Check ID Endpoint interaction

					httpClient = new DefaultHttpClient();
					factory = new HttpComponentsClientHttpRequestFactory(
							httpClient);
					restTemplate = new RestTemplate(factory);

					form = new LinkedMultiValueMap<String, String>();

					form.add("access_token",
							jsonRoot.getAsJsonObject().get("id_token")
									.getAsString());

					jsonString = null;

					try {
						jsonString = restTemplate.postForObject(
								checkIDEndpointURI, form, String.class);
					} catch (HttpClientErrorException httpClientErrorException) {

						logger.error("Check ID Endpoint error response:  "
								+ httpClientErrorException.getStatusText()
								+ " : " + httpClientErrorException.getMessage());

						return null;
					}

					jsonRoot = new JsonParser().parse(jsonString);

					String user_id = jsonRoot.getAsJsonObject().get("user_id")
							.getAsString();
					String nonce = jsonRoot.getAsJsonObject().get("nonce")
							.getAsString();

					// The nonce in the returned ID Token is compared to the
					// signed session cookie to detect ID Token replay by third
					// parties.

					Cookie nonceSignatureCookie = WebUtils.getCookie(request,
							NONCE_SIGNATURE_COOKIE_NAME);

					if (nonceSignatureCookie != null) {

						String sigText = nonceSignatureCookie.getValue();

						if (sigText != null && !sigText.isEmpty()) {

							if (!verify(signer, publicKey, nonce, sigText)) {
								logger.error("Possible replay attack detected! "
										+ "The comparison of the nonce in the returned "
										+ "ID Token to the signed session "
										+ NONCE_SIGNATURE_COOKIE_NAME
										+ " failed.");

								return null;
							}

						} else {
							logger.error(NONCE_SIGNATURE_COOKIE_NAME
									+ " was found, but was null or empty.");

							return null;
						}

					} else {

						logger.error(NONCE_SIGNATURE_COOKIE_NAME
								+ " cookie was not found.");

						return null;
					}

					// Create an Authentication object for the token, and
					// return.

					OpenIDConnectAuthenticationToken token = new OpenIDConnectAuthenticationToken(
							user_id);

					Authentication authentication = this
							.getAuthenticationManager().authenticate(token);

					return authentication;

				}

			} else {

				// Handle an Authorization request

				Map<String, String> urlVariables = new HashMap<String, String>();

				// Required parameters:

				urlVariables.put("response_type", "code");
				urlVariables.put("client_id", clientId);
				urlVariables.put("scope", scope);
				urlVariables.put("redirect_uri", Utility.findBaseUrl(request));

				// Create a string value used to associate a user agent session
				// with an ID Token to mitigate replay attacks. The value is
				// passed through unmodified to the ID Token. One method is to
				// store a random value as a signed session cookie, and pass the
				// value in the nonce parameter.

				String nonce = new BigInteger(50, new Random()).toString(16);

				Cookie nonceCookie = new Cookie(NONCE_SIGNATURE_COOKIE_NAME,
						sign(signer, privateKey, nonce.getBytes()));

				response.addCookie(nonceCookie);

				urlVariables.put("nonce", nonce);

				// Optional parameters:

				// TODO: display, prompt, request, request_uri

				response.sendRedirect(buildURL(authorizationEndpointURI,
						urlVariables));
			}
		}
		return null;
	}
}
