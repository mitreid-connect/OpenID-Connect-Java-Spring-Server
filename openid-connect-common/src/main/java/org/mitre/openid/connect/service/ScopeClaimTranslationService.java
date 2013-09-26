package org.mitre.openid.connect.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

/**
 * Service to map scopes to claims, and claims to Java field names
 * 
 * @author Amanda Anganes
 *
 */
public class ScopeClaimTranslationService {

		private SetMultimap<String, String> scopesToClaims = HashMultimap.create();
		private Map<String, String> claimsToFields = Maps.newHashMap();
		
		/**
		 * Default constructor; initializes scopesToClaims map
		 */
		public ScopeClaimTranslationService() {
			
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
		
		public Set<String> getClaimsForScope(String scope) {
			if (scopesToClaims.containsKey(scope)) {
				return scopesToClaims.get(scope);
			} else {
				return new HashSet<String>();
			}
		}
		
		public Set<String> getClaimsForScopeSet(Set<String> scopes) {
			Set<String> result = new HashSet<String>();
			for (String scope : scopes) {
				result.addAll(getClaimsForScope(scope));
			}
			return result;
		}
	
		public String getFieldNameForClaim(String claim) {
			return claimsToFields.get(claim);
		}
	
}
