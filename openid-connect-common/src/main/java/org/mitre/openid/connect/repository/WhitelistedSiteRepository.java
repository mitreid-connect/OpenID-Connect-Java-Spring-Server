/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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
package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.openid.connect.model.WhitelistedSite;

/**
 * WhitelistedSite repository interface
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
public interface WhitelistedSiteRepository {

	/**
	 * Return a collection of all WhitelistedSite managed by this repository
	 *
	 * @return the WhitelistedSite collection, or null
	 */
	public Collection<WhitelistedSite> getAll();

	/**
	 * Returns the WhitelistedSite for the given id
	 *
	 * @param id
	 *            id the id of the WhitelistedSite
	 * @return a valid WhitelistedSite if it exists, null otherwise
	 */
	public WhitelistedSite getById(Long id);

	/**
	 * Find a WhitelistedSite by its associated ClientDetails reference
	 *
	 * @param client	the Relying Party
	 * @return			the corresponding WhitelistedSite if one exists for the RP, or null
	 */
	public WhitelistedSite getByClientId(String clientId);

	/**
	 * Return a collection of the WhitelistedSites created by a given user
	 *
	 * @param creator	the id of the admin who may have created some WhitelistedSites
	 * @return			the collection of corresponding WhitelistedSites, if any, or null
	 */
	public Collection<WhitelistedSite> getByCreator(String creatorId);

	/**
	 * Removes the given IdToken from the repository
	 *
	 * @param whitelistedSite
	 *            the WhitelistedSite object to remove
	 */
	public void remove(WhitelistedSite whitelistedSite);

	/**
	 * Persists a WhitelistedSite
	 *
	 * @param whitelistedSite
	 * @return
	 */
	public WhitelistedSite save(WhitelistedSite whiteListedSite);

	/**
	 * Persist changes to a whitelistedSite. The ID of oldWhitelistedSite is retained.
	 * @param oldWhitelistedSite
	 * @param whitelistedSite
	 * @return
	 */
	public WhitelistedSite update(WhitelistedSite oldWhitelistedSite, WhitelistedSite whitelistedSite);

}
