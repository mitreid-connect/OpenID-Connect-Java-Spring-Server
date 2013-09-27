/**
 * 
 */
package org.mitre.openid.connect.service;

import java.util.Set;

/**
 * @author jricher
 *
 */
public interface ScopeClaimTranslationService {

	public Set<String> getClaimsForScope(String scope);

	public Set<String> getClaimsForScopeSet(Set<String> scopes);

	public String getFieldNameForClaim(String claim);

}