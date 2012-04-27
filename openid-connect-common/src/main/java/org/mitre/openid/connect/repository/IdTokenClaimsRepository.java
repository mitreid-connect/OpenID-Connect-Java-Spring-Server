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

import org.mitre.openid.connect.model.IdTokenClaims;

public interface IdTokenClaimsRepository {

	/**
	 * Returns the IdTokenClaims for the given id
	 * 
	 * @param id
	 *            id the id of the Address
	 * @return a valid IdTokenClaims if it exists, null otherwise
	 */
	public IdTokenClaims getById(Long id);

	/**
	 * Removes the given IdTokenClaims from the repository
	 * 
	 * @param address
	 *            the IdTokenClaims object to remove
	 */
	public void remove(IdTokenClaims idTokenClaims);

	/**
	 * Removes an IdTokenClaims from the repository
	 * 
	 * @param id
	 *            the id of the IdTokenClaims to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a IdTokenClaims
	 * 
	 * @param idTokenClaims
	 *            the IdTokenClaims to be saved
	 * @return
	 */
	public IdTokenClaims save(IdTokenClaims idTokenClaims);
}
