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
package cz.muni.ics.openid.connect.service;

import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.openid.connect.model.ApprovedSite;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * Interface for ApprovedSite service
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
public interface ApprovedSiteService {

	ApprovedSite createApprovedSite(String clientId, String userId, Date timeoutDate, Set<String> allowedScopes);

	/**
	 * Return a collection of all ApprovedSites
	 *
	 * @return the ApprovedSite collection, or null
	 */
	Collection<ApprovedSite> getAll();

	/**
	 * Return a collection of ApprovedSite managed by this repository matching the
	 * provided client ID and user ID
	 *
	 * @param clientId
	 * @param userId
	 * @return
	 */
	Collection<ApprovedSite> getByClientIdAndUserId(String clientId, String userId);

	/**
	 * Save an ApprovedSite
	 *
	 * @param approvedSite
	 *            the ApprovedSite to be saved
	 */
	ApprovedSite save(ApprovedSite approvedSite);

	/**
	 * Get ApprovedSite for id
	 *
	 * @param id
	 *            id for ApprovedSite
	 * @return ApprovedSite for id, or null
	 */
	ApprovedSite getById(Long id);

	/**
	 * Remove the ApprovedSite
	 *
	 * @param approvedSite
	 *            the ApprovedSite to remove
	 */
	void remove(ApprovedSite approvedSite);

	/**
	 * Get all sites approved by this user
	 * @param userId
	 * @return
	 */
	Collection<ApprovedSite> getByUserId(String userId);

	/**
	 * Get all sites associated with this client
	 * @param clientId
	 * @return
	 */
	Collection<ApprovedSite> getByClientId(String clientId);

	/**
	 * Clear out any approved sites for a given client.
	 * @param client
	 */
	void clearApprovedSitesForClient(ClientDetails client);

	/**
	 * Remove all expired approved sites fromt he data store.
	 * @return
	 */
	void clearExpiredSites();

	/**
	 * Return all approved access tokens for the site.
	 * @return
	 */
	List<OAuth2AccessTokenEntity> getApprovedAccessTokens(ApprovedSite approvedSite);

}
