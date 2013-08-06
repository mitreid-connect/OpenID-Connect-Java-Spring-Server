/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
package org.mitre.oauth2.repository.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaOAuth2TokenRepository implements OAuth2TokenRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	public Set<OAuth2AccessTokenEntity> getAllAccessTokens() {
		TypedQuery<OAuth2AccessTokenEntity> query = manager.createNamedQuery("OAuth2AccessTokenEntity.getAll", OAuth2AccessTokenEntity.class);
		return new LinkedHashSet<OAuth2AccessTokenEntity>(query.getResultList());
	}

	@Override
	public Set<OAuth2RefreshTokenEntity> getAllRefreshTokens() {
		TypedQuery<OAuth2RefreshTokenEntity> query = manager.createNamedQuery("OAuth2RefreshTokenEntity.getAll", OAuth2RefreshTokenEntity.class);
		return new LinkedHashSet<OAuth2RefreshTokenEntity>(query.getResultList());
	}


	@Override
	public OAuth2AccessTokenEntity getAccessTokenByValue(String accessTokenValue) {
		TypedQuery<OAuth2AccessTokenEntity> query = manager.createNamedQuery("OAuth2AccessTokenEntity.getByTokenValue", OAuth2AccessTokenEntity.class);
		query.setParameter("tokenValue", accessTokenValue);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	public OAuth2AccessTokenEntity getAccessTokenById(Long id) {
		return manager.find(OAuth2AccessTokenEntity.class, id);
	}

	@Override
	@Transactional
	public OAuth2AccessTokenEntity saveAccessToken(OAuth2AccessTokenEntity token) {
		return JpaUtil.saveOrUpdate(token.getId(), manager, token);
	}

	@Override
	@Transactional
	public void removeAccessToken(OAuth2AccessTokenEntity accessToken) {
		OAuth2AccessTokenEntity found = getAccessTokenByValue(accessToken.getValue());
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("Access token not found: " + accessToken);
		}
	}

	@Override
	@Transactional
	public void clearAccessTokensForRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
		TypedQuery<OAuth2AccessTokenEntity> query = manager.createNamedQuery("OAuth2AccessTokenEntity.getByRefreshToken", OAuth2AccessTokenEntity.class);
		query.setParameter("refreshToken", refreshToken);
		List<OAuth2AccessTokenEntity> accessTokens = query.getResultList();
		for (OAuth2AccessTokenEntity accessToken : accessTokens) {
			removeAccessToken(accessToken);
		}
	}

	@Override
	public OAuth2RefreshTokenEntity getRefreshTokenByValue(String refreshTokenValue) {
		TypedQuery<OAuth2RefreshTokenEntity> query = manager.createNamedQuery("OAuth2RefreshTokenEntity.getByTokenValue", OAuth2RefreshTokenEntity.class);
		query.setParameter("tokenValue", refreshTokenValue);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	public OAuth2RefreshTokenEntity getRefreshTokenById(Long id) {
		return manager.find(OAuth2RefreshTokenEntity.class, id);
	}

	@Override
	@Transactional
	public OAuth2RefreshTokenEntity saveRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
		return JpaUtil.saveOrUpdate(refreshToken.getId(), manager, refreshToken);
	}

	@Override
	@Transactional
	public void removeRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
		OAuth2RefreshTokenEntity found = getRefreshTokenByValue(refreshToken.getValue());
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("Refresh token not found: " + refreshToken);
		}
	}

	@Override
	@Transactional
	public void clearTokensForClient(ClientDetailsEntity client) {
		TypedQuery<OAuth2AccessTokenEntity> queryA = manager.createNamedQuery("OAuth2AccessTokenEntity.getByClient", OAuth2AccessTokenEntity.class);
		queryA.setParameter("client", client);
		List<OAuth2AccessTokenEntity> accessTokens = queryA.getResultList();
		for (OAuth2AccessTokenEntity accessToken : accessTokens) {
			removeAccessToken(accessToken);
		}
		TypedQuery<OAuth2RefreshTokenEntity> queryR = manager.createNamedQuery("OAuth2RefreshTokenEntity.getByClient", OAuth2RefreshTokenEntity.class);
		queryR.setParameter("client", client);
		List<OAuth2RefreshTokenEntity> refreshTokens = queryR.getResultList();
		for (OAuth2RefreshTokenEntity refreshToken : refreshTokens) {
			removeRefreshToken(refreshToken);
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2TokenRepository#getAccessTokensForClient(org.mitre.oauth2.model.ClientDetailsEntity)
	 */
	@Override
	public List<OAuth2AccessTokenEntity> getAccessTokensForClient(ClientDetailsEntity client) {
		TypedQuery<OAuth2AccessTokenEntity> queryA = manager.createNamedQuery("OAuth2AccessTokenEntity.getByClient", OAuth2AccessTokenEntity.class);
		queryA.setParameter("client", client);
		List<OAuth2AccessTokenEntity> accessTokens = queryA.getResultList();
		return accessTokens;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2TokenRepository#getRefreshTokensForClient(org.mitre.oauth2.model.ClientDetailsEntity)
	 */
	@Override
	public List<OAuth2RefreshTokenEntity> getRefreshTokensForClient(ClientDetailsEntity client) {
		TypedQuery<OAuth2RefreshTokenEntity> queryR = manager.createNamedQuery("OAuth2RefreshTokenEntity.getByClient", OAuth2RefreshTokenEntity.class);
		queryR.setParameter("client", client);
		List<OAuth2RefreshTokenEntity> refreshTokens = queryR.getResultList();
		return refreshTokens;
	}

	@Override
	public OAuth2AccessTokenEntity getByAuthentication(OAuth2Authentication auth) {
		TypedQuery<OAuth2AccessTokenEntity> queryA = manager.createNamedQuery("OAuth2AccessTokenEntity.getByAuthentication", OAuth2AccessTokenEntity.class);
		queryA.setParameter("authentication", auth);
		List<OAuth2AccessTokenEntity> accessTokens = queryA.getResultList();
		return JpaUtil.getSingleResult(accessTokens);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2TokenRepository#getAccessTokenForIdToken(org.mitre.oauth2.model.OAuth2AccessTokenEntity)
	 */
	@Override
	public OAuth2AccessTokenEntity getAccessTokenForIdToken(OAuth2AccessTokenEntity idToken) {
		TypedQuery<OAuth2AccessTokenEntity> queryA = manager.createNamedQuery("OAuth2AccessTokenEntity.getByIdToken", OAuth2AccessTokenEntity.class);
		queryA.setParameter("idToken", idToken);
		List<OAuth2AccessTokenEntity> accessTokens = queryA.getResultList();
		return JpaUtil.getSingleResult(accessTokens);
	}

}
