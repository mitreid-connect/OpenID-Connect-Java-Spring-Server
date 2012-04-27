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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * The OpenID Connect Authentication Filter
 * 
 * Configured like:
 * 
 * <security:http auto-config="false" use-expressions="true"
 * disable-url-rewriting="true" entry-point-ref="authenticationEntryPoint"
 * pattern="/**">
 * 
 * <security:intercept-url pattern="/somepath/**" access="denyAll" />
 * 
 * <security:custom-filter before="PRE_AUTH_FILTER "
 * ref="openIdConnectAuthenticationFilter" />
 * 
 * <security:intercept-url pattern="/**"
 * access="hasAnyRole('ROLE_USER','ROLE_ADMIN')" /> <security:logout />
 * </security:http>
 * 
 * <bean id="authenticationEntryPoint" class=
 * "org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint"
 * > <property name="loginFormUrl" value="/openid_connect_login"/> </bean>
 * 
 * <security:authentication-manager alias="authenticationManager" /> <bean
 * id="openIdConnectAuthenticationFilter"
 * class="org.mitre.openid.connect.client.OpenIdConnectAuthenticationFilter">
 * 
 * <property name="authenticationManager" ref="authenticationManager" />
 * <property name="errorRedirectURI" value="/login.jsp?authfail=openid" /> <!--
 * TODO: or would this be value="/login.jsp?authfail=openid_connect" -->
 * <property name="authorizationEndpointURI" value=
 * "http://sever.example.com:8080/openid-connect-server/openidconnect/auth" />
 * <property name="tokenEndpointURI"
 * value="http://sever.example.com:8080/openid-connect-server/checkid" />
 * <property name="checkIDEndpointURI"
 * value="http://sever.example.com:8080/openid-connect-server/checkid" />
 * <property name="clientId" value="someClientId" /> <property
 * name="clientSecret" value="someClientSecret" /> </bean>
 * 
 * @author nemonik
 * 
 */
public class OpenIdConnectAuthenticationFilter extends
		AbstractAuthenticationProcessingFilter {

	private final static int HTTP_SOCKET_TIMEOUT = 30000;
	private final static String SCOPE = "openid";
	private final static int KEY_SIZE = 1024;
	private final static String SIGNING_ALGORITHM = "SHA256withRSA";
	private final static String NONCE_SIGNATURE_COOKIE_NAME = "nonce";
	private final static String FILTER_PROCESSES_URL = "/openid_connect_login";

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
	public static String buildRedirectURI(HttpServletRequest request,
			String[] ignoreFields) {

		List<String> ignore = (ignoreFields != null) ? Arrays
				.asList(ignoreFields) : null;

		boolean isFirst = true;

		StringBuffer sb = request.getRequestURL();

		for (Enumeration<?> e = request.getParameterNames(); e
				.hasMoreElements();) {

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
	public static String buildURL(String baseURI,
			Map<String, String> queryStringFields) {

		StringBuilder URLBuilder = new StringBuilder(baseURI);

		char appendChar = '?';

		for (Map.Entry<String, String> param : queryStringFields.entrySet()) {
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
	 * @param signer
	 *            The algorithm to sign with
	 * @param privateKey
	 *            The private key to sign with
	 * @param data
	 *            The data to be signed
	 * @return The signature text
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
	 *            The data
	 * @param sigText
	 *            The signature text
	 * @return True if valid, false if not
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

	private String errorRedirectURI;

	private String authorizationEndpointURI;

	private String tokenEndpointURI;

	private String checkIDEndpointURI;

	private String clientSecret;

	private String clientId;

	private String scope;

	private int httpSocketTimeout = HTTP_SOCKET_TIMEOUT;

	private PublicKey publicKey;

	private PrivateKey privateKey;

	private Signature signer;

	/**
	 * OpenIdConnectAuthenticationFilter constructor
	 */
	protected OpenIdConnectAuthenticationFilter() {
		super(FILTER_PROCESSES_URL);
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

		Assert.notNull(errorRedirectURI,
				"An Error Redirect URI must be supplied");

		Assert.notNull(authorizationEndpointURI,
				"An Authorization Endpoint URI must be supplied");

		Assert.notNull(tokenEndpointURI,
				"A Token ID Endpoint URI must be supplied");

		Assert.notNull(checkIDEndpointURI,
				"A Check ID Endpoint URI must be supplied");

		Assert.notNull(clientId, "A Client ID must be supplied");

		Assert.notNull(clientSecret, "A Client Secret must be supplied");

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

		// prepend the spec necessary SCOPE
		setScope((scope != null && !scope.isEmpty()) ? SCOPE + " " + scope
				: SCOPE);
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

			handleError(request, response);

		} else {

			// Determine if the Authorization Endpoint issued an
			// authorization grant

			if (request.getParameter("code") != null) {

				return handleAuthorizationGrantResponse(request);

			} else {

				handleAuthorizationRequest(request, response);
			}
		}

		return null;
	}

	/**
	 * Handles the authorization grant response
	 * 
	 * @param request
	 *            The request from which to extract parameters and perform the
	 *            authentication
	 * @return The authenticated user token, or null if authentication is
	 *         incomplete.
	 */
	private Authentication handleAuthorizationGrantResponse(
			HttpServletRequest request) {

		final boolean debug = logger.isDebugEnabled();

		String authorizationGrant = request.getParameter("code");

		// Handle Token Endpoint interaction
		HttpClient httpClient = new DefaultHttpClient();

		httpClient.getParams().setParameter("http.socket.timeout",
				new Integer(httpSocketTimeout));

		//
		// TODO: basic auth is untested (it wasn't working last I
		// tested)
		// UsernamePasswordCredentials credentials = new
		// UsernamePasswordCredentials(
		// clientId, clientSecret);
		// ((DefaultHttpClient) httpClient).getCredentialsProvider()
		// .setCredentials(AuthScope.ANY, credentials);
		//

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
				httpClient);

		RestTemplate restTemplate = new RestTemplate(factory);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("grant_type", "authorization_code");
		form.add("code", authorizationGrant);
		form.add("redirect_uri", OpenIdConnectAuthenticationFilter
				.buildRedirectURI(request, new String[] { "code" }));

		// pass clientId and clientSecret in post of request
		form.add("client_id", clientId);
		form.add("client_secret", clientSecret);

		if (debug) {
			logger.debug("tokenEndpointURI = " + tokenEndpointURI);
			logger.debug("form = " + form);
		}

		String jsonString = null;

		try {
			jsonString = restTemplate.postForObject(tokenEndpointURI, form,
					String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			// Handle error

			logger.error("Token Endpoint error response:  "
					+ httpClientErrorException.getStatusText() + " : "
					+ httpClientErrorException.getMessage());

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.");
		}

		JsonElement jsonRoot = new JsonParser().parse(jsonString);

		if (jsonRoot.getAsJsonObject().get("error") != null) {

			// Handle error

			String error = jsonRoot.getAsJsonObject().get("error")
					.getAsString();

			logger.error("Token Endpoint returned: " + error);

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.  Token Endpoint returned: "
							+ error);

		} else {

			// Extract the id_token to insert into the
			// OpenIdConnectAuthenticationToken

			IdToken idToken = null;

			if (jsonRoot.getAsJsonObject().get("id_token") != null) {

				try {
					idToken = IdToken.parse(jsonRoot.getAsJsonObject()
							.get("id_token").getAsString());

					List<String> parts = Lists.newArrayList(Splitter.on(".")
							.split(jsonRoot.getAsJsonObject().get("id_token")
									.getAsString()));

					if (parts.size() != 3) {
						throw new IllegalArgumentException(
								"Invalid JWT format.");
					}

					String h64 = parts.get(0);
					String c64 = parts.get(1);
					String s64 = parts.get(2);
					
					logger.debug("h64 = " + h64);
					logger.debug("c64 = " + c64);
					logger.debug("s64 = " + s64);
					

				} catch (Exception e) {

					// I suspect this could happen

					logger.error("Problem parsing id_token:  " + e);
					// e.printStackTrace();

					throw new AuthenticationServiceException(
							"Problem parsing id_token return from Token endpoint: "
									+ e);
				}

			} else {

				// An error is unlikely, but it good security to check

				logger.error("Token Endpoint did not return a token_id");

				throw new AuthenticationServiceException(
						"Token Endpoint did not return a token_id");
			}

			// Handle Check ID Endpoint interaction

			httpClient = new DefaultHttpClient();

			httpClient.getParams().setParameter("http.socket.timeout",
					new Integer(httpSocketTimeout));

			factory = new HttpComponentsClientHttpRequestFactory(httpClient);
			restTemplate = new RestTemplate(factory);

			form = new LinkedMultiValueMap<String, String>();

			form.add("access_token", jsonRoot.getAsJsonObject().get("id_token")
					.getAsString());

			jsonString = null;

			try {
				jsonString = restTemplate.postForObject(checkIDEndpointURI,
						form, String.class);
			} catch (HttpClientErrorException httpClientErrorException) {

				// Handle error

				logger.error("Check ID Endpoint error response:  "
						+ httpClientErrorException.getStatusText() + " : "
						+ httpClientErrorException.getMessage());

				throw new AuthenticationServiceException("Unable check token.");
			}

			jsonRoot = new JsonParser().parse(jsonString);

			// String iss = jsonRoot.getAsJsonObject().get("iss")
			// .getAsString();
			String userId = jsonRoot.getAsJsonObject().get("user_id")
					.getAsString();
			// String aud = jsonRoot.getAsJsonObject().get("aud")
			// .getAsString();
			String nonce = jsonRoot.getAsJsonObject().get("nonce")
					.getAsString();
			// String exp = jsonRoot.getAsJsonObject().get("exp")
			// .getAsString();

			// Compare returned ID Token to signed session cookie
			// to detect ID Token replay by third parties.

			Cookie nonceSignatureCookie = WebUtils.getCookie(request,
					NONCE_SIGNATURE_COOKIE_NAME);

			if (nonceSignatureCookie != null) {

				String sigText = nonceSignatureCookie.getValue();

				if (sigText != null && !sigText.isEmpty()) {

					if (!verify(signer, publicKey, nonce, sigText)) {
						logger.error("Possible replay attack detected! "
								+ "The comparison of the nonce in the returned "
								+ "ID Token to the signed session "
								+ NONCE_SIGNATURE_COOKIE_NAME + " failed.");

						throw new AuthenticationServiceException(
								"Possible replay attack detected! "
										+ "The comparison of the nonce in the returned "
										+ "ID Token to the signed session "
										+ NONCE_SIGNATURE_COOKIE_NAME
										+ " failed.");
					}

				} else {
					logger.error(NONCE_SIGNATURE_COOKIE_NAME
							+ " was found, but was null or empty.");

					throw new AuthenticationServiceException(
							NONCE_SIGNATURE_COOKIE_NAME
									+ " was found, but was null or empty.");
				}

			} else {

				logger.error(NONCE_SIGNATURE_COOKIE_NAME
						+ " cookie was not found.");

				throw new AuthenticationServiceException(
						NONCE_SIGNATURE_COOKIE_NAME + " cookie was not found.");
			}

			// Create an Authentication object for the token, and
			// return.

			OpenIdConnectAuthenticationToken token = new OpenIdConnectAuthenticationToken(
					userId, idToken);

			Authentication authentication = this.getAuthenticationManager()
					.authenticate(token);

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
	 * @throws IOException
	 *             If an input or output exception occurs
	 */
	private void handleAuthorizationRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		Map<String, String> urlVariables = new HashMap<String, String>();

		// Required parameters:

		urlVariables.put("response_type", "code");
		urlVariables.put("client_id", clientId);
		urlVariables.put("scope", scope);
		urlVariables.put("redirect_uri", OpenIdConnectAuthenticationFilter
				.buildRedirectURI(request, null));

		// Create a string value used to associate a user agent session
		// with an ID Token to mitigate replay attacks. The value is
		// passed through unmodified to the ID Token. One method is to
		// store a random value as a signed session cookie, and pass the
		// value in the nonce parameter.

		String nonce = new BigInteger(50, new SecureRandom()).toString(16);

		Cookie nonceCookie = new Cookie(NONCE_SIGNATURE_COOKIE_NAME, sign(
				signer, privateKey, nonce.getBytes()));

		response.addCookie(nonceCookie);

		urlVariables.put("nonce", nonce);

		// Optional parameters:

		// TODO: display, prompt, request, request_uri

		response.sendRedirect(OpenIdConnectAuthenticationFilter.buildURL(
				authorizationEndpointURI, urlVariables));
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
	private void handleError(HttpServletRequest request,
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

		response.sendRedirect(OpenIdConnectAuthenticationFilter.buildURL(
				errorRedirectURI, requestParams));
	}

	public void setAuthorizationEndpointURI(String authorizationEndpointURI) {
		this.authorizationEndpointURI = authorizationEndpointURI;
	}

	public void setCheckIDEndpointURI(String checkIDEndpointURI) {
		this.checkIDEndpointURI = checkIDEndpointURI;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setErrorRedirectURI(String errorRedirectURI) {
		this.errorRedirectURI = errorRedirectURI;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setTokenEndpointURI(String tokenEndpointURI) {
		this.tokenEndpointURI = tokenEndpointURI;
	}
}
