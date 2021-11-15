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
package cz.muni.ics.openid.connect.repository;

import cz.muni.ics.openid.connect.model.ApprovedSite;
import java.util.Collection;

/**
 * ApprovedSite repository interface
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
public interface ApprovedSiteRepository {

	/**
	 * Returns the ApprovedSite for the given id
	 *
	 * @param id
	 *            id the id of the ApprovedSite
	 * @return a valid ApprovedSite if it exists, null otherwise
	 */
	ApprovedSite getById(Long id);

	/**
	 * Return a collection of all ApprovedSites managed by this repository
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
	 * Removes the given ApprovedSite from the repository
	 *
	 * @param aggregator
	 *            the ApprovedSite object to remove
	 */
	void remove(ApprovedSite approvedSite);

	/**
	 * Persists an ApprovedSite
	 *
	 * @param aggregator
	 *            valid ApprovedSite instance
	 * @return the persisted entity
	 */
	ApprovedSite save(ApprovedSite approvedSite);

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

}
