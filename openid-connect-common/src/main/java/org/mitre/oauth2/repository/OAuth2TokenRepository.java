/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.oauth2.repository;

import java.util.List;
import java.util.Set;

import org.mitre.data.PageCriteria;
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

	public Set<OAuth2AccessTokenEntity> getAllAccessTokens();

	public Set<OAuth2RefreshTokenEntity> getAllRefreshTokens();

	public Set<OAuth2AccessTokenEntity> getAllExpiredAccessTokens();

	public Set<OAuth2AccessTokenEntity> getAllExpiredAccessTokens(PageCriteria pageCriteria);

	public Set<OAuth2RefreshTokenEntity> getAllExpiredRefreshTokens();

	public Set<OAuth2RefreshTokenEntity> getAllExpiredRefreshTokens(PageCriteria pageCriteria);

	public Set<OAuth2AccessTokenEntity> getAccessTokensForResourceSet(ResourceSet rs);

	/**
	 * removes duplicate access tokens.
	 *
	 * @deprecated this method was added to return the remove duplicate access tokens values
	 * so that {code removeAccessToken(OAuth2AccessTokenEntity o)} would not to fail. the
	 * removeAccessToken method has been updated so as it will not fail in the event that an
	 * accessToken has been duplicated, so this method is unnecessary.
	 *
	 */
	@Deprecated
	public void clearDuplicateAccessTokens();

	/**
	 * removes duplicate refresh tokens.
	 *
	 * @deprecated this method was added to return the remove duplicate refresh token value
	 * so that {code removeRefreshToken(OAuth2RefreshTokenEntity o)} would not to fail. the
	 * removeRefreshToken method has been updated so as it will not fail in the event that
	 * refreshToken has been duplicated, so this method is unnecessary.
	 *
	 */
	@Deprecated
	public void clearDuplicateRefreshTokens();

	public List<OAuth2AccessTokenEntity> getAccessTokensForApprovedSite(ApprovedSite approvedSite);

}
