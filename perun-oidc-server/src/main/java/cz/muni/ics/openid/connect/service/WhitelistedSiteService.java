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

import cz.muni.ics.openid.connect.model.WhitelistedSite;
import java.util.Collection;

/**
 * Interface for WhitelistedSite service
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
public interface WhitelistedSiteService {

	/**
	 * Return a collection of all WhitelistedSite managed by this service
	 *
	 * @return the WhitelistedSite collection, or null
	 */
	Collection<WhitelistedSite> getAll();

	/**
	 * Returns the WhitelistedSite for the given id
	 *
	 * @param id
	 *            id the id of the WhitelistedSite
	 * @return a valid WhitelistedSite if it exists, null otherwise
	 */
	WhitelistedSite getById(Long id);

	/**
	 * Find a WhitelistedSite by its associated ClientDetails reference
	 *
	 * @param client	the Relying Party
	 * @return			the corresponding WhitelistedSite if one exists for the RP, or null
	 */
	WhitelistedSite getByClientId(String clientId);



	/**
	 * Removes the given WhitelistedSite from the repository
	 *
	 * @param address
	 *            the WhitelistedSite object to remove
	 */
	void remove(WhitelistedSite whitelistedSite);

	/**
	 * Persists a new WhitelistedSite
	 *
	 * @param whitelistedSite
	 *            the WhitelistedSite to be saved
	 * @return
	 */
	WhitelistedSite saveNew(WhitelistedSite whitelistedSite);

	/**
	 * Updates an existing whitelisted site
	 */
	WhitelistedSite update(WhitelistedSite oldWhitelistedSite, WhitelistedSite whitelistedSite);

}
