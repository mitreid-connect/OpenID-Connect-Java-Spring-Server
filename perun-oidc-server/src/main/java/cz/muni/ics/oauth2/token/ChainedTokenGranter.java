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
package cz.muni.ics.oauth2.token;

import com.google.common.collect.Sets;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.OAuth2TokenEntityService;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.stereotype.Component;

/**
 * @author jricher
 *
 */
@Component("chainedTokenGranter")
public class ChainedTokenGranter extends AbstractTokenGranter {

	public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant_type:redelegate";

	// keep down-cast versions so we can get to the right queries
	private final OAuth2TokenEntityService tokenServices;

	/**
	 * @param tokenServices
	 * @param clientDetailsService
	 * @param GRANT_TYPE
	 */
	@Autowired
	public ChainedTokenGranter(OAuth2TokenEntityService tokenServices, ClientDetailsEntityService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
		this.tokenServices = tokenServices;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getOAuth2Authentication(org.springframework.security.oauth2.provider.AuthorizationRequest)
	 */
	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) throws AuthenticationException, InvalidTokenException {
		// read and load up the existing token
		String incomingTokenValue = tokenRequest.getRequestParameters().get("token");
		OAuth2AccessTokenEntity incomingToken = tokenServices.readAccessToken(incomingTokenValue);

		// check for scoping in the request, can't up-scope with a chained request
		Set<String> approvedScopes = incomingToken.getScope();
		Set<String> requestedScopes = tokenRequest.getScope();

		if (requestedScopes == null) {
			requestedScopes = new HashSet<>();
		}

		// do a check on the requested scopes -- if they exactly match the client scopes, they were probably shadowed by the token granter
		if (client.getScope().equals(requestedScopes)) {
			requestedScopes = new HashSet<>();
		}

		// if our scopes are a valid subset of what's allowed, we can continue
		if (approvedScopes.containsAll(requestedScopes)) {

			if (requestedScopes.isEmpty()) {
				// if there are no scopes, inherit the original scopes from the token
				tokenRequest.setScope(approvedScopes);
			} else {
				// if scopes were asked for, give only the subset of scopes requested
				// this allows safe downscoping
				tokenRequest.setScope(Sets.intersection(requestedScopes, approvedScopes));
			}

			// NOTE: don't revoke the existing access token

			// create a new access token
			OAuth2Authentication authentication = new OAuth2Authentication(getRequestFactory().createOAuth2Request(client, tokenRequest), incomingToken.getAuthenticationHolder().getAuthentication().getUserAuthentication());

			return authentication;

		} else {
			throw new InvalidScopeException("Invalid scope requested in chained request", approvedScopes);
		}

	}

}
