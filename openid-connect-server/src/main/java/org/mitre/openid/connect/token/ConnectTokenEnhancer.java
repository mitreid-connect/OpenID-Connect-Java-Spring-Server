package org.mitre.openid.connect.token;

import java.util.Date;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.model.IdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import com.google.common.base.Strings;

public class ConnectTokenEnhancer implements TokenEnhancer {

	@Autowired
	private ConfigurationPropertiesBean configBean;
	
	@Autowired
	private OAuth2TokenEntityService tokenServices;
	
	@Autowired
	private IdTokenGeneratorService idTokenService;
	
	@Autowired
	private JwtSigningAndValidationService jwtService;
	
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,	OAuth2Authentication authentication) {
		
		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) accessToken;
		
		String clientId = "";
		token.getJwt().getClaims().setAudience(clientId);
		
		token.getJwt().getClaims().setIssuer(configBean.getIssuer());

		token.getJwt().getClaims().setIssuedAt(new Date());
		// handle expiration
		token.getJwt().getClaims().setExpiration(token.getExpiration());
		
		jwtService.signJwt(token.getJwt());
		
		/**
		 * Authorization request scope MUST include "openid", but access token request 
		 * may or may not include the scope parameter. As long as the AuthorizationRequest 
		 * has the proper scope, we can consider this a valid OpenID Connect request.
		 */
		if (authentication.getAuthorizationRequest().getScope().contains("openid")) {

			String userId = authentication.getName();
		
			IdToken idToken = idTokenService.generateIdToken(userId, configBean.getIssuer());
			idToken.getClaims().setAudience(clientId);
			idToken.getClaims().setIssuedAt(new Date());
			idToken.getClaims().setIssuer(configBean.getIssuer());
			
			String nonce = authentication.getAuthorizationRequest().getParameters().get("nonce");
			if (!Strings.isNullOrEmpty(nonce)) {
				idToken.getClaims().setNonce(nonce);
			}
			// TODO: expiration? other fields?
			
			//Sign
			//TODO: check client to see if they have a preferred alg, attempt to use that
			
			jwtService.signJwt(idToken);
			
			token.setIdToken(idToken);
		}
		
		tokenServices.saveAccessToken(token);
		return token;
	}

}
