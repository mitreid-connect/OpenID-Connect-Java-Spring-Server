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
 *******************************************************************************/
package org.mitre.openid.connect.service.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mitre.openid.connect.service.ScopeClaimTranslationService;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

/**
 * Service to map scopes to claims, and claims to Java field names
 * 
 * @author Amanda Anganes
 *
 */
@Service("scopeClaimTranslator")
public class DefaultScopeClaimTranslationService implements ScopeClaimTranslationService {

	private SetMultimap<String, String> scopesToClaims = HashMultimap.create();
	private Map<String, String> claimsToFields = Maps.newHashMap();

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
		scopesToClaims.put("profile", "zone_info");
		scopesToClaims.put("profile", "locale");
		scopesToClaims.put("profile", "updated_time");
		scopesToClaims.put("profile", "birthdate");

		scopesToClaims.put("email", "email");
		scopesToClaims.put("email", "email_verified");

		scopesToClaims.put("phone", "phone_number");
		scopesToClaims.put("phone", "phone_number_verified");

		scopesToClaims.put("address", "address");

		claimsToFields.put("sub", "sub");

		claimsToFields.put("name", "name");
		claimsToFields.put("preferred_username", "preferredUsername");
		claimsToFields.put("given_name", "givenName");
		claimsToFields.put("family_name", "familyName");
		claimsToFields.put("middle_name", "middleName");
		claimsToFields.put("nickname", "nickname");
		claimsToFields.put("profile", "profile");
		claimsToFields.put("picture", "picture");
		claimsToFields.put("website", "website");
		claimsToFields.put("gender", "gender");
		claimsToFields.put("zone_info", "zoneinfo");
		claimsToFields.put("locale", "locale");
		claimsToFields.put("updated_time", "updatedTime");
		claimsToFields.put("birthdate", "birthdate");

		claimsToFields.put("email", "email");
		claimsToFields.put("email_verified", "emailVerified");

		claimsToFields.put("phone_number", "phoneNumber");
		claimsToFields.put("phone_number_verified", "phoneNumberVerified");

		claimsToFields.put("address", "address");

	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.ScopeClaimTranslationService#getClaimsForScope(java.lang.String)
	 */
	@Override
	public Set<String> getClaimsForScope(String scope) {
		if (scopesToClaims.containsKey(scope)) {
			return scopesToClaims.get(scope);
		} else {
			return new HashSet<String>();
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.ScopeClaimTranslationService#getClaimsForScopeSet(java.util.Set)
	 */
	@Override
	public Set<String> getClaimsForScopeSet(Set<String> scopes) {
		Set<String> result = new HashSet<String>();
		for (String scope : scopes) {
			result.addAll(getClaimsForScope(scope));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.ScopeClaimTranslationService#getFieldNameForClaim(java.lang.String)
	 */
	@Override
	public String getFieldNameForClaim(String claim) {
		return claimsToFields.get(claim);
	}

}
