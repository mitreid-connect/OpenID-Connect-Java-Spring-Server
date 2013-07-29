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
/**
 * 
 */
package org.mitre.oauth2.service.impl;

import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.repository.AuthorizationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.stereotype.Service;

/**
 * Database-backed, random-value authorization code service implementation.
 * 
 * @author aanganes
 *
 */
@Service
public class DefaultOAuth2AuthorizationCodeService implements AuthorizationCodeServices {

	@Autowired
	private AuthorizationCodeRepository repository;

	private RandomValueStringGenerator generator = new RandomValueStringGenerator();

	/**
	 * Generate a random authorization code and create an AuthorizationCodeEntity,
	 * which will be stored in the repository.
	 * 
	 * @param authentication 	the authentication of the current user, to be retrieved when the
	 * 							code is consumed
	 * @return 					the authorization code
	 */
	@Override
	public String createAuthorizationCode(OAuth2Authentication authentication) {
		String code = generator.generate();

		AuthorizationCodeEntity entity = new AuthorizationCodeEntity(code, authentication);
		repository.save(entity);

		return code;
	}

	/**
	 * Consume a given authorization code.
	 * Match the provided string to an AuthorizationCodeEntity. If one is found, return
	 * the authentication associated with the code. If one is not found, throw an
	 * InvalidGrantException.
	 * 
	 * @param code		the authorization code
	 * @return			the authentication that made the original request
	 * @throws 			InvalidGrantException, if an AuthorizationCodeEntity is not found with the given value
	 */
	@Override
	public OAuth2Authentication consumeAuthorizationCode(String code) throws InvalidGrantException {

		OAuth2Authentication auth = repository.consume(code);
		return auth;
	}

	/**
	 * @return the repository
	 */
	public AuthorizationCodeRepository getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(AuthorizationCodeRepository repository) {
		this.repository = repository;
	}

}
