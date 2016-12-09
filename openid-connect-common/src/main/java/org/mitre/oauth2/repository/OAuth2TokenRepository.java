/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.oauth2.repository;

import java.util.List;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.uma.model.ResourceSet;

public interface OAuth2TokenRepository {

	public OAuth2AccessTokenEntity saveAccessToken(OAuth2AccessTokenEntity token);

	public OAuth2RefreshTokenEntity getRefreshTokenByValue(String refreshTokenValue);

	public OAuth2RefreshTokenEntity getRefreshTokenById(Long Id);

	public void clearAccessTokensForRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	public void removeRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	public OAuth2RefreshTokenEntity saveRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	public OAuth2AccessTokenEntity getAccessTokenByValue(String accessTokenValue);

	public OAuth2AccessTokenEntity getAccessTokenById(Long id);

	public void removeAccessToken(OAuth2AccessTokenEntity accessToken);

	public void clearTokensForClient(ClientDetailsEntity client);

	public List<OAuth2AccessTokenEntity> getAccessTokensForClient(ClientDetailsEntity client);

	public List<OAuth2RefreshTokenEntity> getRefreshTokensForClient(ClientDetailsEntity client);

	public OAuth2AccessTokenEntity getAccessTokenForIdToken(OAuth2AccessTokenEntity idToken);

	public Set<OAuth2AccessTokenEntity> getAllAccessTokens();

	public Set<OAuth2RefreshTokenEntity> getAllRefreshTokens();

	public Set<OAuth2AccessTokenEntity> getAllExpiredAccessTokens();

	public Set<OAuth2RefreshTokenEntity> getAllExpiredRefreshTokens();

	public Set<OAuth2AccessTokenEntity> getAccessTokensForResourceSet(ResourceSet rs);

	public void clearDuplicateAccessTokens();

	public void clearDuplicateRefreshTokens();
	
	public List<OAuth2AccessTokenEntity> getAccessTokensForApprovedSite(ApprovedSite approvedSite);

}
