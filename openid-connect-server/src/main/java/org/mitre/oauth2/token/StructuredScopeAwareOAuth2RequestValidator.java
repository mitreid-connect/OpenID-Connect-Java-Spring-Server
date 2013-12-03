/**
 * 
 */
package org.mitre.oauth2.token;

import java.util.Set;

import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.TokenRequest;

/**
 * 
 * Validates the scopes on a request by comparing them against a client's
 * allowed scopes, but allow structured scopes to function.
 * 
 * @author jricher
 * 
 */
public class StructuredScopeAwareOAuth2RequestValidator implements OAuth2RequestValidator {

	@Autowired
	private SystemScopeService scopeService;

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.OAuth2RequestValidator#validateScope(java.util.Map, java.util.Set)
	 */
	private void validateScope(Set<String> requestedScopes, Set<String> clientScopes) throws InvalidScopeException {
		if (requestedScopes != null && !requestedScopes.isEmpty()) {
			if (clientScopes != null && !clientScopes.isEmpty()) {
				if (!scopeService.scopesMatch(clientScopes, requestedScopes)) {
					throw new InvalidScopeException("Invalid scope", clientScopes);
				}
			}
		}
	}

	@Override
	public void validateScope(AuthorizationRequest authorizationRequest, ClientDetails client) throws InvalidScopeException {
		validateScope(authorizationRequest.getScope(), client.getScope());
	}

	@Override
	public void validateScope(TokenRequest tokenRequest, ClientDetails client) throws InvalidScopeException {
		validateScope(tokenRequest.getScope(), client.getScope());
	}

}
