/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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
package org.mitre.oauth2.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultOAuth2ProviderTokenService {

	// Grace period for time-sensitive tests.
	private static final long DELTA = 100L;

	// Test Fixture:
	private OAuth2Authentication authentication;
	private ClientDetailsEntity client;
	private ClientDetailsEntity badClient;
	private String clientId = "test_client";
	private String badClientId = "bad_client";
	private Set<String> scope = Sets.newHashSet("openid", "profile", "email", "offline_access");
	private OAuth2RefreshTokenEntity refreshToken;
	private String refreshTokenValue = "refresh_token_value";
	private TokenRequest tokenRequest;

	// for use when refreshing access tokens
	private OAuth2Request storedAuthRequest;
	private OAuth2Authentication storedAuthentication;
	private AuthenticationHolderEntity storedAuthHolder;
	private Set<String> storedScope;

	@Mock
	private OAuth2TokenRepository tokenRepository;

	@Mock
	private AuthenticationHolderRepository authenticationHolderRepository;

	@Mock
	private ClientDetailsEntityService clientDetailsService;

	@Mock
	private TokenEnhancer tokenEnhancer;

	@Mock
	private SystemScopeService scopeService;

	@InjectMocks
	private DefaultOAuth2ProviderTokenService service;

	/**
	 * Set up a mock authentication and mock client to work with.
	 */
	@Before
	public void prepare() {
		Mockito.reset(tokenRepository, authenticationHolderRepository, clientDetailsService, tokenEnhancer);



		authentication = Mockito.mock(OAuth2Authentication.class);
		OAuth2Request clientAuth = new OAuth2Request(null, clientId, null, true, scope, null, null, null, null);
		Mockito.when(authentication.getOAuth2Request()).thenReturn(clientAuth);

		client = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(client.getClientId()).thenReturn(clientId);
		Mockito.when(clientDetailsService.loadClientByClientId(clientId)).thenReturn(client);
		Mockito.when(client.isReuseRefreshToken()).thenReturn(true);

		// by default in tests, allow refresh tokens
		Mockito.when(client.isAllowRefresh()).thenReturn(true);

		// by default, clear access tokens on refresh
		Mockito.when(client.isClearAccessTokensOnRefresh()).thenReturn(true);

		badClient = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(badClient.getClientId()).thenReturn(badClientId);
		Mockito.when(clientDetailsService.loadClientByClientId(badClientId)).thenReturn(badClient);

		refreshToken = Mockito.mock(OAuth2RefreshTokenEntity.class);
		Mockito.when(tokenRepository.getRefreshTokenByValue(refreshTokenValue)).thenReturn(refreshToken);
		Mockito.when(refreshToken.getClient()).thenReturn(client);
		Mockito.when(refreshToken.isExpired()).thenReturn(false);

		tokenRequest = new TokenRequest(null, clientId, null, null);

		storedAuthentication = authentication;
		storedAuthRequest = clientAuth;
		storedAuthHolder = Mockito.mock(AuthenticationHolderEntity.class);
		storedScope = Sets.newHashSet(scope);

		Mockito.when(refreshToken.getAuthenticationHolder()).thenReturn(storedAuthHolder);
		Mockito.when(storedAuthHolder.getAuthentication()).thenReturn(storedAuthentication);
		Mockito.when(storedAuthentication.getOAuth2Request()).thenReturn(storedAuthRequest);

		Mockito.when(authenticationHolderRepository.save(Matchers.any(AuthenticationHolderEntity.class))).thenReturn(storedAuthHolder);

		Mockito.when(scopeService.fromStrings(Matchers.anySet())).thenAnswer(new Answer<Set<SystemScope>>() {
			@Override
			public Set<SystemScope> answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Set<String> input = (Set<String>) args[0];
				Set<SystemScope> output = new HashSet<>();
				for (String scope : input) {
					output.add(new SystemScope(scope));
				}
				return output;
			}
		});

		Mockito.when(scopeService.toStrings(Matchers.anySet())).thenAnswer(new Answer<Set<String>>() {
			@Override
			public Set<String> answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Set<SystemScope> input = (Set<SystemScope>) args[0];
				Set<String> output = new HashSet<>();
				for (SystemScope scope : input) {
					output.add(scope.getValue());
				}
				return output;
			}
		});

		// we're not testing restricted or reserved scopes here, just pass through
		Mockito.when(scopeService.removeReservedScopes(Matchers.anySet())).then(AdditionalAnswers.returnsFirstArg());
		Mockito.when(scopeService.removeRestrictedAndReservedScopes(Matchers.anySet())).then(AdditionalAnswers.returnsFirstArg());

		Mockito.when(tokenEnhancer.enhance(Matchers.any(OAuth2AccessTokenEntity.class), Matchers.any(OAuth2Authentication.class)))
		.thenAnswer(new Answer<OAuth2AccessTokenEntity>(){
			@Override
			public OAuth2AccessTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (OAuth2AccessTokenEntity) args[0];
			}
		});

		Mockito.when(tokenRepository.saveAccessToken(Matchers.any(OAuth2AccessTokenEntity.class)))
		.thenAnswer(new Answer<OAuth2AccessTokenEntity>() {
			@Override
			public OAuth2AccessTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (OAuth2AccessTokenEntity) args[0];
			}

		});

		Mockito.when(tokenRepository.saveRefreshToken(Matchers.any(OAuth2RefreshTokenEntity.class)))
		.thenAnswer(new Answer<OAuth2RefreshTokenEntity>() {
			@Override
			public OAuth2RefreshTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (OAuth2RefreshTokenEntity) args[0];
			}
		});

	}

	/**
	 * Tests exception handling for null authentication or null authorization.
	 */
	@Test
	public void createAccessToken_nullAuth() {

		Mockito.when(authentication.getOAuth2Request()).thenReturn(null);

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

		Mockito.when(clientDetailsService.loadClientByClientId(Matchers.anyString())).thenReturn(null);

		service.createAccessToken(authentication);
	}

	/**
	 * Tests the creation of access tokens for clients that are not allowed to have refresh tokens.
	 */
	@Test
	public void createAccessToken_noRefresh() {

		Mockito.when(client.isAllowRefresh()).thenReturn(false);

		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);

		Mockito.verify(clientDetailsService).loadClientByClientId(Matchers.anyString());
		Mockito.verify(authenticationHolderRepository).save(Matchers.any(AuthenticationHolderEntity.class));
		Mockito.verify(tokenEnhancer).enhance(Matchers.any(OAuth2AccessTokenEntity.class), Matchers.eq(authentication));
		Mockito.verify(tokenRepository).saveAccessToken(Matchers.any(OAuth2AccessTokenEntity.class));
		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		Mockito.verify(tokenRepository, Mockito.never()).saveRefreshToken(Matchers.any(OAuth2RefreshTokenEntity.class));

		assertThat(token.getRefreshToken(), is(nullValue()));
	}

	/**
	 * Tests the creation of access tokens for clients that are allowed to have refresh tokens.
	 */
	@Test
	public void createAccessToken_yesRefresh() {

		OAuth2Request clientAuth = new OAuth2Request(null, clientId, null, true, Sets.newHashSet(SystemScopeService.OFFLINE_ACCESS), null, null, null, null);
		Mockito.when(authentication.getOAuth2Request()).thenReturn(clientAuth);
		Mockito.when(client.isAllowRefresh()).thenReturn(true);

		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);

		// Note: a refactor may be appropriate to only save refresh tokens once to the repository during creation.
		Mockito.verify(tokenRepository, Mockito.atLeastOnce()).saveRefreshToken(Matchers.any(OAuth2RefreshTokenEntity.class));
		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(token.getRefreshToken(), is(notNullValue()));

	}

	/**
	 * Checks to see that the expiration date of new tokens is being set accurately to within some delta for time skew.
	 */
	@Test
	public void createAccessToken_expiration() {

		Integer accessTokenValiditySeconds = 3600;
		Integer refreshTokenValiditySeconds = 600;

		Mockito.when(client.getAccessTokenValiditySeconds()).thenReturn(accessTokenValiditySeconds);
		Mockito.when(client.getRefreshTokenValiditySeconds()).thenReturn(refreshTokenValiditySeconds);

		long start = System.currentTimeMillis();
		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);
		long end = System.currentTimeMillis();

		// Accounting for some delta for time skew on either side.
		Date lowerBoundAccessTokens = new Date(start + (accessTokenValiditySeconds * 1000L) - DELTA);
		Date upperBoundAccessTokens = new Date(end + (accessTokenValiditySeconds * 1000L) + DELTA);
		Date lowerBoundRefreshTokens = new Date(start + (refreshTokenValiditySeconds * 1000L) - DELTA);
		Date upperBoundRefreshTokens = new Date(end + (refreshTokenValiditySeconds * 1000L) + DELTA);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertTrue(token.getExpiration().after(lowerBoundAccessTokens) && token.getExpiration().before(upperBoundAccessTokens));
		assertTrue(token.getRefreshToken().getExpiration().after(lowerBoundRefreshTokens) && token.getRefreshToken().getExpiration().before(upperBoundRefreshTokens));
	}

	@Test
	public void createAccessToken_checkClient() {

		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(token.getClient().getClientId(), equalTo(clientId));
	}

	@Test
	public void createAccessToken_checkScopes() {

		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(token.getScope(), equalTo(scope));
	}

	@Test
	public void createAccessToken_checkAttachedAuthentication() {

		AuthenticationHolderEntity authHolder = Mockito.mock(AuthenticationHolderEntity.class);
		Mockito.when(authHolder.getAuthentication()).thenReturn(authentication);

		Mockito.when(authenticationHolderRepository.save(Matchers.any(AuthenticationHolderEntity.class))).thenReturn(authHolder);

		OAuth2AccessTokenEntity token = service.createAccessToken(authentication);

		assertThat(token.getAuthenticationHolder().getAuthentication(), equalTo(authentication));
		Mockito.verify(authenticationHolderRepository).save(Matchers.any(AuthenticationHolderEntity.class));
		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

	}

	@Test(expected = InvalidTokenException.class)
	public void refreshAccessToken_noRefreshToken() {

		Mockito.when(tokenRepository.getRefreshTokenByValue(Matchers.anyString())).thenReturn(null);

		service.refreshAccessToken(refreshTokenValue, tokenRequest);
	}

	@Test(expected = InvalidClientException.class)
	public void refreshAccessToken_notAllowRefresh() {

		Mockito.when(client.isAllowRefresh()).thenReturn(false);

		service.refreshAccessToken(refreshTokenValue, tokenRequest);
	}

	@Test(expected = InvalidClientException.class)
	public void refreshAccessToken_clientMismatch() {

		tokenRequest = new TokenRequest(null, badClientId, null, null);

		service.refreshAccessToken(refreshTokenValue, tokenRequest);
	}

	@Test(expected = InvalidTokenException.class)
	public void refreshAccessToken_expired() {

		Mockito.when(refreshToken.isExpired()).thenReturn(true);

		service.refreshAccessToken(refreshTokenValue, tokenRequest);
	}

	@Test
	public void refreshAccessToken_verifyAcessToken() {

		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);

		Mockito.verify(tokenRepository).clearAccessTokensForRefreshToken(refreshToken);

		assertThat(token.getClient(), equalTo(client));
		assertThat(token.getRefreshToken(), equalTo(refreshToken));
		assertThat(token.getAuthenticationHolder(), equalTo(storedAuthHolder));

		Mockito.verify(tokenEnhancer).enhance(token, storedAuthentication);
		Mockito.verify(tokenRepository).saveAccessToken(token);
		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

	}

	@Test
	public void refreshAccessToken_rotateRefreshToken() {

		when(client.isReuseRefreshToken()).thenReturn(false);

		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);

		Mockito.verify(tokenRepository).clearAccessTokensForRefreshToken(refreshToken);

		assertThat(token.getClient(), equalTo(client));
		assertThat(token.getRefreshToken(), not(equalTo(refreshToken)));
		assertThat(token.getAuthenticationHolder(), equalTo(storedAuthHolder));

		Mockito.verify(tokenEnhancer).enhance(token, storedAuthentication);
		Mockito.verify(tokenRepository).saveAccessToken(token);
		Mockito.verify(tokenRepository).removeRefreshToken(refreshToken);
		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

	}

	@Test
	public void refreshAccessToken_keepAccessTokens() {

		when(client.isClearAccessTokensOnRefresh()).thenReturn(false);

		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);

		Mockito.verify(tokenRepository, never()).clearAccessTokensForRefreshToken(refreshToken);

		assertThat(token.getClient(), equalTo(client));
		assertThat(token.getRefreshToken(), equalTo(refreshToken));
		assertThat(token.getAuthenticationHolder(), equalTo(storedAuthHolder));

		Mockito.verify(tokenEnhancer).enhance(token, storedAuthentication);
		Mockito.verify(tokenRepository).saveAccessToken(token);
		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

	}

	@Test
	public void refreshAccessToken_requestingSameScope() {

		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(token.getScope(), equalTo(storedScope));
	}

	@Test
	public void refreshAccessToken_requestingLessScope() {

		Set<String> lessScope = Sets.newHashSet("openid", "profile");

		tokenRequest.setScope(lessScope);

		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(token.getScope(), equalTo(lessScope));
	}

	@Test(expected = InvalidScopeException.class)
	public void refreshAccessToken_requestingMoreScope() {

		Set<String> moreScope = Sets.newHashSet(storedScope);
		moreScope.add("address");
		moreScope.add("phone");

		tokenRequest.setScope(moreScope);

		service.refreshAccessToken(refreshTokenValue, tokenRequest);
	}

	/**
	 * Tests the case where only some of the valid scope values are being requested along with
	 * other extra unauthorized scope values.
	 */
	@Test(expected = InvalidScopeException.class)
	public void refreshAccessToken_requestingMixedScope() {

		Set<String> mixedScope = Sets.newHashSet("openid", "profile", "address", "phone"); // no email or offline_access

		tokenRequest.setScope(mixedScope);

		service.refreshAccessToken(refreshTokenValue, tokenRequest);
	}

	@Test
	public void refreshAccessToken_requestingEmptyScope() {

		Set<String> emptyScope = Sets.newHashSet();

		tokenRequest.setScope(emptyScope);

		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(token.getScope(), equalTo(storedScope));
	}

	@Test
	public void refreshAccessToken_requestingNullScope() {

		tokenRequest.setScope(null);

		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(token.getScope(), equalTo(storedScope));

	}

	/**
	 * Checks to see that the expiration date of refreshed tokens is being set accurately to within some delta for time skew.
	 */
	@Test
	public void refreshAccessToken_expiration() {

		Integer accessTokenValiditySeconds = 3600;

		Mockito.when(client.getAccessTokenValiditySeconds()).thenReturn(accessTokenValiditySeconds);

		long start = System.currentTimeMillis();
		OAuth2AccessTokenEntity token = service.refreshAccessToken(refreshTokenValue, tokenRequest);
		long end = System.currentTimeMillis();

		// Accounting for some delta for time skew on either side.
		Date lowerBoundAccessTokens = new Date(start + (accessTokenValiditySeconds * 1000L) - DELTA);
		Date upperBoundAccessTokens = new Date(end + (accessTokenValiditySeconds * 1000L) + DELTA);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertTrue(token.getExpiration().after(lowerBoundAccessTokens) && token.getExpiration().before(upperBoundAccessTokens));
	}

}
