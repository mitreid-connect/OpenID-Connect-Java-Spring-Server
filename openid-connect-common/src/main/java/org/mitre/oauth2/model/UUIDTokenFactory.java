/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
