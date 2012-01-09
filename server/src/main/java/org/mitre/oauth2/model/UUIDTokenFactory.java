package org.mitre.oauth2.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class UUIDTokenFactory implements OAuth2AccessTokenEntityFactory, OAuth2RefreshTokenEntityFactory {

	/**
	 * Create a new access token and set its value to a random UUID
	 */
	@Override
	public OAuth2AccessTokenEntity createNewAccessToken() {
		// create our token container
		OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
		
		// set a random value (TODO: support JWT)
	    String tokenValue = UUID.randomUUID().toString();
	    token.setValue(tokenValue);
	    
	    return token;
	}

	/**
	 * Create a new refresh token and set its value to a random UUID
	 */
	@Override
    public OAuth2RefreshTokenEntity createNewRefreshToken() {
		OAuth2RefreshTokenEntity refreshToken = new OAuth2RefreshTokenEntity();
		
		// set a random value for the refresh
		String refreshTokenValue = UUID.randomUUID().toString();
		refreshToken.setValue(refreshTokenValue);

		return refreshToken;
    }

}
