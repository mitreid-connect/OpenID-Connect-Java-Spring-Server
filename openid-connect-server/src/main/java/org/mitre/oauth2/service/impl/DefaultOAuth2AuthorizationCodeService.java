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
package org.mitre.oauth2.service.impl;

import java.util.Collection;
import java.util.Date;

import org.mitre.data.AbstractPageOperationTemplate;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.PKCEAlgorithm;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.AuthorizationCodeRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.mitre.openid.connect.request.ConnectRequestParameters.CODE_CHALLENGE_METHOD;

/**
 * Database-backed, random-value authorization code service implementation.
 *
 * @author aanganes
 *
 */
@Service("defaultOAuth2AuthorizationCodeService")
public class DefaultOAuth2AuthorizationCodeService implements AuthorizationCodeServices {
	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(DefaultOAuth2AuthorizationCodeService.class);

	@Autowired
	private AuthorizationCodeRepository repository;

	@Autowired
	private AuthenticationHolderRepository authenticationHolderRepository;

	@Autowired
	private ClientDetailsEntityService clientDetailsService;

	private int authCodeExpirationSeconds = 60 * 5; // expire in 5 minutes by default

	private RandomValueStringGenerator generator = new RandomValueStringGenerator(22);

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

		// look up our client
		OAuth2Request request = authentication.getOAuth2Request();

		ClientDetailsEntity client = clientDetailsService.loadClientByClientId(request.getClientId());

		if (client == null) {
			throw new InvalidClientException("Client not found: " + request.getClientId());
		}

		// We want to check the code challenge method, if it doesn't match the client setting, we won't
		//   proceed to hand out an Authorization Code.
		if (request.getExtensions().containsKey(CODE_CHALLENGE_METHOD)) {
			PKCEAlgorithm alg = PKCEAlgorithm.parse((String) request.getExtensions().get(CODE_CHALLENGE_METHOD));

			// make sure the code challenge method matches the one defined for the client
			if (client.getCodeChallengeMethod() != null && !client.getCodeChallengeMethod().equals(alg)) {
				logger.error("Challenge method didn't match");
				throw new InvalidRequestException("Code challenge method does not match method defined in client");
			}
		}

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
