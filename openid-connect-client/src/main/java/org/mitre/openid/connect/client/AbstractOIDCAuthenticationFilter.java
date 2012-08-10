/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.openid.connect.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JwtSigningAndValidationServiceDefault;
import org.mitre.key.fetch.KeyFetcher;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.mitre.openid.connect.model.IdToken;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Abstract OpenID Connect Authentication Filter class
 * 
 * @author nemonik
 * 
 */
public class AbstractOIDCAuthenticationFilter extends
		AbstractAuthenticationProcessingFilter {

	protected static final String REDIRECT_URI_SESION_VARIABLE = "redirect_uri";
	protected static final String STATE_SESSION_VARIABLE = "state";
	protected final static String NONCE_SESSION_VARIABLE = "nonce";
	protected final static int HTTP_SOCKET_TIMEOUT = 30000;
	protected final static String DEFAULT_SCOPE = "openid";

	protected final static String FILTER_PROCESSES_URL = "/openid_connect_login";

	
	private Map<OIDCServerConfiguration, JwtSigningAndValidationService> validationServices = new HashMap<OIDCServerConfiguration, JwtSigningAndValidationService>();
	
	/**
	 * Builds the redirect_uri that will be sent to the Authorization Endpoint.
	 * By default returns the URL of the current request minus zero or more
	 * fields of the URL's query string.
	 * 
	 * @param request
	 *            the current request which is being processed by this filter
	 * @param ignoreFields
	 *            an array of field names to ignore.
	 * @return a URL built from the messaged parameters.
	 */
	public static String buildRedirectURI(HttpServletRequest request, String[] ignoreFields) {

		List<String> ignore = (ignoreFields != null) ? Arrays.asList(ignoreFields) : null;
 
		boolean isFirst = true;

		StringBuffer sb = request.getRequestURL();

		for (Enumeration<?> e = request.getParameterNames(); e.hasMoreElements();) {

			String name = (String) e.nextElement();

			if ((ignore == null) || (!ignore.contains(name))) {

				// Assume for simplicity that there is only one value

				String value = request.getParameter(name);

				if (value == null) {
					continue;
				}

				if (isFirst) {
					sb.append("?");
					isFirst = false;
				}

				sb.append(name).append("=").append(value);

				if (e.hasMoreElements()) {
					sb.append("&");
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Return the URL w/ GET parameters
	 * 
	 * @param baseURI
	 *            A String containing the protocol, server address, path, and
	 *            program as per "http://server/path/program"
	 * @param queryStringFields
	 *            A map where each key is the field name and the associated
	 *            key's value is the field value used to populate the URL's
	 *            query string
	 * @return A String representing the URL in form of
	 *         http://server/path/program?query_string from the messaged
	 *         parameters.
	 */
	public static String buildURL(String baseURI, Map<String, String> queryStringFields) {
// TODO: replace this with URIUtils call
		StringBuilder URLBuilder = new StringBuilder(baseURI);

		char appendChar = '?';

		for (Map.Entry<String, String> param : queryStringFields.entrySet()) {

			try {

				URLBuilder.append(appendChar)
					.append(param.getKey())
					.append('=')
					.append(URLEncoder.encode(param.getValue(), "UTF-8"));

			} catch (UnsupportedEncodingException uee) {

				throw new IllegalStateException(uee);

			}

			appendChar = '&';
		}

		return URLBuilder.toString();
	}

	protected String errorRedirectURI;

	protected String scope;

	protected int httpSocketTimeout = HTTP_SOCKET_TIMEOUT;

	/**
	 * OpenIdConnectAuthenticationFilter constructor
	 */
	protected AbstractOIDCAuthenticationFilter() {
		super(FILTER_PROCESSES_URL);
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		Assert.notNull(errorRedirectURI, "An Error Redirect URI must be supplied");

		// prepend the spec necessary DEFAULT_SCOPE
		setScope((scope != null && !scope.isEmpty()) ? DEFAULT_SCOPE + " " + scope : DEFAULT_SCOPE);
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

		logger.debug("Request: "
				+ request.getRequestURI()
				+ (StringUtils.isNotBlank(request.getQueryString()) ? "?"
						+ request.getQueryString() : ""));

		return null;
	}

	/**
	 * Handles the authorization grant response
	 * 
	 * @param authorizationCode
	 *            The Authorization grant code
	 * @param request
	 *            The request from which to extract parameters and perform the
	 *            authentication
	 * @return The authenticated user token, or null if authentication is
	 *         incomplete.
	 * @throws Exception 
	 * @throws UnsupportedEncodingException
	 */
	protected Authentication handleAuthorizationGrantResponse(
			String authorizationCode, HttpServletRequest request,
			OIDCServerConfiguration serverConfig) {

		final boolean debug = logger.isDebugEnabled();

		HttpSession session = request.getSession();
		
		// check for state
		String storedState = getStoredState(session);
		if (!StringUtils.isBlank(storedState)) {
			String state = request.getParameter("state");
			if (!storedState.equals(state)) {
				throw new AuthenticationServiceException("State parameter mismatch on return. Expected " + storedState + " got " + state);
			}
		}
		
		// Handle Token Endpoint interaction
		HttpClient httpClient = new DefaultHttpClient();

		httpClient.getParams().setParameter("http.socket.timeout", new Integer(httpSocketTimeout));

		//
		// TODO: basic auth is untested (it wasn't working last I
		// tested)
		// UsernamePasswordCredentials credentials = new
		// UsernamePasswordCredentials(serverConfig.getClientId(),
		// serverConfig.getClientSecret());
		// ((DefaultHttpClient)
		// httpClient).getCredentialsProvider().setCredentials(AuthScope.ANY,
		// credentials);
		//

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		RestTemplate restTemplate = new RestTemplate(factory);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("grant_type", "authorization_code");
		form.add("code", authorizationCode);
		
		String redirectUri = getStoredRedirectUri(session);
		if (redirectUri != null) {
			form.add("redirect_uri", redirectUri);
		}

		// pass clientId and clientSecret in post of request
		form.add("client_id", serverConfig.getClientId());
		form.add("client_secret", serverConfig.getClientSecret());

		if (debug) {
			logger.debug("tokenEndpointURI = " + serverConfig.getTokenEndpointURI());
			logger.debug("form = " + form);
		}

		String jsonString = null;

		try {
			jsonString = restTemplate.postForObject(
					serverConfig.getTokenEndpointURI(), form, String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			// Handle error

			logger.error("Token Endpoint error response:  "
					+ httpClientErrorException.getStatusText() + " : "
					+ httpClientErrorException.getMessage());

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.");
		}

		logger.debug("from TokenEndpoint jsonString = " + jsonString);
		
		JsonElement jsonRoot = new JsonParser().parse(jsonString);
		if (!jsonRoot.isJsonObject()) {
			throw new AuthenticationServiceException("Token Endpoint did not return a JSON object: " + jsonRoot);
		}

		JsonObject tokenResponse = jsonRoot.getAsJsonObject();
		
		if (tokenResponse.get("error") != null) {

			// Handle error

			String error = tokenResponse.get("error")
					.getAsString();

			logger.error("Token Endpoint returned: " + error);

			throw new AuthenticationServiceException("Unable to obtain Access Token.  Token Endpoint returned: " + error);

		} else {

			// Extract the id_token to insert into the
			// OpenIdConnectAuthenticationToken
			
			// get out all the token strings
			String accessTokenValue = null;
			String idTokenValue = null;
			String refreshTokenValue = null;
			
			if (tokenResponse.has("access_token")) {
				accessTokenValue = tokenResponse.get("access_token").getAsString();
			} else {
				throw new AuthenticationServiceException("Token Endpoint did not return an access_token: " + jsonString);
			}
			
			if (tokenResponse.has("id_token")) {
				idTokenValue = tokenResponse.get("id_token").getAsString();
			} else {
				logger.error("Token Endpoint did not return an id_token");
				throw new AuthenticationServiceException("Token Endpoint did not return an id_token");
			}
			
			if (tokenResponse.has("refresh_token")) {
				refreshTokenValue = tokenResponse.get("refresh_token").getAsString();
			}
			
			JwtSigningAndValidationService jwtValidator = getValidatorForServer(serverConfig); 
			IdToken idToken = null;
			
			try {
				idToken = IdToken.parse(idTokenValue);
			
			} catch (AuthenticationServiceException e) {

				// I suspect this could happen

				logger.error("Problem parsing id_token:  " + e);
				// e.printStackTrace();

				throw new AuthenticationServiceException("Problem parsing id_token return from Token endpoint: " + e);
			}

			// validate our ID Token over a number of tests
			
			if(!jwtValidator.validateSignature(idToken.toString())) {
				throw new AuthenticationServiceException("Signature not validated");
			}
			
			if(idToken.getClaims().getIssuer() == null) {
				throw new AuthenticationServiceException("Issuer is null");
			}
			
			if(!idToken.getClaims().getIssuer().equals(serverConfig.getIssuer())){
				throw new AuthenticationServiceException("Issuers do not match");
			}
			
			if(jwtValidator.isJwtExpired(idToken)) {
				throw new AuthenticationServiceException("Id Token is expired");
			}
			
			if(!jwtValidator.validateIssuedAt(idToken)) {
				throw new AuthenticationServiceException("Id Token issuedAt failed");
			}

			// Clients are required to compare nonce claim in ID token to 
			// the nonce sent in the Authorization request.  The client 
			// stores this value as a signed session cookie to detect a 
			// replay by third parties.
			//
			// See: OpenID Connect Messages Section 2.1.1 entitled "ID Token"
			//
			// http://openid.net/specs/openid-connect-messages-1_0.html#id_token
			//
			
			//String nonce = idToken.getClaims().getClaimAsString("nonce");

			String nonce = idToken.getClaims().getNonce();
			
			if (StringUtils.isBlank(nonce)) {
				
				logger.error("ID token did not contain a nonce claim.");

				throw new AuthenticationServiceException("ID token did not contain a nonce claim.");
			}

			String storedNonce = getStoredNonce(session);
			if (!nonce.equals(storedNonce)) {
				logger.error("Possible replay attack detected! The comparison of the nonce in the returned "
						+ "ID Token to the session " + NONCE_SESSION_VARIABLE + " failed. Expected " + storedNonce + " got " + nonce + ".");

				throw new AuthenticationServiceException(
						"Possible replay attack detected! The comparison of the nonce in the returned "
								+ "ID Token to the session " + NONCE_SESSION_VARIABLE + " failed. Expected " + storedNonce + " got " + nonce + ".");
			}

			// pull the user_id out as a claim on the id_token
			
			String userId = idToken.getTokenClaims().getUserId();
			
			// construct an OpenIdConnectAuthenticationToken and return 
			// a Authentication object w/the userId and the idToken
			
			OpenIdConnectAuthenticationToken token = new OpenIdConnectAuthenticationToken(userId, idTokenValue, accessTokenValue, refreshTokenValue);

			Authentication authentication = this.getAuthenticationManager().authenticate(token);

			return authentication;

		

		}
	}

	/**
	 * Initiate an Authorization request
	 * 
	 * @param request
	 *            The request from which to extract parameters and perform the
	 *            authentication
	 * @param response
	 *            The response, needed to set a cookie and do a redirect as part
	 *            of a multi-stage authentication process
	 * @param serverConfiguration
	 * @throws IOException
	 *             If an input or output exception occurs
	 */
	protected void handleAuthorizationRequest(HttpServletRequest request,
			HttpServletResponse response, OIDCServerConfiguration serverConfiguration) throws IOException {

		HttpSession session = request.getSession();
		
		Map<String, String> urlVariables = new HashMap<String, String>();

		// Required parameters:

		urlVariables.put("response_type", "code");
		urlVariables.put("client_id", serverConfiguration.getClientId());
		urlVariables.put("scope", scope);
		
		String redirectURI = buildRedirectURI(request, null);
		urlVariables.put("redirect_uri", redirectURI);
		session.setAttribute(REDIRECT_URI_SESION_VARIABLE, redirectURI);

		// Create a string value used to associate a user agent session
		// with an ID Token to mitigate replay attacks. The value is
		// passed through unmodified to the ID Token. One method is to
		// store a random value as a signed session cookie, and pass the
		// value in the nonce parameter.

		String nonce = createNonce(session);
		urlVariables.put("nonce", nonce);

		String state = createState(session);
		urlVariables.put("state", state);
		
		// Optional parameters:

		// TODO: display, prompt, request, request_uri

		String authRequest = AbstractOIDCAuthenticationFilter.buildURL(serverConfiguration.getAuthorizationEndpointURI(), urlVariables);

		logger.debug("Auth Request:  " + authRequest);

		response.sendRedirect(authRequest);
	}

	/**
	 * Handle Authorization Endpoint error
	 * 
	 * @param request
	 *            The request from which to extract parameters and handle the
	 *            error
	 * @param response
	 *            The response, needed to do a redirect to display the error
	 * @throws IOException
	 *             If an input or output exception occurs
	 */
	protected void handleError(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String error = request.getParameter("error");
		String errorDescription = request.getParameter("error_description");
		String errorURI = request.getParameter("error_uri");

		Map<String, String> requestParams = new HashMap<String, String>();

		requestParams.put("error", error);

		if (errorDescription != null) {
			requestParams.put("error_description", errorDescription);
		}

		if (errorURI != null) {
			requestParams.put("error_uri", errorURI);
		}

		response.sendRedirect(AbstractOIDCAuthenticationFilter.buildURL(
				errorRedirectURI, requestParams));
	}

	public void setErrorRedirectURI(String errorRedirectURI) {
		this.errorRedirectURI = errorRedirectURI;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	

	/**
	 * Get the named stored session variable as a string. Return null if not found or not a string.
	 * @param session
	 * @param key
	 * @return
	 */
	private static String getStoredSessionString(HttpSession session, String key) {
		Object o = session.getAttribute(key);
		if (o != null && o instanceof String) {
			return o.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Create a cryptographically random nonce and store it in the session
	 * @param session
	 * @return
	 */
	protected static String createNonce(HttpSession session) {
		String nonce = new BigInteger(50, new SecureRandom()).toString(16);
		session.setAttribute(NONCE_SESSION_VARIABLE, nonce);

		return nonce;
	}

	/**
	 * Get the nonce we stored in the session
	 * @param session
	 * @return
	 */
	protected static String getStoredNonce(HttpSession session) {
		return getStoredSessionString(session, NONCE_SESSION_VARIABLE);
	}
	
	/**
	 * Create a cryptographically random state and store it in the session
	 * @param session
	 * @return
	 */
	protected static String createState(HttpSession session) {
		String state = new BigInteger(50, new SecureRandom()).toString(16);
		session.setAttribute(STATE_SESSION_VARIABLE, state);
		
		return state;
	}
	
	/**
	 * Get the state we stored in the session
	 * @param session
	 * @return
	 */
	protected static String getStoredState(HttpSession session) {
		return getStoredSessionString(session, STATE_SESSION_VARIABLE);
	}

	/**
	 * Get the stored redirect URI that we used on the way out
	 * @param serverConfig
	 * @return
	 */
	protected static String getStoredRedirectUri(HttpSession session) {
		return getStoredSessionString(session, REDIRECT_URI_SESION_VARIABLE);
	}
	
	
	protected JwtSigningAndValidationService getValidatorForServer(OIDCServerConfiguration serverConfig) {

		if(getValidationServices().containsKey(serverConfig)){
			return validationServices.get(serverConfig);
		} else {
						
			KeyFetcher keyFetch = new KeyFetcher();
			PublicKey signingKey = null;
			
			// If the config has the X509 url, prefer that to the JWK
			if(serverConfig.getX509SigningUrl() != null) {
				signingKey = keyFetch.retrieveX509Key(serverConfig);
				
			} else {
				// otherwise prefer the JWK
				signingKey = keyFetch.retrieveJwkKey(serverConfig);
			}
			
			if (signingKey != null) {
				Map<String, JwtSigner> signers = new HashMap<String, JwtSigner>();
				
				if (signingKey instanceof RSAPublicKey) {
					
					RSAPublicKey rsaKey = (RSAPublicKey)signingKey;
					
					// build an RSA signer
					RsaSigner signer256 = new RsaSigner(JwsAlgorithm.RS256.getJwaName(), rsaKey, null);
					RsaSigner signer384 = new RsaSigner(JwsAlgorithm.RS384.getJwaName(), rsaKey, null);
					RsaSigner signer512 = new RsaSigner(JwsAlgorithm.RS512.getJwaName(), rsaKey, null);

					signers.put(serverConfig.getIssuer() + JwsAlgorithm.RS256.getJwaName(), signer256);
					signers.put(serverConfig.getIssuer() + JwsAlgorithm.RS384.getJwaName(), signer384);
					signers.put(serverConfig.getIssuer() + JwsAlgorithm.RS512.getJwaName(), signer512);
				}

                JwtSigningAndValidationService signingAndValidationService = new JwtSigningAndValidationServiceDefault(signers);
				
				validationServices.put(serverConfig, signingAndValidationService);
				
				return signingAndValidationService;
				
			} else {
				return null;
			}
		}

	}

	public Map<OIDCServerConfiguration, JwtSigningAndValidationService> getValidationServices() {
		return validationServices;
	}

	public void setValidationServices(
			Map<OIDCServerConfiguration, JwtSigningAndValidationService> validationServices) {
		this.validationServices = validationServices;
	}
}
