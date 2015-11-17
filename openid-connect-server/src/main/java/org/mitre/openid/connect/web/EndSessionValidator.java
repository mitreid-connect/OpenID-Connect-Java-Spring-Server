package org.mitre.openid.connect.web;

import static org.springframework.util.StringUtils.isEmpty;

import java.text.ParseException;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.SignedJWT;

@Component
public class EndSessionValidator {
	
	private static final Logger logger = LoggerFactory.getLogger(EndSessionValidator.class);
	
	@Autowired
	private OAuth2TokenEntityService tokenService;

	public boolean isValid(String idTokenHint, String postLogoutRedirectUri, AbstractAuthenticationToken auth) {
		if (idTokenHint == null || !isValidSyntax(idTokenHint) || isEmpty(postLogoutRedirectUri)) {
			return false;
		}
		OAuth2AccessTokenEntity accessToken = findAccessToken(idTokenHint);
		if (accessToken == null || 
				accessToken.getClient() == null) {
			return false;
		} else if (accessToken.getClient().getPostLogoutRedirectUris() == null ||
				!accessToken.getClient().getPostLogoutRedirectUris().contains(postLogoutRedirectUri)) {
			logger.info("Unregistered post_logout_redirect_uri: " + postLogoutRedirectUri);
			return false;
		} else if (accessToken.getAuthenticationHolder() == null ||
				accessToken.getAuthenticationHolder().getUserAuth() == null ||
				accessToken.getAuthenticationHolder().getUserAuth().getName() == null ||
				auth == null ||
				auth.getPrincipal() == null ||
				!accessToken.getAuthenticationHolder().getUserAuth().getName().equals(auth.getPrincipal())) {
			logger.info("Can't verify correct user for logout");
			return false;
		} else {
			return true;
		}
	}

	private OAuth2AccessTokenEntity findAccessToken(String idTokenHint) {
		OAuth2AccessTokenEntity accessToken = null;
		try {
			accessToken = tokenService.readAccessToken(idTokenHint);
		} catch (AuthenticationException e) {
			logger.info("Error reading id_token: " + idTokenHint);
		} catch (InvalidTokenException e) {
			logger.info("Error reading id_token: " + idTokenHint);
		}
		return accessToken;
	}

	private boolean isValidSyntax(String idTokenHint) {
		try {
			SignedJWT.parse(idTokenHint);
			return true;
		} catch (ParseException e) {
			logger.info("bad id_token: " + idTokenHint);
			return false;
		}
	}

	public void setTokenService(OAuth2TokenEntityService tokenService) {
		this.tokenService = tokenService;
	}

}
