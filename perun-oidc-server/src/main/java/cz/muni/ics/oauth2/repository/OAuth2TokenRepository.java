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
package cz.muni.ics.oauth2.repository;

import cz.muni.ics.data.PageCriteria;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity;
import cz.muni.ics.openid.connect.model.ApprovedSite;
import cz.muni.ics.uma.model.ResourceSet;
import java.util.List;
import java.util.Set;

public interface OAuth2TokenRepository {

	OAuth2AccessTokenEntity saveAccessToken(OAuth2AccessTokenEntity token);

	OAuth2RefreshTokenEntity getRefreshTokenByValue(String refreshTokenValue);

	OAuth2RefreshTokenEntity getRefreshTokenById(Long Id);

	void clearAccessTokensForRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	void removeRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	OAuth2RefreshTokenEntity saveRefreshToken(OAuth2RefreshTokenEntity refreshToken);

	OAuth2AccessTokenEntity getAccessTokenByValue(String accessTokenValue);

	OAuth2AccessTokenEntity getAccessTokenById(Long id);

	void removeAccessToken(OAuth2AccessTokenEntity accessToken);

	void clearTokensForClient(ClientDetailsEntity client);

	List<OAuth2AccessTokenEntity> getAccessTokensForClient(ClientDetailsEntity client);

	List<OAuth2RefreshTokenEntity> getRefreshTokensForClient(ClientDetailsEntity client);
	
	Set<OAuth2AccessTokenEntity> getAccessTokensByUserName(String name);
	
	Set<OAuth2RefreshTokenEntity> getRefreshTokensByUserName(String name);

	Set<OAuth2AccessTokenEntity> getAllAccessTokens();

	Set<OAuth2RefreshTokenEntity> getAllRefreshTokens();

	Set<OAuth2AccessTokenEntity> getAllExpiredAccessTokens();

	Set<OAuth2AccessTokenEntity> getAllExpiredAccessTokens(PageCriteria pageCriteria);

	Set<OAuth2RefreshTokenEntity> getAllExpiredRefreshTokens();

	Set<OAuth2RefreshTokenEntity> getAllExpiredRefreshTokens(PageCriteria pageCriteria);

	Set<OAuth2AccessTokenEntity> getAccessTokensForResourceSet(ResourceSet rs);

	/**
	 * removes duplicate access tokens.
	 *
	 * @deprecated this method was added to return the remove duplicate access tokens values
	 * so that {code removeAccessToken(OAuth2AccessTokenEntity o)} would not to fail. the
	 * removeAccessToken method has been updated so as it will not fail in the event that an
	 * accessToken has been duplicated, so this method is unnecessary.
	 */
	@Deprecated
	void clearDuplicateAccessTokens();

	/**
	 * removes duplicate refresh tokens.
	 *
	 * @deprecated this method was added to return the remove duplicate refresh token value
	 * so that {code removeRefreshToken(OAuth2RefreshTokenEntity o)} would not to fail. the
	 * removeRefreshToken method has been updated so as it will not fail in the event that
	 * refreshToken has been duplicated, so this method is unnecessary.
	 */
	@Deprecated
	void clearDuplicateRefreshTokens();

	List<OAuth2AccessTokenEntity> getAccessTokensForApprovedSite(ApprovedSite approvedSite);

}
