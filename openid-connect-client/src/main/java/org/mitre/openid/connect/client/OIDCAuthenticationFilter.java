/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
 *******************************************************************************/
package org.mitre.openid.connect.client;

import static org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod.PRIVATE_KEY;
import static org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod.SECRET_BASIC;
import static org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod.SECRET_JWT;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.jwt.signer.service.impl.SymmetricKeyJWTValidatorCacheService;
import org.mitre.oauth2.model.PKCEAlgorithm;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.AuthRequestOptionsService;
import org.mitre.openid.connect.client.service.AuthRequestUrlBuilder;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.IssuerService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.StaticAuthRequestOptionsService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mitre.openid.connect.model.PendingOIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

/**
 * OpenID Connect Authentication Filter class
 * 
 * @author nemonik, jricher
 * 
 */
public class OIDCAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	protected final static String REDIRECT_URI_SESION_VARIABLE = "redirect_uri";
	protected final static String CODE_VERIFIER_SESSION_VARIABLE = "code_verifier";
	protected final static String STATE_SESSION_VARIABLE = "state";
	protected final static String NONCE_SESSION_VARIABLE = "nonce";
	protected final static String ISSUER_SESSION_VARIABLE = "issuer";
	protected final static String TARGET_SESSION_VARIABLE = "target";
	protected final static int HTTP_SOCKET_TIMEOUT = 30000;

	public final static String FILTER_PROCESSES_URL = "/openid_connect_login";

	// Allow for time sync issues by having a window of X seconds.
	private int timeSkewAllowance = 300;

	// fetches and caches public keys for servers
	@Autowired(required=false)
	private JWKSetCacheService validationServices;

	// creates JWT signer/validators for symmetric keys
	@Autowired(required=false)
	private SymmetricKeyJWTValidatorCacheService symmetricCacheService;

	// signer based on keypair for this client (for outgoing auth requests)
	@Autowired(required=false)
	private JWTSigningAndValidationService authenticationSignerService;


	/*
	 * Modular services to build out client filter.
	 */
	// looks at the request and determines which issuer to use for lookup on the server
	private IssuerService issuerService;
	// holds server information (auth URI, token URI, etc.), indexed by issuer
	private ServerConfigurationService servers;
	// holds client information (client ID, redirect URI, etc.), indexed by issuer of the server
	private ClientConfigurationService clients;
	// provides extra options to inject into the outbound request
	private AuthRequestOptionsService authOptions = new StaticAuthRequestOptionsService(); // initialize with an empty set of options
	// builds the actual request URI based on input from all other services
	private AuthRequestUrlBuilder authRequestBuilder;

	// private helpers to handle target link URLs
	private TargetLinkURIAuthenticationSuccessHandler targetSuccessHandler = new TargetLinkURIAuthenticationSuccessHandler();
	private TargetLinkURIChecker deepLinkFilter;

	protected int httpSocketTimeout = HTTP_SOCKET_TIMEOUT;

	/**
	 * OpenIdConnectAuthenticationFilter constructor
	 */
	public OIDCAuthenticationFilter() {
		super(FILTER_PROCESSES_URL);
		targetSuccessHandler.passthrough = super.getSuccessHandler();
		super.setAuthenticationSuccessHandler(targetSuccessHandler);
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		// if our JOSE validators don't get wired in, drop defaults into place

		if (validationServices == null) {
			validationServices = new JWKSetCacheService();
		}

		if (symmetricCacheService == null) {
			symmetricCacheService = new SymmetricKeyJWTValidatorCacheService();
		}

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

			if (!Strings.isNullOrEmpty(issResp.getTargetLinkUri())) {
				// there's a target URL in the response, we should save this so we can forward to it later
				session.setAttribute(TARGET_SESSION_VARIABLE, issResp.getTargetLinkUri());
			}

			if (Strings.isNullOrEmpty(issuer)) {
				logger.error("No issuer found: " + issuer);
				throw new AuthenticationServiceException("No issuer found: " + issuer);
			}

			ServerConfiguration serverConfig = servers.getServerConfiguration(issuer);
			if (serverConfig == null) {
				logger.error("No server configuration found for issuer: " + issuer);
				throw new AuthenticationServiceException("No server configuration found for issuer: " + issuer);
			}


			session.setAttribute(ISSUER_SESSION_VARIABLE, serverConfig.getIssuer());

			RegisteredClient clientConfig = clients.getClientConfiguration(serverConfig);
			if (clientConfig == null) {
				logger.error("No client configuration found for issuer: " + issuer);
				throw new AuthenticationServiceException("No client configuration found for issuer: " + issuer);
			}

			String redirectUri = null;
			if (clientConfig.getRegisteredRedirectUri() != null && clientConfig.getRegisteredRedirectUri().size() == 1) {
				// if there's a redirect uri configured (and only one), use that
				redirectUri = Iterables.getOnlyElement(clientConfig.getRegisteredRedirectUri());
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
			
			// if we're using PKCE, handle the challenge here
			if (clientConfig.getCodeChallengeMethod() != null) {
				String codeVerifier = createCodeVerifier(session);
				options.put("code_challenge_method", clientConfig.getCodeChallengeMethod().getName());
				if (clientConfig.getCodeChallengeMethod().equals(PKCEAlgorithm.plain)) {
					options.put("code_challenge", codeVerifier);
				} else if (clientConfig.getCodeChallengeMethod().equals(PKCEAlgorithm.S256)) {
					try {
						MessageDigest digest = MessageDigest.getInstance("SHA-256");
						String hash = Base64URL.encode(digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII))).toString();
						options.put("code_challenge", hash);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					
				}
			}

			String authRequest = authRequestBuilder.buildAuthRequestUrl(serverConfig, clientConfig, redirectUri, nonce, state, options, issResp.getLoginHint());

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
		String requestState = request.getParameter("state");
		if (storedState == null || !storedState.equals(requestState)) {
			throw new AuthenticationServiceException("State parameter mismatch on return. Expected " + storedState + " got " + requestState);
		}

		// look up the issuer that we set out to talk to
		String issuer = getStoredSessionString(session, ISSUER_SESSION_VARIABLE);

		// pull the configurations based on that issuer
		ServerConfiguration serverConfig = servers.getServerConfiguration(issuer);
		final RegisteredClient clientConfig = clients.getClientConfiguration(serverConfig);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("code", authorizationCode);
		form.setAll(authOptions.getTokenOptions(serverConfig, clientConfig, request));
		
		String codeVerifier = getStoredCodeVerifier(session);
		if (codeVerifier != null) {
			form.add("code_verifier", codeVerifier);
		}

		String redirectUri = getStoredSessionString(session, REDIRECT_URI_SESION_VARIABLE);
		if (redirectUri != null) {
			form.add("redirect_uri", redirectUri);
		}

		// Handle Token Endpoint interaction

		HttpClient httpClient = HttpClientBuilder.create()
				.useSystemProperties()
				.setDefaultRequestConfig(
						RequestConfig.custom()
						.setSocketTimeout(httpSocketTimeout)
						.build()
						)
						.build();

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		RestTemplate restTemplate;

		if (SECRET_BASIC.equals(clientConfig.getTokenEndpointAuthMethod())){
			// use BASIC auth if configured to do so
			restTemplate = new RestTemplate(factory) {

				@Override
				protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
					ClientHttpRequest httpRequest = super.createRequest(url, method);
					httpRequest.getHeaders().add("Authorization",
							String.format("Basic %s", Base64.encode(String.format("%s:%s",
									UriUtils.encodePathSegment(clientConfig.getClientId(), "UTF-8"),
									UriUtils.encodePathSegment(clientConfig.getClientSecret(), "UTF-8")))));

					return httpRequest;
				}
			};
		} else {
			// we're not doing basic auth, figure out what other flavor we have
			restTemplate = new RestTemplate(factory);

			if (SECRET_JWT.equals(clientConfig.getTokenEndpointAuthMethod()) || PRIVATE_KEY.equals(clientConfig.getTokenEndpointAuthMethod())) {
				// do a symmetric secret signed JWT for auth


				JWTSigningAndValidationService signer = null;
				JWSAlgorithm alg = clientConfig.getTokenEndpointAuthSigningAlg();

				if (SECRET_JWT.equals(clientConfig.getTokenEndpointAuthMethod()) &&
						(alg.equals(JWSAlgorithm.HS256)
								|| alg.equals(JWSAlgorithm.HS384)
								|| alg.equals(JWSAlgorithm.HS512))) {

					// generate one based on client secret
					signer = symmetricCacheService.getSymmetricValidtor(clientConfig.getClient());

				} else if (PRIVATE_KEY.equals(clientConfig.getTokenEndpointAuthMethod())) {

					// needs to be wired in to the bean
					signer = authenticationSignerService;

					if (alg == null) {
						alg = authenticationSignerService.getDefaultSigningAlgorithm();
					}
				}

				if (signer == null) {
					throw new AuthenticationServiceException("Couldn't find required signer service for use with private key auth.");
				}

				JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();

				claimsSet.issuer(clientConfig.getClientId());
				claimsSet.subject(clientConfig.getClientId());
				claimsSet.audience(Lists.newArrayList(serverConfig.getTokenEndpointUri()));
				claimsSet.jwtID(UUID.randomUUID().toString());

				// TODO: make this configurable
				Date exp = new Date(System.currentTimeMillis() + (60 * 1000)); // auth good for 60 seconds
				claimsSet.expirationTime(exp);

				Date now = new Date(System.currentTimeMillis());
				claimsSet.issueTime(now);
				claimsSet.notBeforeTime(now);

				JWSHeader header = new JWSHeader(alg, null, null, null, null, null, null, null, null, null,
						signer.getDefaultSignerKeyId(),
						null, null);
				SignedJWT jwt = new SignedJWT(header, claimsSet.build());

				signer.signJwt(jwt, alg);

				form.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
				form.add("client_assertion", jwt.serialize());
			} else {
				//Alternatively use form based auth
				form.add("client_id", clientConfig.getClientId());
				form.add("client_secret", clientConfig.getClientSecret());
			}

		}

		logger.debug("tokenEndpointURI = " + serverConfig.getTokenEndpointUri());
		logger.debug("form = " + form);

		String jsonString = null;

		try {
			jsonString = restTemplate.postForObject(serverConfig.getTokenEndpointUri(), form, String.class);
		} catch (RestClientException e) {

			// Handle error

			logger.error("Token Endpoint error response:  " + e.getMessage());

			throw new AuthenticationServiceException("Unable to obtain Access Token: " + e.getMessage());
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
				JWT idToken = JWTParser.parse(idTokenValue);

				// validate our ID Token over a number of tests
				JWTClaimsSet idClaims = idToken.getJWTClaimsSet();

				// check the signature
				JWTSigningAndValidationService jwtValidator = null;

				Algorithm tokenAlg = idToken.getHeader().getAlgorithm();

				Algorithm clientAlg = clientConfig.getIdTokenSignedResponseAlg();

				if (clientAlg != null) {
					if (!clientAlg.equals(tokenAlg)) {
						throw new AuthenticationServiceException("Token algorithm " + tokenAlg + " does not match expected algorithm " + clientAlg);
					}
				}

				if (idToken instanceof PlainJWT) {

					if (clientAlg == null) {
						throw new AuthenticationServiceException("Unsigned ID tokens can only be used if explicitly configured in client.");
					}

					if (tokenAlg != null && !tokenAlg.equals(Algorithm.NONE)) {
						throw new AuthenticationServiceException("Unsigned token received, expected signature with " + tokenAlg);
					}
				} else if (idToken instanceof SignedJWT) {

					SignedJWT signedIdToken = (SignedJWT)idToken;

					if (tokenAlg.equals(JWSAlgorithm.HS256)
							|| tokenAlg.equals(JWSAlgorithm.HS384)
							|| tokenAlg.equals(JWSAlgorithm.HS512)) {

						// generate one based on client secret
						jwtValidator = symmetricCacheService.getSymmetricValidtor(clientConfig.getClient());
					} else {
						// otherwise load from the server's public key
						jwtValidator = validationServices.getValidator(serverConfig.getJwksUri());
					}

					if (jwtValidator != null) {
						if(!jwtValidator.validateSignature(signedIdToken)) {
							throw new AuthenticationServiceException("Signature validation failed");
						}
					} else {
						logger.error("No validation service found. Skipping signature validation");
						throw new AuthenticationServiceException("Unable to find an appropriate signature validator for ID Token.");
					}
				} // TODO: encrypted id tokens

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

				// construct an PendingOIDCAuthenticationToken and return a Authentication object w/the userId and the idToken

				PendingOIDCAuthenticationToken token = new PendingOIDCAuthenticationToken(idClaims.getSubject(), idClaims.getIssuer(),
						serverConfig,
						idToken, accessTokenValue, refreshTokenValue);

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
	
	/**
	 * Create a random code challenge and store it in the session
	 * @param session
	 * @return
	 */
	protected static String createCodeVerifier(HttpSession session) {
		String challenge = new BigInteger(50, new SecureRandom()).toString(16);
		session.setAttribute(CODE_VERIFIER_SESSION_VARIABLE, challenge);
		return challenge;
	}
	
	/**
	 * Retrieve the stored challenge from our session
	 * @param session
	 * @return
	 */
	protected static String getStoredCodeVerifier(HttpSession session) {
		return getStoredSessionString(session, CODE_VERIFIER_SESSION_VARIABLE);
	}


	@Override
	public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
		targetSuccessHandler.passthrough = successHandler;
		super.setAuthenticationSuccessHandler(targetSuccessHandler);
	}




	/**
	 * Handle a successful authentication event. If the issuer service sets
	 * a target URL, we'll go to that. Otherwise we'll let the superclass handle
	 * it for us with the configured behavior.
	 */
	protected class TargetLinkURIAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

		private AuthenticationSuccessHandler passthrough;

		@Override
		public void onAuthenticationSuccess(HttpServletRequest request,
				HttpServletResponse response, Authentication authentication)
						throws IOException, ServletException {

			HttpSession session = request.getSession();

			// check to see if we've got a target
			String target = getStoredSessionString(session, TARGET_SESSION_VARIABLE);

			if (!Strings.isNullOrEmpty(target)) {
				session.removeAttribute(TARGET_SESSION_VARIABLE);

				target = deepLinkFilter.filter(target);

				response.sendRedirect(target);
			} else {
				// if the target was blank, use the default behavior here
				passthrough.onAuthenticationSuccess(request, response, authentication);
			}

		}

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

	public SymmetricKeyJWTValidatorCacheService getSymmetricCacheService() {
		return symmetricCacheService;
	}

	public void setSymmetricCacheService(SymmetricKeyJWTValidatorCacheService symmetricCacheService) {
		this.symmetricCacheService = symmetricCacheService;
	}

	public TargetLinkURIAuthenticationSuccessHandler getTargetLinkURIAuthenticationSuccessHandler() {
		return targetSuccessHandler;
	}

	public void setTargetLinkURIAuthenticationSuccessHandler(
			TargetLinkURIAuthenticationSuccessHandler targetSuccessHandler) {
		this.targetSuccessHandler = targetSuccessHandler;
	}

	public TargetLinkURIChecker targetLinkURIChecker() {
		return deepLinkFilter;
	}

	public void setTargetLinkURIChecker(TargetLinkURIChecker deepLinkFilter) {
		this.deepLinkFilter = deepLinkFilter;
	}

}
