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
package cz.muni.ics.openid.connect.service.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.repository.OAuth2TokenRepository;
import cz.muni.ics.openid.connect.model.ApprovedSite;
import cz.muni.ics.openid.connect.repository.ApprovedSiteRepository;
import cz.muni.ics.openid.connect.service.ApprovedSiteService;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ApprovedSiteService
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
@Service("defaultApprovedSiteService")
@Slf4j
public class DefaultApprovedSiteService implements ApprovedSiteService {

	@Autowired
	private ApprovedSiteRepository approvedSiteRepository;

	@Autowired
	private OAuth2TokenRepository tokenRepository;

	@Override
	public Collection<ApprovedSite> getAll() {
		return approvedSiteRepository.getAll();
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public ApprovedSite save(ApprovedSite approvedSite) {
		return approvedSiteRepository.save(approvedSite);
	}

	@Override
	public ApprovedSite getById(Long id) {
		return approvedSiteRepository.getById(id);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(ApprovedSite approvedSite) {

		//Remove any associated access and refresh tokens
		List<OAuth2AccessTokenEntity> accessTokens = getApprovedAccessTokens(approvedSite);

		for (OAuth2AccessTokenEntity token : accessTokens) {
			if (token.getRefreshToken() != null) {
				tokenRepository.removeRefreshToken(token.getRefreshToken());
			}
			tokenRepository.removeAccessToken(token);
		}

		approvedSiteRepository.remove(approvedSite);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public ApprovedSite createApprovedSite(String clientId, String userId, Date timeoutDate, Set<String> allowedScopes) {

		ApprovedSite as = approvedSiteRepository.save(new ApprovedSite());

		Date now = new Date();
		as.setCreationDate(now);
		as.setAccessDate(now);
		as.setClientId(clientId);
		as.setUserId(userId);
		as.setTimeoutDate(timeoutDate);
		as.setAllowedScopes(allowedScopes);

		return save(as);

	}

	@Override
	public Collection<ApprovedSite> getByClientIdAndUserId(String clientId, String userId) {

		return approvedSiteRepository.getByClientIdAndUserId(clientId, userId);

	}

	/**
	 * @param userId
	 * @return
	 * @see ApprovedSiteRepository#getByUserId(java.lang.String)
	 */
	@Override
	public Collection<ApprovedSite> getByUserId(String userId) {
		return approvedSiteRepository.getByUserId(userId);
	}

	/**
	 * @param clientId
	 * @return
	 * @see ApprovedSiteRepository#getByClientId(java.lang.String)
	 */
	@Override
	public Collection<ApprovedSite> getByClientId(String clientId) {
		return approvedSiteRepository.getByClientId(clientId);
	}


	@Override
	public void clearApprovedSitesForClient(ClientDetails client) {
		Collection<ApprovedSite> approvedSites = approvedSiteRepository.getByClientId(client.getClientId());
		if (approvedSites != null) {
			for (ApprovedSite approvedSite : approvedSites) {
				remove(approvedSite);
			}
		}
	}

	@Override
	public void clearExpiredSites() {

		log.debug("Clearing expired approved sites");

		Collection<ApprovedSite> expiredSites = getExpired();
		if (expiredSites.size() > 0) {
			log.info("Found " + expiredSites.size() + " expired approved sites.");
		}
		if (expiredSites != null) {
			for (ApprovedSite expired : expiredSites) {
				remove(expired);
			}
		}

	}

	private Predicate<ApprovedSite> isExpired = new Predicate<ApprovedSite>() {
		@Override
		public boolean apply(ApprovedSite input) {
			return (input != null && input.isExpired());
		}
	};

	private Collection<ApprovedSite> getExpired() {
		return Collections2.filter(approvedSiteRepository.getAll(), isExpired);
	}

	@Override
	public List<OAuth2AccessTokenEntity> getApprovedAccessTokens(
			ApprovedSite approvedSite) {
		return tokenRepository.getAccessTokensForApprovedSite(approvedSite);

	}

}
