package org.bbplus;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Service;

@Service
public class PreregistrationTokenService implements ResourceServerTokenServices {

	@Override
	public OAuth2Authentication loadAuthentication(String accessToken)
			throws AuthenticationException {
		
		// Return an empty (but valid) authorizationRequest,
		// which OAuth2AuthenticationManager will enhance by adding details.
		// (Details include the accessToken string, which is all we real need.)
		AuthorizationRequest r = new AuthorizationRequest(null,null,null,null,null,null,false,"","",null);
		PreregistrationToken ret = new PreregistrationToken(r.createOAuth2Request(), null);
		return ret;
	}

	@Override
	public OAuth2AccessToken readAccessToken(String accessToken) {
		return null;
	}

}
