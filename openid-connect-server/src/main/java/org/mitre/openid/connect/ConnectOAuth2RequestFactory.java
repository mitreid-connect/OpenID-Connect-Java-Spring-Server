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
import java.util.Map;
import java.util.Set;

import org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.DefaultJwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWEObject.State;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.Use;
import com.nimbusds.jose.util.Base64URL;
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
	public OAuth2Request createOAuth2Request(AuthorizationRequest request) {
		return new OAuth2Request(request.getRequestParameters(), request.getClientId(), request.getAuthorities(), 
				request.isApproved(), request.getScope(), request.getResourceIds(), request.getRedirectUri(), request.getExtensions());
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
		
		if (inputParams.containsKey("request")) {
			request.getExtensions().put("request", inputParams.get("request"));
			processRequestObject(inputParams.get("request"), request);
		}
		

		if ((request.getScope() == null || request.getScope().isEmpty())) {
			if (request.getClientId() != null) {
				ClientDetails client = clientDetailsService.loadClientByClientId(request.getClientId());
				Set<String> clientScopes = client.getScope();
				request.setScope(clientScopes);
			}
		}

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

			// TODO: check parameter consistency, move keys to constants
			
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
			 * Claims precedence order logic:
			 * 
			 * if (in Claims):
			 * 		if (in params):
			 * 			if (equal):
			 * 				OK
			 * 			else (not equal):
			 * 				error
			 * 		else (not in params):
			 * 			add to params
			 * else (not in claims):
			 * 		we don't care
			 */

			// now that we've got the JWT, and it's been parsed, validated, and/or decrypted, we can process the claims
			
			ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
			
			Set<String> responseTypes = OAuth2Utils.parseParameterList(claims.getStringClaim("response_type"));
			if (responseTypes != null && !responseTypes.isEmpty()) {
				if (request.getResponseTypes() == null || request.getResponseTypes().isEmpty()) {
					// if it's null or empty, we fill in the value with what we were passed
					request.setResponseTypes(responseTypes);
				} else if (!request.getResponseTypes().equals(responseTypes)) {
					// FIXME: throw an error					
				}
			}

			String redirectUri = claims.getStringClaim("redirect_uri"); 
			if (redirectUri != null) {
				if (request.getRedirectUri() == null) {
					request.setRedirectUri(redirectUri);
				} else if (!request.getRedirectUri().equals(redirectUri)) {
					// FIXME: throw an error
				}
			}

			String state = claims.getStringClaim("state");
			if(state != null) {
				if (request.getState() == null) {
					request.setState(state);
				} else if (!request.getState().equals(state)) {
					// FIXME: throw an error
				}
			}

			String nonce = claims.getStringClaim("nonce");
			if(nonce != null) {
				if (request.getExtensions().get("nonce") == null) {
					request.getExtensions().put("nonce", nonce);
				} else if (!request.getExtensions().get("nonce").equals(nonce)) {
					// FIXME: throw an error
				}
			}

			String display = claims.getStringClaim("display");
			if (display != null) {
				if (request.getExtensions().get("display") == null) {
					request.getExtensions().put("display", display);
				} else if (!request.getExtensions().get("display").equals(display)) {
					// FIXME: throw an error
				}
			}

			String prompt = claims.getStringClaim("prompt");
			if (prompt != null) {
				if (request.getExtensions().get("prompt") == null) {
					request.getExtensions().put("prompt", prompt);
				} else if (!request.getExtensions().get("prompt").equals(prompt)) {
					// FIXME: throw an error
				}
			}
			
			Set<String> scope = OAuth2Utils.parseParameterList(claims.getStringClaim("scope"));
			if (scope != null && !scope.isEmpty()) {
				if (request.getScope() == null || request.getScope().isEmpty()) {
					request.setScope(scope);
				} else if (!request.getScope().equals(scope)) {
					// FIXME: throw an error
				}
			}
			
			JsonObject claimRequest = parseClaimRequest(claims.getStringClaim("claims"));
			if (claimRequest != null) {
				if (request.getExtensions().get("claims") == null) {
					// we save the string because the object might not serialize
					request.getExtensions().put("claims", claimRequest.toString());
				} else if (parseClaimRequest(request.getExtensions().get("claims").toString()).equals(claimRequest)) {
					// FIXME: throw an error
				}
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
