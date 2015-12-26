/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
package org.mitre.oauth2.repository;

import java.util.Collection;

import org.mitre.oauth2.model.AuthorizationCodeEntity;

/**
 * Interface for saving and consuming OAuth2 authorization codes as AuthorizationCodeEntitys.
 * 
 * @author aanganes
 *
 */
public interface AuthorizationCodeRepository {

	/**
	 * Save an AuthorizationCodeEntity to the repository
	 * 
	 * @param authorizationCode the AuthorizationCodeEntity to save
	 * @return					the saved AuthorizationCodeEntity
	 */
	public AuthorizationCodeEntity save(AuthorizationCodeEntity authorizationCode);

	/**
	 * Get an authorization code from the repository by value.
	 * 
	 * @param code						the authorization code value
	 * @return							the authentication associated with the code
	 */
	public AuthorizationCodeEntity getByCode(String code);

	/**
	 * Remove an authorization code from the repository
	 * 
	 * @param authorizationCodeEntity
	 */
	public void remove(AuthorizationCodeEntity authorizationCodeEntity);

	/**
	 * @return A collection of all expired codes.
	 */
	public Collection<AuthorizationCodeEntity> getExpiredCodes();

}
