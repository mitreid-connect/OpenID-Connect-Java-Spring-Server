/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/
package org.mitre.openid.connect.request;


import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mitre.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.jwt.signer.service.impl.SymmetricKeyJWTValidatorCacheService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWEObject.State;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.mitre.openid.connect.request.ConnectRequestParameters.CLAIMS;
import static org.mitre.openid.connect.request.ConnectRequestParameters.CLIENT_ID;
import static org.mitre.openid.connect.request.ConnectRequestParameters.CSRF;
import static org.mitre.openid.connect.request.ConnectRequestParameters.DISPLAY;
import static org.mitre.openid.connect.request.ConnectRequestParameters.LOGIN_HINT;
import static org.mitre.openid.connect.request.ConnectRequestParameters.MAX_AGE;
import static org.mitre.openid.connect.request.ConnectRequestParameters.NONCE;
import static org.mitre.openid.connect.request.ConnectRequestParameters.PROMPT;
import static org.mitre.openid.connect.request.ConnectRequestParameters.REDIRECT_URI;
import static org.mitre.openid.connect.request.ConnectRequestParameters.REQUEST;
import static org.mitre.openid.connect.request.ConnectRequestParameters.RESPONSE_TYPE;
import static org.mitre.openid.connect.request.ConnectRequestParameters.SCOPE;
import static org.mitre.openid.connect.request.ConnectRequestParameters.STATE;

@Component("connectOAuth2RequestFactory")
public class ConnectOAuth2RequestFactory extends DefaultOAuth2RequestFactory {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConnectOAuth2RequestFactory.class);

	private ClientDetailsEntityService clientDetailsService;

	@Autowired
	private JWKSetCacheService validators;

	@Autowired
	private SymmetricKeyJWTValidatorCacheService symmetricCacheService;

	@Autowired
	private SystemScopeService systemScopes;

	@Autowired
	private JWTEncryptionAndDecryptionService encryptionService;

	private JsonParser parser = new JsonParser();

	/**
	 * Constructor with arguments
	 * 
	 * @param clientDetailsService
	 * @param nonceService
	 */
	@Autowired
	public ConnectOAuth2RequestFactory(ClientDetailsEntityService clientDetailsService) {
		super(clientDetailsService);
		this.clientDetailsService = clientDetailsService;
	}

	@Override
	public AuthorizationRequest createAuthorizationRequest(Map<String, String> inputParams) {


		AuthorizationRequest request = new AuthorizationRequest(inputParams, Collections.<String, String> emptyMap(),
				inputParams.get(OAuth2Utils.CLIENT_ID),
				OAuth2Utils.parseParameterList(inputParams.get(OAuth2Utils.SCOPE)), null,
				null, false, inputParams.get(OAuth2Utils.STATE),
				inputParams.get(OAuth2Utils.REDIRECT_URI),
				OAuth2Utils.parseParameterList(inputParams.get(OAuth2Utils.RESPONSE_TYPE)));

		//Add extension parameters to the 'extensions' map

		if (inputParams.containsKey(PROMPT)) {
			request.getExtensions().put(PROMPT, inputParams.get(PROMPT));
		}
		if (inputParams.containsKey(NONCE)) {
			request.getExtensions().put(NONCE, inputParams.get(NONCE));
		}

		if (inputParams.containsKey(CLAIMS)) {
			JsonObject claimsRequest = parseClaimRequest(inputParams.get(CLAIMS));
			if (claimsRequest != null) {
				request.getExtensions().put(CLAIMS, claimsRequest.toString());
			}
		}

		if (inputParams.containsKey(MAX_AGE)) {
			request.getExtensions().put(MAX_AGE, inputParams.get(MAX_AGE));
		}

		if (inputParams.containsKey(LOGIN_HINT)) {
			request.getExtensions().put(LOGIN_HINT, inputParams.get(LOGIN_HINT));
		}

		if (inputParams.containsKey(REQUEST)) {
			request.getExtensions().put(REQUEST, inputParams.get(REQUEST));
			processRequestObject(inputParams.get(REQUEST), request);
		}

		if (request.getClientId() != null) {
			try {
				ClientDetailsEntity client = clientDetailsService.loadClientByClientId(request.getClientId());

				if ((request.getScope() == null || request.getScope().isEmpty())) {
					Set<String> clientScopes = client.getScope();
					request.setScope(clientScopes);
				}

				if (request.getExtensions().get(MAX_AGE) == null && client.getDefaultMaxAge() != null) {
					request.getExtensions().put(MAX_AGE, client.getDefaultMaxAge().toString());
				}
			} catch (OAuth2Exception e) {
				logger.error("Caught OAuth2 exception trying to test client scopes and max age:", e);
			}
		}


		// add CSRF protection to the request on first parse
		String csrf = UUID.randomUUID().toString();
		request.getExtensions().put(CSRF, csrf);



		return request;
	}

	/**
	 * @param inputParams
	 * @return
	 */
	private void processRequestObject(String jwtString, AuthorizationRequest request) {

		// parse the request object
		try {
			JWT jwt = JWTParser.parse(jwtString);

			if (jwt instanceof SignedJWT) {
				// it's a signed JWT, check the signature

				SignedJWT signedJwt = (SignedJWT)jwt;

				// need to check clientId first so that we can load the client to check other fields
				if (request.getClientId() == null) {
					request.setClientId(signedJwt.getJWTClaimsSet().getStringClaim(CLIENT_ID));
				}

				ClientDetailsEntity client = clientDetailsService.loadClientByClientId(request.getClientId());

				if (client == null) {
					throw new InvalidClientException("Client not found: " + request.getClientId());
				}


				JWSAlgorithm alg = signedJwt.getHeader().getAlgorithm();

				if (client.getRequestObjectSigningAlg() == null ||
						!client.getRequestObjectSigningAlg().equals(alg)) {
					throw new InvalidClientException("Client's registered request object signing algorithm (" + client.getRequestObjectSigningAlg() + ") does not match request object's actual algorithm (" + alg.getName() + ")");
				}

				if (alg.equals(JWSAlgorithm.RS256)
						|| alg.equals(JWSAlgorithm.RS384)
						|| alg.equals(JWSAlgorithm.RS512)
						|| alg.equals(JWSAlgorithm.ES256)
						|| alg.equals(JWSAlgorithm.ES384)
						|| alg.equals(JWSAlgorithm.ES512)
						|| alg.equals(JWSAlgorithm.PS256)
						|| alg.equals(JWSAlgorithm.PS384)
						|| alg.equals(JWSAlgorithm.PS512)) {

					// it's a public key, need to find the JWK URI and fetch the key

					if (client.getJwksUri() == null) {
						throw new InvalidClientException("Client must have a JWKS registered to use signed request objects with a public key.");
					}

					// check JWT signature
					JWTSigningAndValidationService validator = validators.getValidator(client.getJwksUri());

					if (validator == null) {
						throw new InvalidClientException("Unable to create signature validator for client's JWKS URI: " + client.getJwksUri());
					}

					if (!validator.validateSignature(signedJwt)) {
						throw new InvalidClientException("Signature did not validate for presented JWT request object.");
					}
				} else if (alg.equals(JWSAlgorithm.HS256)
						|| alg.equals(JWSAlgorithm.HS384)
						|| alg.equals(JWSAlgorithm.HS512)) {

					// it's HMAC, we need to make a validator based on the client secret

					JWTSigningAndValidationService validator = symmetricCacheService.getSymmetricValidtor(client);

					if (validator == null) {
						throw new InvalidClientException("Unable to create signature validator for client's secret: " + client.getClientSecret());
					}

					if (!validator.validateSignature(signedJwt)) {
						throw new InvalidClientException("Signature did not validate for presented JWT request object.");
					}


				}


			} else if (jwt instanceof PlainJWT) {
				PlainJWT plainJwt = (PlainJWT)jwt;

				// need to check clientId first so that we can load the client to check other fields
				if (request.getClientId() == null) {
					request.setClientId(plainJwt.getJWTClaimsSet().getStringClaim(CLIENT_ID));
				}

				ClientDetailsEntity client = clientDetailsService.loadClientByClientId(request.getClientId());

				if (client == null) {
					throw new InvalidClientException("Client not found: " + request.getClientId());
				}

				if (client.getRequestObjectSigningAlg() == null) {
					throw new InvalidClientException("Client is not registered for unsigned request objects (no request_object_signing_alg registered)");
				} else if (!client.getRequestObjectSigningAlg().equals(Algorithm.NONE)) {
					throw new InvalidClientException("Client is not registered for unsigned request objects (request_object_signing_alg is " + client.getRequestObjectSigningAlg() +")");
				}

				// if we got here, we're OK, keep processing

			} else if (jwt instanceof EncryptedJWT) {

				EncryptedJWT encryptedJWT = (EncryptedJWT)jwt;

				// decrypt the jwt if we can

				encryptionService.decryptJwt(encryptedJWT);

				// TODO: what if the content is a signed JWT? (#525)

				if (!encryptedJWT.getState().equals(State.DECRYPTED)) {
					throw new InvalidClientException("Unable to decrypt the request object");
				}

				// need to check clientId first so that we can load the client to check other fields
				if (request.getClientId() == null) {
					request.setClientId(encryptedJWT.getJWTClaimsSet().getStringClaim(CLIENT_ID));
				}

				ClientDetailsEntity client = clientDetailsService.loadClientByClientId(request.getClientId());

				if (client == null) {
					throw new InvalidClientException("Client not found: " + request.getClientId());
				}


			}


			/*
			 * NOTE: Claims inside the request object always take precedence over those in the parameter map.
			 */

			// now that we've got the JWT, and it's been parsed, validated, and/or decrypted, we can process the claims

			ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();

			Set<String> responseTypes = OAuth2Utils.parseParameterList(claims.getStringClaim(RESPONSE_TYPE));
			if (responseTypes != null && !responseTypes.isEmpty()) {
				if (!responseTypes.equals(request.getResponseTypes())) {
					logger.info("Mismatch between request object and regular parameter for response_type, using request object");
				}
				request.setResponseTypes(responseTypes);
			}

			String redirectUri = claims.getStringClaim(REDIRECT_URI);
			if (redirectUri != null) {
				if (!redirectUri.equals(request.getRedirectUri())) {
					logger.info("Mismatch between request object and regular parameter for redirect_uri, using request object");
				}
				request.setRedirectUri(redirectUri);
			}

			String state = claims.getStringClaim(STATE);
			if(state != null) {
				if (!state.equals(request.getState())) {
					logger.info("Mismatch between request object and regular parameter for state, using request object");
				}
				request.setState(state);
			}

			String nonce = claims.getStringClaim(NONCE);
			if(nonce != null) {
				if (!nonce.equals(request.getExtensions().get(NONCE))) {
					logger.info("Mismatch between request object and regular parameter for nonce, using request object");
				}
				request.getExtensions().put(NONCE, nonce);
			}

			String display = claims.getStringClaim(DISPLAY);
			if (display != null) {
				if (!display.equals(request.getExtensions().get(DISPLAY))) {
					logger.info("Mismatch between request object and regular parameter for display, using request object");
				}
				request.getExtensions().put(DISPLAY, display);
			}

			String prompt = claims.getStringClaim(PROMPT);
			if (prompt != null) {
				if (!prompt.equals(request.getExtensions().get(PROMPT))) {
					logger.info("Mismatch between request object and regular parameter for prompt, using request object");
				}
				request.getExtensions().put(PROMPT, prompt);
			}

			Set<String> scope = OAuth2Utils.parseParameterList(claims.getStringClaim(SCOPE));
			if (scope != null && !scope.isEmpty()) {
				if (!scope.equals(request.getScope())) {
					logger.info("Mismatch between request object and regular parameter for scope, using request object");
				}
				request.setScope(scope);
			}

			JsonObject claimRequest = parseClaimRequest(claims.getStringClaim(CLAIMS));
			if (claimRequest != null) {
				if (!claimRequest.equals(parseClaimRequest(request.getExtensions().get(CLAIMS).toString()))) {
					logger.info("Mismatch between request object and regular parameter for claims, using request object");
				}
				// we save the string because the object might not be a Java Serializable, and we can parse it easily enough anyway
				request.getExtensions().put(CLAIMS, claimRequest.toString());
			}

			String loginHint = claims.getStringClaim(LOGIN_HINT);
			if (loginHint != null) {
				if (!loginHint.equals(request.getExtensions().get(LOGIN_HINT))) {
					logger.info("Mistmatch between request object and regular parameter for login_hint, using requst object");
				}
				request.getExtensions().put(LOGIN_HINT, loginHint);
			}

		} catch (ParseException e) {
			logger.error("ParseException while parsing RequestObject:", e);
		}
	}

	/**
	 * @param claimRequestString
	 * @return
	 */
	private JsonObject parseClaimRequest(String claimRequestString) {
		if (Strings.isNullOrEmpty(claimRequestString)) {
			return null;
		} else {
			JsonElement el = parser.parse(claimRequestString);
			if (el != null && el.isJsonObject()) {
				return el.getAsJsonObject();
			} else {
				return null;
			}
		}
	}

}
