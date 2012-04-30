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

import org.mitre.openid.connect.model.IdToken;

/**
 * IdToken repository interface
 * 
 * @author Michael Joseph Walsh
 *
 */
public interface IdTokenRepository {

	/**
	 * Returns the IdToken for the given id
	 * 
	 * @param id
	 *            id the id of the IdToken
	 * @return a valid IdToken if it exists, null otherwise
	 */	
	public IdToken getById(Long id);

	/**
	 * Removes the given IdToken from the repository
	 * 
	 * @param idToken
	 *            the IdToken object to remove
	 */
	public void remove(IdToken idToken);

	/**
	 * Removes an IdToken from the repository
	 * 
	 * @param id
	 *            the id of the IdToken to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a IdToken
	 * 
	 * @param idToken
	 * @return
	 */
	public IdToken save(IdToken idToken);
}
