/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
/**
 *
 */
package org.mitre.oauth2.token;

import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.TokenRequest;

import static org.mitre.openid.connect.request.ConnectRequestParameters.CODE_CHALLENGE_METHOD;


/**
 *
 * Validates the scopes on a request by comparing them against a client's
 * allowed scopes, but allow custom scopes to function through the system scopes
 *
 * @author jricher
 *
 */
public class ScopeServiceAwareOAuth2RequestValidator implements OAuth2RequestValidator {

	@Autowired
	private SystemScopeService scopeService;

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.OAuth2RequestValidator#validateScope(java.util.Map, java.util.Set)
	 */
	private void validateScope(Set<String> requestedScopes, Set<String> clientScopes) throws InvalidScopeException {
		if (requestedScopes != null && !requestedScopes.isEmpty()) {
			if (clientScopes != null && !clientScopes.isEmpty()) {
				if (!scopeService.scopesMatch(clientScopes, requestedScopes)) {
					throw new InvalidScopeException("Invalid scope; requested:" + requestedScopes, clientScopes);
				}
			}
		}
	}

	private void validateCodeChallengeMethod(AuthorizationRequest request, ClientDetails client) {
		ClientDetailsEntity clientEntity = (ClientDetailsEntity)client;
		if (clientEntity.getCodeChallengeMethod() != null) {
			// We're only basing this off the client, and not some sort of server wide "Enforce PKCE with method X"
			String clientCodeChallengeMethod = clientEntity.getCodeChallengeMethod().getName();

			if(request.getExtensions().get(CODE_CHALLENGE_METHOD) == null ||
				!request.getExtensions().get(CODE_CHALLENGE_METHOD).toString().equals(clientCodeChallengeMethod)) {
				throw OAuth2Exception.create(OAuth2Exception.INVALID_REQUEST, "Request code_challenge_method: '"
					+ request.getExtensions().get(CODE_CHALLENGE_METHOD).toString()
					+ "' does not match client settings");
			}
		}
	}

	@Override
	public void validateScope(AuthorizationRequest authorizationRequest, ClientDetails client) throws InvalidScopeException {
		validateScope(authorizationRequest.getScope(), client.getScope());
		// We're going to validate the code challenge method. This method is part of the SecOAuth defined interface,
		//    which should be more generic to validate more than just the scopes.
		validateCodeChallengeMethod(authorizationRequest, client);
	}

	@Override
	public void validateScope(TokenRequest tokenRequest, ClientDetails client) throws InvalidScopeException {
		validateScope(tokenRequest.getScope(), client.getScope());
	}

}
