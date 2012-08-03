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
package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;

/**
 * ApprovedSite repository interface
 *
 * @author Michael Joseph Walsh
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
	public ApprovedSite getById(Long id);

	/**
	 * Return a collection of all ApprovedSites managed by this repository
	 * 
	 * @return the ApprovedSite collection, or null
	 */
	public Collection<ApprovedSite> getAll();

	/**
	 * Return a collection of ApprovedSite managed by this repository matching the
	 * provided ClientDetailsEntity
	 * 
	 * @param userId
	 * @return
	 */
	public Collection<ApprovedSite> getByClientDetails(
			ClientDetailsEntity clientDetails);

	/**
	 * Return a collection of ApprovedSite managed by this repository matching the
	 * provided user id
	 * 
	 * @param userId
	 * @return
	 */
	public Collection<ApprovedSite> getByUserId(String userId);

	/**
	 * Removes the given ApprovedSite from the repository
	 * 
	 * @param aggregator
	 *            the ApprovedSite object to remove
	 */
	public void remove(ApprovedSite approvedSite);

	/**
	 * Removes an ApprovedSite from the repository
	 * 
	 * @param id
	 *            the id of the ApprovedSite to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists an ApprovedSite
	 * 
	 * @param aggregator
	 *            valid ApprovedSite instance
	 * @return the persisted entity
	 */
	public ApprovedSite save(ApprovedSite approvedSite);
}
