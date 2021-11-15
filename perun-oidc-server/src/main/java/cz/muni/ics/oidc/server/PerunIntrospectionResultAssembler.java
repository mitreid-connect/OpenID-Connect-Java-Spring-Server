package cz.muni.ics.oidc.server;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.service.impl.DefaultIntrospectionResultAssembler;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.ScopeClaimTranslationService;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Assembler of result obtained from introspection endpoint.
 * Adds "iss" to identify issuer in response from Introspection endpoint to Resource Server.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class PerunIntrospectionResultAssembler extends DefaultIntrospectionResultAssembler {

	private final ConfigurationPropertiesBean configBean;
	private final ScopeClaimTranslationService translator;

	public PerunIntrospectionResultAssembler(ConfigurationPropertiesBean configBean,
											 ScopeClaimTranslationService translator)
	{
		this.configBean = configBean;
		this.translator = translator;
	}

	@Override
	public Map<String, Object> assembleFrom(OAuth2AccessTokenEntity accessToken, UserInfo userInfo,
											Set<String> resourceServerScopes)
	{
		log.info("adding claims at Introspection endpoint for client {}({}) and user {}({})",
				accessToken.getClient().getClientId(), accessToken.getClient().getClientName(), userInfo.getSub(),
				userInfo.getName());
		Map<String, Object> map = super.assembleFrom(accessToken, userInfo, resourceServerScopes);
		Set<String> accessTokenScopes = accessToken.getScope();
		Set<String> scopes = Sets.intersection(resourceServerScopes, accessTokenScopes);
		log.debug("resource server scopes: {}", resourceServerScopes);
		log.debug("access token scopes   : {}", accessTokenScopes);
		log.debug("common scopes         : {}", scopes);
		this.addDataToResponse(map, userInfo, scopes);
		return map;
	}

	private void addDataToResponse(Map<String, Object> map, UserInfo userInfo, Set<String> scopes) {
		log.debug("adding iss to introspection response {}", map);
		map.put("iss", configBean.getIssuer());
		log.debug("adding user claims");
		Set<String> authorizedClaims = translator.getClaimsForScopeSet(scopes);
		for (Map.Entry<String, JsonElement> claim : userInfo.toJson().entrySet()) {
			if (authorizedClaims.contains(claim.getKey()) && claim.getValue() != null && !claim.getValue().isJsonNull()) {
				log.debug("adding claim {} with value {}", claim.getKey(), claim.getValue());
				map.put(claim.getKey(), claim.getValue());
			}
		}
	}

}
