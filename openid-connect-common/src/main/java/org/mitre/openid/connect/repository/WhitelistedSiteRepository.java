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

import org.mitre.openid.connect.model.WhitelistedSite;

/**
 * WhitelistedSite repository interface
 * 
 * @author Michael Joseph Walsh
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
	 * Removes the given IdToken from the repository
	 * 
	 * @param whitelistedSite
	 *            the WhitelistedSite object to remove
	 */
	public void remove(WhitelistedSite whitelistedSite);

	/**
	 * Removes an WhitelistedSite from the repository
	 * 
	 * @param id
	 *            the id of the IdToken to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a WhitelistedSite
	 * 
	 * @param whitelistedSite
	 * @return
	 */
	public WhitelistedSite save(WhitelistedSite whiteListedSite);

}
