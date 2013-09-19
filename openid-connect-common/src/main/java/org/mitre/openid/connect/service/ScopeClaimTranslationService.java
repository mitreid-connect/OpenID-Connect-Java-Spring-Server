package org.mitre.openid.connect.service;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;

/**
 * Service to map scopes to claims, and claims to Java field names
 * 
 * @author Amanda Anganes
 *
 */
public class ScopeClaimTranslationService {

		private ArrayListMultimap<String, String> scopesToClaims = ArrayListMultimap.create();
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
			
			scopesToClaims.put("address", "address.formatted");
			scopesToClaims.put("address", "address.street_address");
			scopesToClaims.put("address", "address.locality");
			scopesToClaims.put("address", "address.region");
			scopesToClaims.put("address", "address.postal_code");
			scopesToClaims.put("address", "address.country");
			
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
			
			//TODO: how to handle compound fields?
			claimsToFields.put("address.formatted", "");
			claimsToFields.put("address.street_address", "");
			claimsToFields.put("address.locality", "");
			claimsToFields.put("address.region", "");
			claimsToFields.put("address.postal_code", "");
			claimsToFields.put("address.country", "");
			
		}
		
		public List<String> getClaimsForScope(String scope) {
			return scopesToClaims.get(scope);
		}
	
		public String getFieldNameForClaim(String claim) {
			return claimsToFields.get(claim);
		}
	
}
