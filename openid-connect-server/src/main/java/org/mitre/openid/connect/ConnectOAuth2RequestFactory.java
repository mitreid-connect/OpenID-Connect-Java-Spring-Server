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

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minidev.json.JSONObject;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetSigningAndValidationServiceCacheService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.nimbusds.jose.util.JSONObjectUtils;
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
			SignedJWT jwsObject = SignedJWT.parse(jwtString);
			JSONObject claims = jwsObject.getPayload().toJSONObject();

			// TODO: check parameter consistency, move keys to constants

			String clientId = JSONObjectUtils.getString(claims, "client_id");
			if (clientId != null) {
				parameters.put("client_id", clientId);
			}

			ClientDetailsEntity client = clientDetailsService.loadClientByClientId(clientId);

			if (client.getJwksUri() == null) {
				throw new InvalidClientException("Client must have a JWKS URI registered to use request objects.");
			}

			// check JWT signature
			JwtSigningAndValidationService validator = validators.get(client.getJwksUri());
			if (validator == null) {
				throw new InvalidClientException("Unable to create signature validator for client's JWKS URI: " + client.getJwksUri());
			}

			if (!validator.validateSignature(jwsObject)) {
				throw new AuthenticationServiceException("Signature did not validate for presented JWT request object.");
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

			String responseTypes = JSONObjectUtils.getString(claims, "response_type");
			if (responseTypes != null) {
				parameters.put("response_type", responseTypes);
			}

			if (claims.get("redirect_uri") != null) {
				if (inputParams.containsKey("redirect_uri") == false) {
					parameters.put("redirect_uri", JSONObjectUtils.getString(claims, "redirect_uri"));
				}
			}

			String state = JSONObjectUtils.getString(claims, "state");
			if(state != null) {
				if (inputParams.containsKey("state") == false) {
					parameters.put("state", state);
				}
			}

			String nonce = JSONObjectUtils.getString(claims, "nonce");
			if(nonce != null) {
				if (inputParams.containsKey("nonce") == false) {
					parameters.put("nonce", nonce);
				}
			}

			String display = JSONObjectUtils.getString(claims, "display");
			if (display != null) {
				if (inputParams.containsKey("display") == false) {
					parameters.put("display", display);
				}
			}

			String prompt = JSONObjectUtils.getString(claims, "prompt");
			if (prompt != null) {
				if (inputParams.containsKey("prompt") == false) {
					parameters.put("prompt", prompt);
				}
			}

			String scope = JSONObjectUtils.getString(claims, "scope");
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

}
