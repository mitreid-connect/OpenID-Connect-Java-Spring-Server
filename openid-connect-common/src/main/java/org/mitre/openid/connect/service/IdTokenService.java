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
package org.mitre.openid.connect.service;

import org.mitre.openid.connect.model.IdToken;

/**
 * Interface for IdToken service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface IdTokenService {

	/**
	 * Save an IdToken
	 * 
	 * @param idToken
	 *            the IdToken to be saved
	 */
	public void save(IdToken idToken);

	/**
	 * Get IdToken for id
	 * 
	 * @param id
	 *            id for IdToken
	 * @return IdToken for id, or null
	 */
	public IdToken getById(Long id);

	/**
	 * Remove the IdToken
	 * 
	 * @param idToken
	 *            the IdToken to remove
	 */
	public void remove(IdToken idToken);

	/**
	 * Remove the IdToken
	 * 
	 * @param id
	 *            id for IdToken to remove
	 */
	public void removeById(Long id);	
}
