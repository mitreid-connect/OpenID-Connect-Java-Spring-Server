/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
package org.mitre.openid.connect;

import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.jwt.signer.service.impl.SymmetricCacheService;
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

@Component("connectOAuth2RequestFactory")
public class ConnectOAuth2RequestFactory extends DefaultOAuth2RequestFactory {

	private static Logger logger = LoggerFactory.getLogger(ConnectOAuth2RequestFactory.class);

	private ClientDetailsEntityService clientDetailsService;

	@Autowired
	private JWKSetCacheService validators;

	@Autowired
	private SymmetricCacheService symmetricCacheService;

	@Autowired
	private SystemScopeService systemScopes;

	@Autowired
	private JwtEncryptionAndDecryptionService encryptionService;

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

		if (inputParams.containsKey("prompt")) {
			request.getExtensions().put("prompt", inputParams.get("prompt"));
		}
		if (inputParams.containsKey("nonce")) {
			request.getExtensions().put("nonce", inputParams.get("nonce"));
		}

		if (inputParams.containsKey("claims")) {
			JsonObject claimsRequest = parseClaimRequest(inputParams.get("claims"));
			if (claimsRequest != null) {
				request.getExtensions().put("claims", claimsRequest.toString());
			}
		}

		if (inputParams.containsKey("max_age")) {
			request.getExtensions().put("max_age", inputParams.get("max_age"));
		}

		if (inputParams.containsKey("request")) {
			request.getExtensions().put("request", inputParams.get("request"));
			processRequestObject(inputParams.get("request"), request);
		}

		if (request.getClientId() != null) {
			try {
				ClientDetailsEntity client = clientDetailsService.loadClientByClientId(request.getClientId());

				if ((request.getScope() == null || request.getScope().isEmpty())) {
					Set<String> clientScopes = client.getScope();
					request.setScope(clientScopes);
				}

				if (request.getExtensions().get("max_age") == null && client.getDefaultMaxAge() != null) {
					request.getExtensions().put("max_age", client.getDefaultMaxAge().toString());
				}
			} catch (OAuth2Exception e) {
				logger.error("Caught OAuth2 exception trying to test client scopes and max age:", e);
			}
		}


		// add CSRF protection to the request on first parse
		String csrf = UUID.randomUUID().toString();
		request.getExtensions().put("csrf", csrf);



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

			// TODO: move keys to constants

			if (jwt instanceof SignedJWT) {
				// it's a signed JWT, check the signature

				SignedJWT signedJwt = (SignedJWT)jwt;

				// need to check clientId first so that we can load the client to check other fields
				if (request.getClientId() == null) {
					request.setClientId(signedJwt.getJWTClaimsSet().getStringClaim("client_id"));
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
						|| alg.equals(JWSAlgorithm.RS512)) {

					// it's RSA, need to find the JWK URI and fetch the key

					if (client.getJwksUri() == null) {
						throw new InvalidClientException("Client must have a JWKS URI registered to use signed request objects.");
					}

					// check JWT signature
					JwtSigningAndValidationService validator = validators.getValidator(client.getJwksUri());

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

					JwtSigningAndValidationService validator = symmetricCacheService.getSymmetricValidtor(client);

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
					request.setClientId(plainJwt.getJWTClaimsSet().getStringClaim("client_id"));
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
					request.setClientId(encryptedJWT.getJWTClaimsSet().getStringClaim("client_id"));
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

			Set<String> responseTypes = OAuth2Utils.parseParameterList(claims.getStringClaim("response_type"));
			if (responseTypes != null && !responseTypes.isEmpty()) {
				if (!responseTypes.equals(request.getResponseTypes())) {
					logger.info("Mismatch between request object and regular parameter for response_type, using request object");
				}
				request.setResponseTypes(responseTypes);
			}

			String redirectUri = claims.getStringClaim("redirect_uri");
			if (redirectUri != null) {
				if (!redirectUri.equals(request.getRedirectUri())) {
					logger.info("Mismatch between request object and regular parameter for redirect_uri, using request object");
				}
				request.setRedirectUri(redirectUri);
			}

			String state = claims.getStringClaim("state");
			if(state != null) {
				if (!state.equals(request.getState())) {
					logger.info("Mismatch between request object and regular parameter for state, using request object");
				}
				request.setState(state);
			}

			String nonce = claims.getStringClaim("nonce");
			if(nonce != null) {
				if (!nonce.equals(request.getExtensions().get("nonce"))) {
					logger.info("Mismatch between request object and regular parameter for nonce, using request object");
				}
				request.getExtensions().put("nonce", nonce);
			}

			String display = claims.getStringClaim("display");
			if (display != null) {
				if (!display.equals(request.getExtensions().get("display"))) {
					logger.info("Mismatch between request object and regular parameter for display, using request object");
				}
				request.getExtensions().put("display", display);
			}

			String prompt = claims.getStringClaim("prompt");
			if (prompt != null) {
				if (!prompt.equals(request.getExtensions().get("prompt"))) {
					logger.info("Mismatch between request object and regular parameter for prompt, using request object");
				}
				request.getExtensions().put("prompt", prompt);
			}

			Set<String> scope = OAuth2Utils.parseParameterList(claims.getStringClaim("scope"));
			if (scope != null && !scope.isEmpty()) {
				if (!scope.equals(request.getScope())) {
					logger.info("Mismatch between request object and regular parameter for scope, using request object");
				}
				request.setScope(scope);
			}

			JsonObject claimRequest = parseClaimRequest(claims.getStringClaim("claims"));
			if (claimRequest != null) {
				if (!claimRequest.equals(parseClaimRequest(request.getExtensions().get("claims").toString()))) {
					logger.info("Mismatch between request object and regular parameter for claims, using request object");
				}
				// we save the string because the object might not be a Java Serializable, and we can parse it easily enough anyway
				request.getExtensions().put("claims", claimRequest.toString());
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
		JsonElement el = parser .parse(claimRequestString);
		if (el != null && el.isJsonObject()) {
			return el.getAsJsonObject();
		} else {
			return null;
		}
	}

}
