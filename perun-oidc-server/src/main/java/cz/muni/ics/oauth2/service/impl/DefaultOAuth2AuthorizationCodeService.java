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
/**
 *
 */
package cz.muni.ics.oauth2.service.impl;

import cz.muni.ics.data.AbstractPageOperationTemplate;
import cz.muni.ics.oauth2.model.AuthenticationHolderEntity;
import cz.muni.ics.oauth2.model.AuthorizationCodeEntity;
import cz.muni.ics.oauth2.repository.AuthenticationHolderRepository;
import cz.muni.ics.oauth2.repository.AuthorizationCodeRepository;
import java.util.Collection;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database-backed, random-value authorization code service implementation.
 *
 * @author aanganes
 *
 */
@Service("defaultOAuth2AuthorizationCodeService")
@Slf4j
public class DefaultOAuth2AuthorizationCodeService implements AuthorizationCodeServices {

	@Autowired
	private AuthorizationCodeRepository repository;

	@Autowired
	private AuthenticationHolderRepository authenticationHolderRepository;

	private int authCodeExpirationSeconds = 60 * 5; // expire in 5 minutes by default

	private final RandomValueStringGenerator generator = new RandomValueStringGenerator(22);

	/**
	 * Generate a random authorization code and create an AuthorizationCodeEntity,
	 * which will be stored in the repository.
	 *
	 * @param authentication 	the authentication of the current user, to be retrieved when the
	 * 							code is consumed
	 * @return 					the authorization code
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public String createAuthorizationCode(OAuth2Authentication authentication) {
		String code = generator.generate();

		// attach the authorization so that we can look it up later
		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setAuthentication(authentication);
		authHolder = authenticationHolderRepository.save(authHolder);

		// set the auth code to expire
		Date expiration = new Date(System.currentTimeMillis() + (getAuthCodeExpirationSeconds() * 1000L));

		AuthorizationCodeEntity entity = new AuthorizationCodeEntity(code, authHolder, expiration);
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

		AuthorizationCodeEntity result = repository.getByCode(code);

		if (result == null) {
			throw new InvalidGrantException("JpaAuthorizationCodeRepository: no authorization code found for value " + code);
		}

		OAuth2Authentication auth = result.getAuthenticationHolder().getAuthentication();

		repository.remove(result);

		return auth;
	}

	/**
	 * Find and remove all expired auth codes.
	 */
	@Transactional(value="defaultTransactionManager")
	public void clearExpiredAuthorizationCodes() {

		new AbstractPageOperationTemplate<AuthorizationCodeEntity>("clearExpiredAuthorizationCodes"){
			@Override
			public Collection<AuthorizationCodeEntity> fetchPage() {
				return repository.getExpiredCodes();
			}

			@Override
			protected void doOperation(AuthorizationCodeEntity item) {
				repository.remove(item);
			}
		}.execute();
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

	/**
	 * @return the authCodeExpirationSeconds
	 */
	public int getAuthCodeExpirationSeconds() {
		return authCodeExpirationSeconds;
	}

	/**
	 * @param authCodeExpirationSeconds the authCodeExpirationSeconds to set
	 */
	public void setAuthCodeExpirationSeconds(int authCodeExpirationSeconds) {
		this.authCodeExpirationSeconds = authCodeExpirationSeconds;
	}

}
