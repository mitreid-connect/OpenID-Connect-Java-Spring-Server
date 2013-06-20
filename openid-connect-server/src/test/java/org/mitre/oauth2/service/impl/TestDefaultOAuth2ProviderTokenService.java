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
package org.mitre.oauth2.service.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import com.google.common.collect.Sets;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultOAuth2ProviderTokenService {

	// Test Fixture:
	private OAuth2Authentication authentication;
	private ClientDetailsEntity client;

	@Mock
	private OAuth2TokenRepository tokenRepository;

	@Mock
	private AuthenticationHolderRepository authenticationHolderRepository;

	@Mock
	private ClientDetailsEntityService clientDetailsService;

	@Mock
	private TokenEnhancer tokenEnhancer;

	@InjectMocks
	private DefaultOAuth2ProviderTokenService service;

	/**
	 * Set up a mock authentication and mock client to work with.
	 */
	@Before
	public void prepare() {
		Mockito.reset(tokenRepository, authenticationHolderRepository, clientDetailsService, tokenEnhancer);

		authentication = Mockito.mock(OAuth2Authentication.class);
		Mockito.when(authentication.getAuthorizationRequest()).thenReturn(Mockito.mock(AuthorizationRequest.class));

		client = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(clientDetailsService.loadClientByClientId(Mockito.anyString())).thenReturn(client);

		// by default in tests, allow refresh tokens
		AuthorizationRequest clientAuth = authentication.getAuthorizationRequest();
		Mockito.when(clientAuth.getScope()).thenReturn(Sets.newHashSet("offline_access"));
		Mockito.when(client.isAllowRefresh()).thenReturn(true);
	}

	/**
	 * Tests exception handling for null authentication or null authorization.
	 */
	@Test
	public void createAccessToken_nullAuth() {

		Mockito.when(authentication.getAuthorizationRequest()).thenReturn(null);

		try {
			service.createAccessToken(null);
			fail("Authentication parameter is null. Excpected a AuthenticationCredentialsNotFoundException.");
		} catch (AuthenticationCredentialsNotFoundException e) {
			assertThat(e, is(notNullValue()));
		}

		try {
			service.createAccessToken(authentication);
			fail("AuthorizationRequest is null. Excpected a AuthenticationCredentialsNotFoundException.");
		} catch (AuthenticationCredentialsNotFoundException e) {
			assertThat(e, is(notNullValue()));
		}
	}

	/**
	 * Tests exception handling for clients not found.
	 */
	@Test(expected = InvalidClientException.class)
	public void createAccessToken_nullClient() {

		Mockito.when(clientDetailsService.loadClientByClientId(Mockito.anyString())).thenReturn(null);

		service.createAccessToken(authentication);
	}

	/**
	 * Tests the creation of access tokens for clients that are not allowed to have refresh tokens.
	 */
	@Test
	public void createAccessToken_noRefresh() {

		Mockito.when(client.isAllowRefresh()).thenReturn(false);

		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);

		Mockito.verify(clientDetailsService).loadClientByClientId(Mockito.anyString());
		Mockito.verify(authenticationHolderRepository).save(Mockito.any(AuthenticationHolderEntity.class));
		Mockito.verify(tokenEnhancer).enhance(token, authentication);
		Mockito.verify(tokenRepository).saveAccessToken(token);

		Mockito.verify(tokenRepository, Mockito.never()).saveRefreshToken(Mockito.any(OAuth2RefreshTokenEntity.class));
		assertThat(token.getRefreshToken(), is(nullValue()));
	}

	/**
	 * Tests the creation of access tokens for clients that are allowed to have refresh tokens.
	 */
	@Test
	public void createAccessToken_yesRefresh() {

		AuthorizationRequest clientAuth = authentication.getAuthorizationRequest();
		Mockito.when(clientAuth.getScope()).thenReturn(Sets.newHashSet("offline_access"));
		Mockito.when(client.isAllowRefresh()).thenReturn(true);

		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);

		// Note: a refactor may be appropriate to only save refresh tokens once to the repository during creation.
		Mockito.verify(tokenRepository, Mockito.atLeastOnce()).saveRefreshToken(Mockito.any(OAuth2RefreshTokenEntity.class));
		assertThat(token.getRefreshToken(), is(notNullValue()));

	}

	/**
	 * Checks to see that the expiration date of new tokens is being set accurately to within some delta for time skew.
	 */
	@Test
	public void createAccessToken_expiration() {

		Integer accessTokenValiditySeconds = 3600;
		Integer refreshTokenValiditySeconds = 600;
		
		long delta = 100L;

		Mockito.when(client.getAccessTokenValiditySeconds()).thenReturn(accessTokenValiditySeconds);
		Mockito.when(client.getRefreshTokenValiditySeconds()).thenReturn(refreshTokenValiditySeconds);

		long start = System.currentTimeMillis();
		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);
		long end = System.currentTimeMillis();

		// Accounting for some delta for time skew on either side.
		Date lowerBoundAccessTokens = new Date(start + (accessTokenValiditySeconds * 1000L) - delta);
		Date upperBoundAccessTokens = new Date(end + (accessTokenValiditySeconds * 1000L) + delta);
		Date lowerBoundRefreshTokens = new Date(start + (refreshTokenValiditySeconds * 1000L) - delta);
		Date upperBoundRefreshTokens = new Date(end + (refreshTokenValiditySeconds * 1000L) + delta);

		assertTrue(token.getExpiration().after(lowerBoundAccessTokens) && token.getExpiration().before(upperBoundAccessTokens));
		assertTrue(token.getRefreshToken().getExpiration().after(lowerBoundRefreshTokens) && token.getRefreshToken().getExpiration().before(upperBoundRefreshTokens));
	}
	
	// TODO verify JWT stuff in createAccessToken().

}
