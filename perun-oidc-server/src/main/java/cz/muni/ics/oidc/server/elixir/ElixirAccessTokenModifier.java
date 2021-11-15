package cz.muni.ics.oidc.server.elixir;

import com.nimbusds.jwt.JWTClaimsSet;
import cz.muni.ics.oidc.server.PerunAccessTokenEnhancer;
import java.util.Collections;
import java.util.Set;
import cz.muni.ics.openid.connect.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * Implements changes required by GA4GH specification followed by the ELIXIR AAI.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
@Slf4j
public class ElixirAccessTokenModifier implements PerunAccessTokenEnhancer.AccessTokenClaimsModifier {

	public ElixirAccessTokenModifier() {
	}

	@Override
	public void modifyClaims(String sub, JWTClaimsSet.Builder builder, OAuth2AccessToken accessToken, OAuth2Authentication authentication, UserInfo userInfo) {
		Set<String> scopes = accessToken.getScope();
		//GA4GH
		if (scopes.contains(GA4GHClaimSource.GA4GH_SCOPE)) {
			log.debug("adding claims required by GA4GH to access token");
			builder.audience(Collections.singletonList(authentication.getOAuth2Request().getClientId()));
		}
	}

}
