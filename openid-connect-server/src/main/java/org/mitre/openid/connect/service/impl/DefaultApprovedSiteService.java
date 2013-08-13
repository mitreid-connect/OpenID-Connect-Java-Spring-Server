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
package org.mitre.openid.connect.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Implementation of the ApprovedSiteService
 * 
 * @author Michael Joseph Walsh, aanganes
 *
 */
@Service
@Transactional
public class DefaultApprovedSiteService implements ApprovedSiteService {

	private static Logger logger = LoggerFactory.getLogger(DefaultApprovedSiteService.class);

	@Autowired
	private ApprovedSiteRepository approvedSiteRepository;

	@Autowired
	private OAuth2TokenRepository tokenRepository;

	@Override
	public Collection<ApprovedSite> getAll() {
		return approvedSiteRepository.getAll();
	}

	@Override
	@Transactional
	public ApprovedSite save(ApprovedSite approvedSite) {
		return approvedSiteRepository.save(approvedSite);
	}

	@Override
	public ApprovedSite getById(Long id) {
		return approvedSiteRepository.getById(id);
	}

	@Override
	@Transactional
	public void remove(ApprovedSite approvedSite) {

		//Remove any associated access and refresh tokens
		Set<OAuth2AccessTokenEntity> accessTokens = approvedSite.getApprovedAccessTokens();

		for (OAuth2AccessTokenEntity token : accessTokens) {
			if (token.getRefreshToken() != null) {
				tokenRepository.removeRefreshToken(token.getRefreshToken());
			}
			tokenRepository.removeAccessToken(token);
		}

		approvedSiteRepository.remove(approvedSite);
	}

	@Override
	@Transactional
	public ApprovedSite createApprovedSite(String clientId, String userId, Date timeoutDate, Set<String> allowedScopes,
			WhitelistedSite whitelistedSite) {

		ApprovedSite as = approvedSiteRepository.save(new ApprovedSite());

		Date now = new Date();
		as.setCreationDate(now);
		as.setAccessDate(now);
		as.setClientId(clientId);
		as.setUserId(userId);
		as.setTimeoutDate(timeoutDate);
		as.setAllowedScopes(allowedScopes);
		as.setWhitelistedSite(whitelistedSite);

		return save(as);

	}

	@Override
	public Collection<ApprovedSite> getByClientIdAndUserId(String clientId, String userId) {

		return approvedSiteRepository.getByClientIdAndUserId(clientId, userId);

	}

	/**
	 * @param userId
	 * @return
	 * @see org.mitre.openid.connect.repository.ApprovedSiteRepository#getByUserId(java.lang.String)
	 */
	@Override
	public Collection<ApprovedSite> getByUserId(String userId) {
		return approvedSiteRepository.getByUserId(userId);
	}

	/**
	 * @param clientId
	 * @return
	 * @see org.mitre.openid.connect.repository.ApprovedSiteRepository#getByClientId(java.lang.String)
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
				approvedSiteRepository.remove(approvedSite);
			}
		}
	}

	@Override
	public void clearExpiredSites() {

		logger.info("Clearing expired approved sites");

		Collection<ApprovedSite> expiredSites = getExpired();
		if (expiredSites != null) {
			for (ApprovedSite expired : expiredSites) {
				approvedSiteRepository.remove(expired);
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

}
