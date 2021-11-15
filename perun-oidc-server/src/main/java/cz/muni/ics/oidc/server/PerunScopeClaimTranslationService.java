package cz.muni.ics.oidc.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import cz.muni.ics.oidc.server.claims.PerunCustomClaimDefinition;
import cz.muni.ics.oidc.server.userInfo.PerunUserInfoService;
import cz.muni.ics.openid.connect.service.ScopeClaimTranslationService;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Translates scopes to claims. A single scope can provide access to multiple claims.
 * Set this as spring bean named "scopeClaimTranslator". This code is copied from class
 * cz.muni.ics.openid.connect.service.impl.DefaultScopeClaimTranslationService
 * which for some reason is not accessible in this project, and extended.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class PerunScopeClaimTranslationService implements ScopeClaimTranslationService {

	public static final String OPENID = "openid";
	public static final String PROFILE = "profile";
	public static final String EMAIL = "email";
	public static final String PHONE = "phone";
	public static final String ADDRESS = "address";

	private final SetMultimap<String, String> scopesToClaims = HashMultimap.create();

	public void setPerunUserInfoService(PerunUserInfoService perunUserInfoService) {
		for(PerunCustomClaimDefinition pccd : perunUserInfoService.getCustomClaims()) {
			log.info("adding custom claim \"{}\" in scope \"{}\" ",pccd.getClaim(),pccd.getScope());
			scopesToClaims.put(pccd.getScope(),pccd.getClaim());
		}
	}

	/**
	 * Default constructor; initializes scopesToClaims map
	 */
	public PerunScopeClaimTranslationService() {
		scopesToClaims.put(OPENID, "sub");

		scopesToClaims.put(PROFILE, "name");
		scopesToClaims.put(PROFILE, "preferred_username");
		scopesToClaims.put(PROFILE, "given_name");
		scopesToClaims.put(PROFILE, "family_name");
		scopesToClaims.put(PROFILE, "middle_name");
		scopesToClaims.put(PROFILE, "nickname");
		scopesToClaims.put(PROFILE, "profile");
		scopesToClaims.put(PROFILE, "picture");
		scopesToClaims.put(PROFILE, "website");
		scopesToClaims.put(PROFILE, "gender");
		scopesToClaims.put(PROFILE, "zoneinfo");
		scopesToClaims.put(PROFILE, "locale");
		scopesToClaims.put(PROFILE, "updated_at");
		scopesToClaims.put(PROFILE, "birthdate");

		scopesToClaims.put(EMAIL, "email");
		scopesToClaims.put(EMAIL, "email_verified");

		scopesToClaims.put(PHONE, "phone_number");
		scopesToClaims.put(PHONE, "phone_number_verified");

		scopesToClaims.put(ADDRESS, "address");
	}

	@Override
	public Set<String> getClaimsForScope(String scope) {
		if (scopesToClaims.containsKey(scope)) {
			return scopesToClaims.get(scope);
		} else {
			return new HashSet<>();
		}
	}

	@Override
	public Set<String> getClaimsForScopeSet(Set<String> scopes) {
		Set<String> result = new HashSet<>();
		for (String scope : scopes) {
			result.addAll(getClaimsForScope(scope));
		}
		return result;
	}

}
