package cz.muni.ics.oidc.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.openid.connect.service.ScopeClaimTranslationService;
import cz.muni.ics.openid.connect.service.UserInfoService;
import cz.muni.ics.openid.connect.service.impl.DefaultOIDCTokenService;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Request;

/**
 * Modifies ID Token.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class PerunOIDCTokenService extends DefaultOIDCTokenService {

	public static final String SESSION_PARAM_ACR = "acr";

	private final UserInfoService userInfoService;
	private final ScopeClaimTranslationService translator;
	private final PerunOidcConfig perunOidcConfig;

	private final Gson gson = new Gson();

	@Autowired
	public PerunOIDCTokenService(UserInfoService userInfoService,
								 ScopeClaimTranslationService translator,
								 PerunOidcConfig perunOidcConfig)
	{
		this.userInfoService = userInfoService;
		this.translator = translator;
		this.perunOidcConfig = perunOidcConfig;
	}

	@Override
	protected void addCustomIdTokenClaims(JWTClaimsSet.Builder idClaims,
										  ClientDetailsEntity client,
										  OAuth2Request request,
										  String sub,
										  OAuth2AccessTokenEntity accessToken)
	{
		log.debug("modifying ID token");
		String userId = accessToken.getAuthenticationHolder().getAuthentication().getName();
		String clientId = request.getClientId();
		log.debug("userId={},clientId={}", userId, clientId);

		Set<String> scopes = accessToken.getScope();
		Set<String> authorizedClaims = translator.getClaimsForScopeSet(scopes);
		Set<String> idTokenClaims = translator.getClaimsForScopeSet(perunOidcConfig.getIdTokenScopes());

		JsonObject userInfoJson = userInfoService.get(userId, clientId, accessToken.getScope(), accessToken.getAuthenticationHolder().getUserAuth())
				.toJson();
		for (Map.Entry<String, JsonElement> claim : userInfoJson.entrySet()) {
			String claimKey = claim.getKey();
			JsonElement claimValue = claim.getValue();
			if (claimValue != null && !claimValue.isJsonNull() && authorizedClaims.contains(claimKey)
					&& idTokenClaims.contains(claimKey))
			{
				log.debug("adding to ID token claim {} with value {}", claimKey, claimValue);
				idClaims.claim(claimKey, gson2jsonsmart(claimValue));
			}
		}

		if (accessToken.getAuthenticationHolder() != null
			&& accessToken.getAuthenticationHolder().getUserAuth() != null)
		{
			String acr = accessToken.getAuthenticationHolder().getUserAuth().getAcr();
			if (acr != null) {
				log.debug("adding to ID token claim acr with value {}", acr);
				idClaims.claim("acr", acr);
			}
		}
	}

	/**
	 * Converts claim values from com.google.gson.JsonElement to net.minidev.json.JSONObject or primitive value
	 *
	 * @param jsonElement Gson representation
	 * @return json-smart representation
	 */
	private Object gson2jsonsmart(JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive p = jsonElement.getAsJsonPrimitive();
			if (p.isString()) {
				return p.getAsString();
			} else if (p.isBoolean()) {
				return p.getAsBoolean();
			} else if (p.isNumber()) {
				return p.getAsNumber();
			} else {
				log.warn("unknown JsonPrimitive {}", p);
				return null;
			}
		} else if (jsonElement.isJsonObject()) {
			try {
				return JSONObjectUtils.parse(gson.toJson(jsonElement));
			} catch (ParseException e) {
				log.error("cannot convert Gson->smart-json.JSONObject", e);
				return null;
			}
		} else if (jsonElement.isJsonArray()) {
			JSONArray jsonArray = new JSONArray();
			jsonElement.getAsJsonArray().forEach(je -> jsonArray.appendElement(gson2jsonsmart(je)));
			return jsonArray;
		} else {
			return null;
		}
	}
}
