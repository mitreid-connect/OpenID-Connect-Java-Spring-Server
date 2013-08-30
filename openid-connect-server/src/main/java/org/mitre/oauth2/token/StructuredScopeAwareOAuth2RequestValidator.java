/**
 * 
 */
package org.mitre.oauth2.token;

import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;

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
	@Override
	public void validateScope(Map<String, String> parameters, Set<String> clientScopes) throws InvalidScopeException {
		if (parameters.containsKey("scope")) {
			if (clientScopes != null && !clientScopes.isEmpty()) {
				Set<String> requestedScopes = OAuth2Utils.parseParameterList(parameters.get("scope"));
				if (!scopeService.scopesMatch(clientScopes, requestedScopes)) {
					throw new InvalidScopeException("Invalid scope", clientScopes);
				}
			}
		}
	}

}
