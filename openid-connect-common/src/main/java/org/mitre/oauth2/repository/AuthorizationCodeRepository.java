/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
package org.mitre.oauth2.repository;

import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

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
	 * Consume an authorization code.
	 * 
	 * @param code						the authorization code value
	 * @return							the authentication associated with the code
	 * @throws InvalidGrantException	if no AuthorizationCodeEntity is found with the given value
	 */
	public OAuth2Authentication consume(String code) throws InvalidGrantException;

}
