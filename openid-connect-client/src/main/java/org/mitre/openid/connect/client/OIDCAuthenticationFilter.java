/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.AuthRequestOptionsService;
import org.mitre.openid.connect.client.service.AuthRequestUrlBuilder;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.IssuerService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.StaticAuthRequestOptionsService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod.*;

/**
 * OpenID Connect Authentication Filter class
 * 
 * @author nemonik, jricher
 * 
 */
public class OIDCAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	protected final static String REDIRECT_URI_SESION_VARIABLE = "redirect_uri";
	protected final static String STATE_SESSION_VARIABLE = "state";
	protected final static String NONCE_SESSION_VARIABLE = "nonce";
	protected final static String ISSUER_SESSION_VARIABLE = "issuer";
	protected final static int HTTP_SOCKET_TIMEOUT = 30000;

	protected final static String FILTER_PROCESSES_URL = "/openid_connect_login";

	// Allow for time sync issues by having a window of X seconds.
	private int timeSkewAllowance = 300;

	@Autowired
	private JWKSetCacheService validationServices;

	// modular services to build out client filter
	private ServerConfigurationService servers;
	private ClientConfigurationService clients;
	private IssuerService issuerService;
	private AuthRequestOptionsService authOptions = new StaticAuthRequestOptionsService(); // initialize with an empty set of options
	private AuthRequestUrlBuilder authRequestBuilder;
	
	protected int httpSocketTimeout = HTTP_SOCKET_TIMEOUT;

	/**
	 * OpenIdConnectAuthenticationFilter constructor
	 */
	protected OIDCAuthenticationFilter() {
		super(FILTER_PROCESSES_URL);
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
	}

	/*
	 * This is the main entry point for the filter.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.web.authentication.
	 * AbstractAuthenticationProcessingFilter
	 * #attemptAuthentication(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

		if (!Strings.isNullOrEmpty(request.getParameter("error"))) {

			// there's an error coming back from the server, need to handle this
			handleError(request, response);
			return null; // no auth, response is sent to display page or something

		} else if (!Strings.isNullOrEmpty(request.getParameter("code"))) {

			// we got back the code, need to process this to get our tokens
			Authentication auth = handleAuthorizationCodeResponse(request, response);
			return auth;

		} else {

			// not an error, not a code, must be an initial login of some type
			handleAuthorizationRequest(request, response);

			return null; // no auth, response redirected to the server's Auth Endpoint (or possibly to the account chooser)
		}

	}

	/**
	 * Initiate an Authorization request
	 * 
	 * @param request
	 *            The request from which to extract parameters and perform the
	 *            authentication
	 * @param response
	 * @throws IOException
	 *             If an input or output exception occurs
	 */
	protected void handleAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

		HttpSession session = request.getSession();

		IssuerServiceResponse issResp = issuerService.getIssuer(request);

		if (issResp == null) {
			logger.error("Null issuer response returned from service.");
			throw new AuthenticationServiceException("No issuer found.");
		}

		if (issResp.shouldRedirect()) {
			response.sendRedirect(issResp.getRedirectUrl());
		} else {
			String issuer = issResp.getIssuer();

			if (Strings.isNullOrEmpty(issuer)) {
				logger.error("No issuer found: " + issuer);
				throw new AuthenticationServiceException("No issuer found: " + issuer);
			}

			session.setAttribute(ISSUER_SESSION_VARIABLE, issuer);

			ServerConfiguration serverConfig = servers.getServerConfiguration(issuer);
			if (serverConfig == null) {
				logger.error("No server configuration found for issuer: " + issuer);
				throw new AuthenticationServiceException("No server configuration found for issuer: " + issuer);
			}


			RegisteredClient clientConfig = clients.getClientConfiguration(serverConfig);
			if (clientConfig == null) {
				logger.error("No client configuration found for issuer: " + issuer);
				throw new AuthenticationServiceException("No client configuration found for issuer: " + issuer);
			}

			String redirectUri = null;
			if (clientConfig.getRegisteredRedirectUri() != null && clientConfig.getRegisteredRedirectUri().size() == 1) {
				// if there's a redirect uri configured (and only one), use that
				redirectUri = clientConfig.getRegisteredRedirectUri().toArray(new String[] {})[0];
			} else {
				// otherwise our redirect URI is this current URL, with no query parameters
				redirectUri = request.getRequestURL().toString();
			}
			session.setAttribute(REDIRECT_URI_SESION_VARIABLE, redirectUri);

			// this value comes back in the id token and is checked there
			String nonce = createNonce(session);

			// this value comes back in the auth code response
			String state = createState(session);

			Map<String, String> options = authOptions.getOptions(serverConfig, clientConfig, request);
			
			String authRequest = authRequestBuilder.buildAuthRequestUrl(serverConfig, clientConfig, redirectUri, nonce, state, options);

			logger.debug("Auth Request:  " + authRequest);

			response.sendRedirect(authRequest);
		}
	}

	/**
	 * @param request
	 *            The request from which to extract parameters and perform the
	 *            authentication
	 * @return The authenticated user token, or null if authentication is
	 *         incomplete.
	 */
	protected Authentication handleAuthorizationCodeResponse(HttpServletRequest request, HttpServletResponse response) {

		String authorizationCode = request.getParameter("code");

		HttpSession session = request.getSession();

		// check for state, if it doesn't match we bail early
		String storedState = getStoredState(session);
		if (!Strings.isNullOrEmpty(storedState)) {
			String state = request.getParameter("state");
			if (!storedState.equals(state)) {
				throw new AuthenticationServiceException("State parameter mismatch on return. Expected " + storedState + " got " + state);
			}
		}

		// look up the issuer that we set out to talk to
		String issuer = getStoredSessionString(session, ISSUER_SESSION_VARIABLE);

		// pull the configurations based on that issuer
		ServerConfiguration serverConfig = servers.getServerConfiguration(issuer);
		final RegisteredClient clientConfig = clients.getClientConfiguration(serverConfig);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("grant_type", "authorization_code");
		form.add("code", authorizationCode);

		String redirectUri = getStoredSessionString(session, REDIRECT_URI_SESION_VARIABLE);
		if (redirectUri != null) {
			form.add("redirect_uri", redirectUri);
		}

		// Handle Token Endpoint interaction
		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpClient.getParams().setParameter("http.socket.timeout", new Integer(httpSocketTimeout));

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		RestTemplate restTemplate;

		if (SECRET_BASIC.equals(clientConfig.getTokenEndpointAuthMethod())){
			// use BASIC auth if configured to do so
			restTemplate = new RestTemplate(factory) {

				@Override
				protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
					ClientHttpRequest httpRequest = super.createRequest(url, method);
					httpRequest.getHeaders().add("Authorization",
							String.format("Basic %s", Base64.encode(String.format("%s:%s", clientConfig.getClientId(), clientConfig.getClientSecret())) ));



					return httpRequest;
				}
			};
		} else {  //Alternatively use form based auth
			restTemplate = new RestTemplate(factory);

			form.add("client_id", clientConfig.getClientId());
			form.add("client_secret", clientConfig.getClientSecret());
		}

		logger.debug("tokenEndpointURI = " + serverConfig.getTokenEndpointUri());
		logger.debug("form = " + form);

		String jsonString = null;

		try {
			jsonString = restTemplate.postForObject(serverConfig.getTokenEndpointUri(), form, String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			// Handle error

			logger.error("Token Endpoint error response:  "
					+ httpClientErrorException.getStatusText() + " : "
					+ httpClientErrorException.getMessage());

			throw new AuthenticationServiceException("Unable to obtain Access Token: " + httpClientErrorException.getMessage());
		}

		logger.debug("from TokenEndpoint jsonString = " + jsonString);

		JsonElement jsonRoot = new JsonParser().parse(jsonString);
		if (!jsonRoot.isJsonObject()) {
			throw new AuthenticationServiceException("Token Endpoint did not return a JSON object: " + jsonRoot);
		}

		JsonObject tokenResponse = jsonRoot.getAsJsonObject();

		if (tokenResponse.get("error") != null) {

			// Handle error

			String error = tokenResponse.get("error").getAsString();

			logger.error("Token Endpoint returned: " + error);

			throw new AuthenticationServiceException("Unable to obtain Access Token.  Token Endpoint returned: " + error);

		} else {

			// Extract the id_token to insert into the
			// OIDCAuthenticationToken

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

			try {
				SignedJWT idToken = SignedJWT.parse(idTokenValue);

				// validate our ID Token over a number of tests
				ReadOnlyJWTClaimsSet idClaims = idToken.getJWTClaimsSet();

				// check the signature
				JwtSigningAndValidationService jwtValidator = validationServices.getValidator(serverConfig.getJwksUri());
				if (jwtValidator != null) {
					if(!jwtValidator.validateSignature(idToken)) {
						throw new AuthenticationServiceException("Signature validation failed");
					}
				} else {
					logger.info("No validation service found. Skipping signature validation");
				}

				// check the issuer
				if (idClaims.getIssuer() == null) {
					throw new AuthenticationServiceException("Id Token Issuer is null");
				} else if (!idClaims.getIssuer().equals(serverConfig.getIssuer())){
					throw new AuthenticationServiceException("Issuers do not match, expected " + serverConfig.getIssuer() + " got " + idClaims.getIssuer());
				}

				// check expiration
				if (idClaims.getExpirationTime() == null) {
					throw new AuthenticationServiceException("Id Token does not have required expiration claim");
				} else {
					// it's not null, see if it's expired
					Date now = new Date(System.currentTimeMillis() - (timeSkewAllowance * 1000));
					if (now.after(idClaims.getExpirationTime())) {
						throw new AuthenticationServiceException("Id Token is expired: " + idClaims.getExpirationTime());
					}
				}

				// check not before
				if (idClaims.getNotBeforeTime() != null) {
					Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
					if (now.before(idClaims.getNotBeforeTime())){
						throw new AuthenticationServiceException("Id Token not valid untill: " + idClaims.getNotBeforeTime());
					}
				}

				// check issued at
				if (idClaims.getIssueTime() == null) {
					throw new AuthenticationServiceException("Id Token does not have required issued-at claim");
				} else {
					// since it's not null, see if it was issued in the future
					Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
					if (now.before(idClaims.getIssueTime())) {
						throw new AuthenticationServiceException("Id Token was issued in the future: " + idClaims.getIssueTime());
					}
				}

				// check audience
				if (idClaims.getAudience() == null) {
					throw new AuthenticationServiceException("Id token audience is null");
				} else if (!idClaims.getAudience().contains(clientConfig.getClientId())) {
					throw new AuthenticationServiceException("Audience does not match, expected " + clientConfig.getClientId() + " got " + idClaims.getAudience());
				}

				// compare the nonce to our stored claim
				String nonce = idClaims.getStringClaim("nonce");
				if (Strings.isNullOrEmpty(nonce)) {

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

				// pull the subject (user id) out as a claim on the id_token

				String userId = idClaims.getSubject();

				// construct an OIDCAuthenticationToken and return a Authentication object w/the userId and the idToken

				OIDCAuthenticationToken token = new OIDCAuthenticationToken(userId, idClaims.getIssuer(), serverConfig, idTokenValue, accessTokenValue, refreshTokenValue);

				Authentication authentication = this.getAuthenticationManager().authenticate(token);

				return authentication;
			} catch (ParseException e) {
				throw new AuthenticationServiceException("Couldn't parse idToken: ", e);
			}



		}
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
	protected void handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String error = request.getParameter("error");
		String errorDescription = request.getParameter("error_description");
		String errorURI = request.getParameter("error_uri");

		throw new AuthenticationServiceException("Error from Authorization Endpoint: " + error + " " + errorDescription + " " + errorURI);
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



	//
	// Getters and setters for configuration variables
	//


	public int getTimeSkewAllowance() {
		return timeSkewAllowance;
	}

	public void setTimeSkewAllowance(int timeSkewAllowance) {
		this.timeSkewAllowance = timeSkewAllowance;
	}

	/**
	 * @return the validationServices
	 */
	public JWKSetCacheService getValidationServices() {
		return validationServices;
	}

	/**
	 * @param validationServices the validationServices to set
	 */
	public void setValidationServices(JWKSetCacheService validationServices) {
		this.validationServices = validationServices;
	}

	/**
	 * @return the servers
	 */
	public ServerConfigurationService getServerConfigurationService() {
		return servers;
	}

	/**
	 * @param servers the servers to set
	 */
	public void setServerConfigurationService(ServerConfigurationService servers) {
		this.servers = servers;
	}

	/**
	 * @return the clients
	 */
	public ClientConfigurationService getClientConfigurationService() {
		return clients;
	}

	/**
	 * @param clients the clients to set
	 */
	public void setClientConfigurationService(ClientConfigurationService clients) {
		this.clients = clients;
	}

	/**
	 * @return the issuerService
	 */
	public IssuerService getIssuerService() {
		return issuerService;
	}

	/**
	 * @param issuerService the issuerService to set
	 */
	public void setIssuerService(IssuerService issuerService) {
		this.issuerService = issuerService;
	}

	/**
	 * @return the authRequestBuilder
	 */
	public AuthRequestUrlBuilder getAuthRequestUrlBuilder() {
		return authRequestBuilder;
	}

	/**
	 * @param authRequestBuilder the authRequestBuilder to set
	 */
	public void setAuthRequestUrlBuilder(AuthRequestUrlBuilder authRequestBuilder) {
		this.authRequestBuilder = authRequestBuilder;
	}

	/**
	 * @return the authOptions
	 */
	public AuthRequestOptionsService getAuthRequestOptionsService() {
		return authOptions;
	}

	/**
	 * @param authOptions the authOptions to set
	 */
	public void setAuthRequestOptionsService(AuthRequestOptionsService authOptions) {
		this.authOptions = authOptions;
	}

}
