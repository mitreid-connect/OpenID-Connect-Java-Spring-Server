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
package cz.muni.ics.openid.connect.service.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import cz.muni.ics.openid.connect.service.ScopeClaimTranslationService;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Service to map scopes to claims, and claims to Java field names
 *
 * @author Amanda Anganes
 *
 */
@Service("scopeClaimTranslator")
public class DefaultScopeClaimTranslationService implements ScopeClaimTranslationService {

	private SetMultimap<String, String> scopesToClaims = HashMultimap.create();

	/**
	 * Default constructor; initializes scopesToClaims map
	 */
	public DefaultScopeClaimTranslationService() {
		scopesToClaims.put("openid", "sub");

		scopesToClaims.put("profile", "name");
		scopesToClaims.put("profile", "preferred_username");
		scopesToClaims.put("profile", "given_name");
		scopesToClaims.put("profile", "family_name");
		scopesToClaims.put("profile", "middle_name");
		scopesToClaims.put("profile", "nickname");
		scopesToClaims.put("profile", "profile");
		scopesToClaims.put("profile", "picture");
		scopesToClaims.put("profile", "website");
		scopesToClaims.put("profile", "gender");
		scopesToClaims.put("profile", "zoneinfo");
		scopesToClaims.put("profile", "locale");
		scopesToClaims.put("profile", "updated_at");
		scopesToClaims.put("profile", "birthdate");

		scopesToClaims.put("email", "email");
		scopesToClaims.put("email", "email_verified");

		scopesToClaims.put("phone", "phone_number");
		scopesToClaims.put("phone", "phone_number_verified");

		scopesToClaims.put("address", "address");
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.ScopeClaimTranslationService#getClaimsForScope(java.lang.String)
	 */
	@Override
	public Set<String> getClaimsForScope(String scope) {
		if (scopesToClaims.containsKey(scope)) {
			return scopesToClaims.get(scope);
		} else {
			return new HashSet<>();
		}
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.ScopeClaimTranslationService#getClaimsForScopeSet(java.util.Set)
	 */
	@Override
	public Set<String> getClaimsForScopeSet(Set<String> scopes) {
		Set<String> result = new HashSet<>();
		for (String scope : scopes) {
			result.addAll(getClaimsForScope(scope));
		}
		return result;
	}

}
