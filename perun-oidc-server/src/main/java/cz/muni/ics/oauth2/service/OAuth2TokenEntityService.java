/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
 *******************************************************************************/
package cz.muni.ics.oauth2.service;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

public interface OAuth2TokenEntityService extends AuthorizationServerTokenServices, ResourceServerTokenServices {

	@Override
    OAuth2AccessTokenEntity readAccessToken(String accessTokenValue);

	OAuth2RefreshTokenEntity getRefreshToken(String refreshTokenValue);

	void revokeRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	void revokeAccessToken(OAuth2AccessTokenEntity accessToken);

	List<OAuth2AccessTokenEntity> getAccessTokensForClient(ClientDetailsEntity client);

	List<OAuth2RefreshTokenEntity> getRefreshTokensForClient(ClientDetailsEntity client);

	void clearExpiredTokens();

	OAuth2AccessTokenEntity saveAccessToken(OAuth2AccessTokenEntity accessToken);

	OAuth2RefreshTokenEntity saveRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	@Override
	OAuth2AccessTokenEntity getAccessToken(OAuth2Authentication authentication);

	OAuth2AccessTokenEntity getAccessTokenById(Long id);

	OAuth2RefreshTokenEntity getRefreshTokenById(Long id);

	Set<OAuth2AccessTokenEntity> getAllAccessTokensForUser(String name);

	Set<OAuth2RefreshTokenEntity> getAllRefreshTokensForUser(String name);

	OAuth2AccessTokenEntity getRegistrationAccessTokenForClient(ClientDetailsEntity client);
}
