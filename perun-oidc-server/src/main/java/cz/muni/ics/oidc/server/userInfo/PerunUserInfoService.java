package cz.muni.ics.oidc.server.userInfo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oauth2.model.SavedUserAuthentication;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oidc.exceptions.ConfigurationException;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.claims.ClaimModifier;
import cz.muni.ics.oidc.server.claims.PerunCustomClaimDefinition;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.userInfo.mappings.AddressMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.EmailMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.OpenidMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.PhoneMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.ProfileMappings;
import cz.muni.ics.oidc.server.userInfo.modifiers.UserInfoModifierContext;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.UserInfoService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.util.StringUtils;

/**
 * Service called from UserInfoEndpoint and other places to get UserInfo.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class PerunUserInfoService implements UserInfoService {

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private JWTSigningAndValidationService jwtService;

	@Autowired
	private PerunOidcConfig perunOidcConfig;

	@Autowired
	private OpenidMappings openidMappings;

	@Autowired
	private ProfileMappings profileMappings;

	@Autowired
	private EmailMappings emailMappings;

	@Autowired
	private AddressMappings addressMappings;

	@Autowired
	private PhoneMappings phoneMappings;

	@Autowired
	private PerunAdapter perunAdapter;

	private LoadingCache<UserInfoCacheKey, UserInfo> cache;

	private Properties properties;

	private Set<String> customClaimNames;

	private List<PerunCustomClaimDefinition> customClaims = new ArrayList<>();

	private UserInfoModifierContext userInfoModifierContext;

	private List<String> forceRegenerateUserinfoCustomClaims = new ArrayList<>();

	private List<String> forceRegenerateUserinfoStandardClaims = new ArrayList<>();

	// == setters and getters ==

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setCustomClaimNames(Set<String> customClaimNames) {
		this.customClaimNames = customClaimNames;
	}

	public void setForceRegenerateUserinfoCustomClaims(String[] claims) {
		this.forceRegenerateUserinfoCustomClaims = Arrays.asList(claims);
	}

	public void setForceRegenerateUserinfoStandardClaims(String[] claims) {
		this.forceRegenerateUserinfoStandardClaims = Arrays.asList(claims);
	}

	public List<PerunCustomClaimDefinition> getCustomClaims() {
		return customClaims;
	}

	public void setPerunAdapter(PerunAdapter perunAdapter) {
		this.perunAdapter = perunAdapter;
	}

	// == init ==

	@PostConstruct
	public void postInit() throws ConfigurationException {
		//custom claims
		this.customClaims = UserInfoUtils.loadCustomClaims(customClaimNames, properties, perunOidcConfig, jwtService);

		this.userInfoModifierContext = new UserInfoModifierContext(properties, perunAdapter);

		log.debug("trying to load modifier for attribute.openid.sub");
		List<ClaimModifier> subModifiers = UserInfoUtils.loadClaimValueModifiers(
				properties, "sub", "attribute.openid.sub");

		PerunUserInfoCacheLoader cacheLoader = PerunUserInfoCacheLoader.builder()
				.openidMappings(openidMappings)
				.profileMappings(profileMappings)
				.emailMappings(emailMappings)
				.phoneMappings(phoneMappings)
				.addressMappings(addressMappings)
				.customClaims(customClaims)
				.fillAttributes(perunOidcConfig.isFillMissingUserAttrs())
				.perunAdapter(perunAdapter)
				.subModifiers(subModifiers)
				.build();

		this.cache = CacheBuilder.newBuilder()
				.maximumSize(100)
				.expireAfterAccess(java.time.Duration.ofSeconds(60))
				.expireAfterWrite(java.time.Duration.ofSeconds(300))
				.build(cacheLoader);
	}

	// == public methods ==

	@Override
	public UserInfo get(String username, String clientId, Set<String> scope, SavedUserAuthentication userAuthentication) {
		return get(username, clientId, scope, userAuthentication.getAuthenticationDetails());
	}

	@Override
	public UserInfo get(String username, String clientId, Set<String> scope, SAMLCredential samlCredential) {
		return get(username, clientId, scope, new SamlAuthenticationDetails(samlCredential));
	}

	@Override
	public UserInfo get(String username, String clientId, Set<String> scope) {
		return get(username, clientId, scope, new SamlAuthenticationDetails());
	}

	// == private methods ==

	private UserInfo get(String username, String clientId, Set<String> scope, SamlAuthenticationDetails details) {
		if (!StringUtils.hasText(clientId)) {
			log.warn("No client_id provided, cannot get userinfo");
			return null;
		}
		ClientDetailsEntity client = null;
		if (StringUtils.hasText(clientId)) {
			client = clientService.loadClientByClientId(clientId);
			if (client == null) {
				log.warn("Did not find client with id '{}', cannot get userinfo", clientId);
				return null;
			}
		}

		PerunUserInfo userInfo;
		try {
			UserInfoCacheKey cacheKey = new UserInfoCacheKey(username, client, details, scope);
			userInfo = (PerunUserInfo) cache.get(cacheKey);
			if (!checkStandardClaims(userInfo) || !checkCustomClaims(userInfo)) {
				log.info("Some required claim is null, regenerate userInfo");
				cache.invalidate(cacheKey);
				userInfo = (PerunUserInfo) cache.get(cacheKey);
			}
			userInfo = userInfoModifierContext.modify(userInfo, clientId);
		} catch (ExecutionException e) {
			log.error("cannot get user from cache", e);
			return null;
		}

		return userInfo;
	}

	private boolean checkStandardClaims(PerunUserInfo userInfo) {
		for (String claim: forceRegenerateUserinfoStandardClaims) {
			switch (claim.toLowerCase()) {
				case "sub": {
					if (userInfo.getSub() == null) {
						return false;
					}
				} break;
				case "preferred_username": {
					if (userInfo.getPreferredUsername() == null) {
						return false;
					}
				} break;
				case "given_name": {
					if (userInfo.getGivenName() == null) {
						return false;
					}
				} break;
				case "family_name": {
					if (userInfo.getFamilyName() == null) {
						return false;
					}
				} break;
				case "middle_name": {
					if (userInfo.getMiddleName() == null) {
						return false;
					}
				} break;
				case "name": {
					if (userInfo.getName() == null) {
						return false;
					}
				} break;
				case "email": {
					if (userInfo.getEmail() == null) {
						return false;
					}
				} break;
				case "address_formatted": {
					if (userInfo.getAddress() == null
							|| userInfo.getAddress().getFormatted() == null)
					{
						return false;
					}
				} break;
				case "phone": {
					if (userInfo.getPhoneNumber() == null) {
						return false;
					}
				} break;
				case "zoneinfo": {
					if (userInfo.getZoneinfo() == null) {
						return false;
					}
				} break;
				case "locale": {
					if (userInfo.getLocale() == null) {
						return false;
					}
				} break;
			}
		}

		log.debug("All required standard claims are OK");
		return true;
	}

	private boolean checkCustomClaims(PerunUserInfo userInfo) {
		for (String claim: forceRegenerateUserinfoCustomClaims) {
			if (userInfo.getCustomClaims().get(claim) == null ||
					userInfo.getCustomClaims().get(claim).isNull()) {
				return false;
			}
		}

		log.debug("All required custom claims are OK");
		return true;
	}

}
