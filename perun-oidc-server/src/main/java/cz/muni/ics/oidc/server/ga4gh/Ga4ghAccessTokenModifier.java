package cz.muni.ics.oidc.server.ga4gh;

import com.nimbusds.jwt.JWTClaimsSet;
import cz.muni.ics.oidc.server.PerunAccessTokenEnhancer;
import cz.muni.ics.openid.connect.model.UserInfo;
import java.util.Collections;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * Implements changes required by GA4GH specification.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
@Slf4j
@NoArgsConstructor
public class Ga4ghAccessTokenModifier implements PerunAccessTokenEnhancer.AccessTokenClaimsModifier {

	@Override
	public void modifyClaims(String sub,
							 JWTClaimsSet.Builder builder,
							 OAuth2AccessToken accessToken,
							 OAuth2Authentication authentication,
							 UserInfo userInfo)
	{
		Set<String> scopes = accessToken.getScope();
		//GA4GH
		if (scopes.contains(ElixirGa4ghClaimSource.GA4GH_SCOPE)) {
			log.debug("Adding claims required by GA4GH to access token");
			builder.audience(Collections.singletonList(authentication.getOAuth2Request().getClientId()));
		}
	}

}
