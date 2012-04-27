package org.mitre.oauth2.repository;

import java.util.List;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface OAuth2TokenRepository {

	public OAuth2AccessTokenEntity saveAccessToken(OAuth2AccessTokenEntity token);

	public OAuth2RefreshTokenEntity getRefreshTokenByValue(String refreshTokenValue);

	public void clearAccessTokensForRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	public void removeRefreshToken(OAuth2RefreshTokenEntity refreshToken);
	
	public OAuth2RefreshTokenEntity saveRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	public OAuth2AccessTokenEntity getAccessTokenByValue(String accessTokenValue);

	public void removeAccessToken(OAuth2AccessTokenEntity accessToken);

	public void clearTokensForClient(ClientDetailsEntity client);

	public List<OAuth2AccessTokenEntity> getAccessTokensForClient(ClientDetailsEntity client);

	public List<OAuth2RefreshTokenEntity> getRefreshTokensForClient(ClientDetailsEntity client);

	public List<OAuth2AccessTokenEntity> getExpiredAccessTokens();

	public List<OAuth2RefreshTokenEntity> getExpiredRefreshTokens();

	public OAuth2AccessTokenEntity getByAuthentication(OAuth2Authentication auth);
	
}
