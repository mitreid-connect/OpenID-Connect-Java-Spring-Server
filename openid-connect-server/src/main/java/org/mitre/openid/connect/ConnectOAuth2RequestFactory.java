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
package org.mitre.openid.connect;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.DefaultJwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetSigningAndValidationServiceCacheService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.Use;
import com.nimbusds.jose.util.Base64URL;
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
	private JWKSetSigningAndValidationServiceCacheService validators;
	
	@Autowired
	private SystemScopeService systemScopes;

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
	public OAuth2Request createOAuth2Request(AuthorizationRequest request) {
		return new OAuth2Request(request.getRequestParameters(), request.getClientId(), request.getAuthorities(), 
				request.isApproved(), request.getScope(), request.getResourceIds(), request.getRedirectUri(), request.getExtensions());
	}
	
	@Override
	public AuthorizationRequest createAuthorizationRequest(Map<String, String> inputParams) {

		Map<String, String> parameters = processRequestObject(inputParams);

		String clientId = parameters.get("client_id");
		ClientDetails client = null;

		if (clientId != null) {
			client = clientDetailsService.loadClientByClientId(clientId);
		}

		AuthorizationRequest request = new AuthorizationRequest(parameters, Collections.<String, String> emptyMap(),
				parameters.get(OAuth2Utils.CLIENT_ID),
				OAuth2Utils.parseParameterList(parameters.get(OAuth2Utils.SCOPE)), null,
				null, false, parameters.get(OAuth2Utils.STATE),
				parameters.get(OAuth2Utils.REDIRECT_URI),
				OAuth2Utils.parseParameterList(parameters.get(OAuth2Utils.RESPONSE_TYPE)));

		Set<String> scopes = OAuth2Utils.parseParameterList(parameters.get("scope"));
		if ((scopes == null || scopes.isEmpty()) && client != null) {
			Set<String> clientScopes = client.getScope();
			scopes = clientScopes;
		}

		request.setScope(scopes);

		return request;
	}

	/**
	 * @param inputParams
	 * @return
	 */
	private Map<String, String> processRequestObject(Map<String, String> inputParams) {

		String jwtString = inputParams.get("request");

		// if there's no request object, bail early
		if (Strings.isNullOrEmpty(jwtString)) {
			return inputParams;
		}

		// start by copying over what's already in there
		Map<String, String> parameters = new HashMap<String, String>(inputParams);

		// parse the request object
		try {
			JWT jwt = JWTParser.parse(jwtString);

			/*
			if (jwt instanceof EncryptedJWT) {
				// TODO: it's an encrypted JWT, decrypt it and use it
			} else {
				// it's not encrypted...
			}
			*/
			
			
			
			
			// TODO: check parameter consistency, move keys to constants
			
			if (jwt instanceof SignedJWT) {
				// it's a signed JWT, check the signature
				
				SignedJWT signedJwt = (SignedJWT)jwt;
			
				String clientId = inputParams.get("client_id");
				if (clientId == null) {
					clientId = signedJwt.getJWTClaimsSet().getStringClaim("client_id");
				}
				
				ClientDetailsEntity client = clientDetailsService.loadClientByClientId(clientId);
				
				if (client == null) {
					throw new InvalidClientException("Client not found: " + clientId);
				}
				
				
				JWSAlgorithm alg = signedJwt.getHeader().getAlgorithm();
				
				if (client.getRequestObjectSigningAlg() != null) {
					if (!client.getRequestObjectSigningAlg().equals(alg)) {
						throw new InvalidClientException("Client's registered request object signing algorithm (" + client.getRequestObjectSigningAlg() + ") does not match request object's actual algorithm (" + alg.getName() + ")");
					}
				}
				
				if (alg.equals(JWSAlgorithm.RS256)
						|| alg.equals(JWSAlgorithm.RS384)
						|| alg.equals(JWSAlgorithm.RS512)) {

					// it's RSA, need to find the JWK URI and fetch the key 

					if (client.getJwksUri() == null) {
						throw new InvalidClientException("Client must have a JWKS URI registered to use signed request objects.");
					}
					
					// check JWT signature
					JwtSigningAndValidationService validator = validators.get(client.getJwksUri());
					
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
					
					JwtSigningAndValidationService validator = getSymmetricValidtor(client);
					
					if (validator == null) {
						throw new InvalidClientException("Unable to create signature validator for client's secret: " + client.getClientSecret());
					}
					
					if (!validator.validateSignature(signedJwt)) {
						throw new InvalidClientException("Signature did not validate for presented JWT request object.");
					}
					
					
				}
					
				
			} else if (jwt instanceof PlainJWT) {
				PlainJWT plainJwt = (PlainJWT)jwt;
				
				String clientId = inputParams.get("client_id");
				if (clientId == null) {
					clientId = plainJwt.getJWTClaimsSet().getStringClaim("client_id");
				}
				
				ClientDetailsEntity client = clientDetailsService.loadClientByClientId(clientId);
				
				if (client == null) {
					throw new InvalidClientException("Client not found: " + clientId);
				}
				
				if (client.getRequestObjectSigningAlg() == null) { 
					throw new InvalidClientException("Client is not registered for unsigned request objects (no request_object_signing_alg registered)");
				} else if (!client.getRequestObjectSigningAlg().equals(Algorithm.NONE)) {
					throw new InvalidClientException("Client is not registered for unsigned request objects (request_object_signing_alg is " + client.getRequestObjectSigningAlg() +")");
				}
				
				// if we got here, we're OK, keep processing
				
			}
			
			/*
			 * if (in Claims):
			 * 		if (in params):
			 * 			if (equal):
			 * 				all set
			 * 			else (not equal):
			 * 				error
			 * 		else (not in params):
			 * 			add to params
			 * else (not in claims):
			 * 		we don't care
			 */

			ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
			
			String clientId = claims.getStringClaim("client_id");
			if (clientId != null) {
				parameters.put("client_id", clientId);
			}
			
			String responseTypes = claims.getStringClaim("response_type");
			if (responseTypes != null) {
				parameters.put("response_type", responseTypes);
			}

			if (claims.getStringClaim("redirect_uri") != null) {
				if (inputParams.containsKey("redirect_uri") == false) {
					parameters.put("redirect_uri", claims.getStringClaim("redirect_uri"));
				}
			}

			String state = claims.getStringClaim("state");
			if(state != null) {
				if (inputParams.containsKey("state") == false) {
					parameters.put("state", state);
				}
			}

			String nonce = claims.getStringClaim("nonce");
			if(nonce != null) {
				if (inputParams.containsKey("nonce") == false) {
					parameters.put("nonce", nonce);
				}
			}

			String display = claims.getStringClaim("display");
			if (display != null) {
				if (inputParams.containsKey("display") == false) {
					parameters.put("display", display);
				}
			}

			String prompt = claims.getStringClaim("prompt");
			if (prompt != null) {
				if (inputParams.containsKey("prompt") == false) {
					parameters.put("prompt", prompt);
				}
			}

			String scope = claims.getStringClaim("scope");
			if (scope != null) {
				if (inputParams.containsKey("scope") == false) {
					parameters.put("scope", scope);
				}
			}
		} catch (ParseException e) {
			logger.error("ParseException while parsing RequestObject:", e);
		}
		return parameters;
	}

	/**
	 * Create a symmetric signing and validation service for the given client
	 * 
	 * @param client
	 * @return
	 */
    private JwtSigningAndValidationService getSymmetricValidtor(ClientDetailsEntity client) {

    	if (client == null) {
    		logger.error("Couldn't create symmetric validator for null client");
    		return null;
    	}
    	
    	if (Strings.isNullOrEmpty(client.getClientSecret())) {
    		logger.error("Couldn't create symmetric validator for client " + client.getClientId() + " without a client secret");
    		return null;
    	}
    	
    	try {
    		
    		JWK jwk = new OctetSequenceKey(Base64URL.encode(client.getClientSecret()), Use.SIGNATURE, null, client.getClientId(), null, null, null);
    		Map<String, JWK> keys = ImmutableMap.of(client.getClientId(), jwk);
	        JwtSigningAndValidationService service = new DefaultJwtSigningAndValidationService(keys);
	        
	        return service;
	        
        } catch (NoSuchAlgorithmException e) {
	        // TODO Auto-generated catch block
	        logger.error("Couldn't create symmetric validator for client " + client.getClientId(), e);
        } catch (InvalidKeySpecException e) {
	        // TODO Auto-generated catch block
	        logger.error("Couldn't create symmetric validator for client " + client.getClientId(), e);
        }
    	
    	return null;
    	
    }

}
