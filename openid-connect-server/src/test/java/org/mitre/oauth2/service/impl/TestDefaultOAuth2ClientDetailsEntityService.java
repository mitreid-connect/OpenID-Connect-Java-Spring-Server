/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/
package org.mitre.oauth2.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.mitre.openid.connect.service.StatsService;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.ResourceSetService;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultOAuth2ClientDetailsEntityService {

	@Mock
	private OAuth2ClientRepository clientRepository;

	@Mock
	private OAuth2TokenRepository tokenRepository;

	@Mock
	private ApprovedSiteService approvedSiteService;

	@Mock
	private WhitelistedSiteService whitelistedSiteService;

	@Mock
	private BlacklistedSiteService blacklistedSiteService;

	@Mock
	private SystemScopeService scopeService;

	@Mock
	private ResourceSetService resourceSetService;

	@Mock
	private StatsService statsService;

	@InjectMocks
	private DefaultOAuth2ClientDetailsEntityService service;

	@Before
	public void prepare() {
		Mockito.reset(clientRepository, tokenRepository, approvedSiteService, whitelistedSiteService, blacklistedSiteService, scopeService, statsService);

		Mockito.when(clientRepository.saveClient(Matchers.any(ClientDetailsEntity.class))).thenAnswer(new Answer<ClientDetailsEntity>() {
			@Override
			public ClientDetailsEntity answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (ClientDetailsEntity) args[0];
			}
		});

		Mockito.when(clientRepository.updateClient(Matchers.anyLong(), Matchers.any(ClientDetailsEntity.class))).thenAnswer(new Answer<ClientDetailsEntity>() {
			@Override
			public ClientDetailsEntity answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (ClientDetailsEntity) args[1];
			}
		});

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

		// we're not testing reserved scopes here, just pass through when it's called
		Mockito.when(scopeService.removeReservedScopes(Matchers.anySet())).then(AdditionalAnswers.returnsFirstArg());

	}

	/**
	 * Failure case of existing client id.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void saveNewClient_badId() {

		// Set up a mock client.
		ClientDetailsEntity client = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(client.getId()).thenReturn(12345L); // any non-null ID will work

		service.saveNewClient(client);
	}

	/**
	 * Failure case of blacklisted client uri.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void saveNewClient_blacklisted() {

		ClientDetailsEntity client = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(client.getId()).thenReturn(null);

		String badUri = "badplace.xxx";

		Mockito.when(blacklistedSiteService.isBlacklisted(badUri)).thenReturn(true);
		Mockito.when(client.getRegisteredRedirectUri()).thenReturn(Sets.newHashSet(badUri));

		service.saveNewClient(client);
	}

	@Test
	public void saveNewClient_idWasAssigned() {

		// Set up a mock client.
		ClientDetailsEntity client = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(client.getId()).thenReturn(null);

		service.saveNewClient(client);

		Mockito.verify(client).setClientId(Matchers.anyString());
	}

	/**
	 * Makes sure client has offline access granted scope if allowed refresh tokens.
	 */
	@Test
	public void saveNewClient_yesOfflineAccess() {

		ClientDetailsEntity client = new ClientDetailsEntity();

		Set<String> grantTypes = new HashSet<>();
		grantTypes.add("refresh_token");
		client.setGrantTypes(grantTypes);

		client = service.saveNewClient(client);

		assertThat(client.getScope().contains(SystemScopeService.OFFLINE_ACCESS), is(equalTo(true)));
	}

	/**
	 * Makes sure client does not have offline access if not allowed to have refresh tokens.
	 */
	@Test
	public void saveNewClient_noOfflineAccess() {

		ClientDetailsEntity client = new ClientDetailsEntity();

		client = service.saveNewClient(client);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(client.getScope().contains(SystemScopeService.OFFLINE_ACCESS), is(equalTo(false)));
	}

	@Test
	public void loadClientByClientId_badId() {

		// null id
		try {
			service.loadClientByClientId(null);
			fail("Null client id. Expected an IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}

		// empty id
		try {
			service.loadClientByClientId("");
			fail("Empty client id. Expected an IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}

		// id not found
		String clientId = "b00g3r";
		Mockito.when(clientRepository.getClientByClientId(clientId)).thenReturn(null);
		try {
			service.loadClientByClientId(clientId);
			fail("Client id not found. Expected an InvalidClientException.");
		} catch (InvalidClientException e) {
			assertThat(e, is(notNullValue()));
		}

	}

	@Test(expected = InvalidClientException.class)
	public void deleteClient_badId() {

		Long id = 12345L;
		ClientDetailsEntity client = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(client.getId()).thenReturn(id);
		Mockito.when(clientRepository.getById(id)).thenReturn(null);

		service.deleteClient(client);
	}

	@Test
	public void deleteClient() {

		Long id = 12345L;
		String clientId = "b00g3r";

		ClientDetailsEntity client = Mockito.mock(ClientDetailsEntity.class);
		Mockito.when(client.getId()).thenReturn(id);
		Mockito.when(client.getClientId()).thenReturn(clientId);

		Mockito.when(clientRepository.getById(id)).thenReturn(client);

		WhitelistedSite site = Mockito.mock(WhitelistedSite.class);
		Mockito.when(whitelistedSiteService.getByClientId(clientId)).thenReturn(site);

		Mockito.when(resourceSetService.getAllForClient(client)).thenReturn(new HashSet<ResourceSet>());

		service.deleteClient(client);

		Mockito.verify(tokenRepository).clearTokensForClient(client);
		Mockito.verify(approvedSiteService).clearApprovedSitesForClient(client);
		Mockito.verify(whitelistedSiteService).remove(site);
		Mockito.verify(clientRepository).deleteClient(client);
	}

	@Test
	public void updateClient_nullClients() {

		ClientDetailsEntity oldClient = Mockito.mock(ClientDetailsEntity.class);
		ClientDetailsEntity newClient = Mockito.mock(ClientDetailsEntity.class);

		try {
			service.updateClient(oldClient, null);
			fail("New client is null. Expected an IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}

		try {
			service.updateClient(null, newClient);
			fail("Old client is null. Expected an IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}

		try {
			service.updateClient(null, null);
			fail("Both clients are null. Expected an IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateClient_blacklistedUri() {

		ClientDetailsEntity oldClient = Mockito.mock(ClientDetailsEntity.class);
		ClientDetailsEntity newClient = Mockito.mock(ClientDetailsEntity.class);

		String badSite = "badsite.xxx";

		Mockito.when(newClient.getRegisteredRedirectUri()).thenReturn(Sets.newHashSet(badSite));
		Mockito.when(blacklistedSiteService.isBlacklisted(badSite)).thenReturn(true);

		service.updateClient(oldClient, newClient);
	}

	@Test
	public void updateClient_yesOfflineAccess() {

		ClientDetailsEntity oldClient = new ClientDetailsEntity();
		ClientDetailsEntity client = new ClientDetailsEntity();

		Set<String> grantTypes = new HashSet<>();
		grantTypes.add("refresh_token");
		client.setGrantTypes(grantTypes);

		client = service.updateClient(oldClient, client);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(client.getScope().contains(SystemScopeService.OFFLINE_ACCESS), is(equalTo(true)));
	}

	@Test
	public void updateClient_noOfflineAccess() {

		ClientDetailsEntity oldClient = new ClientDetailsEntity();

		oldClient.getScope().add(SystemScopeService.OFFLINE_ACCESS);

		ClientDetailsEntity client = new ClientDetailsEntity();

		client = service.updateClient(oldClient, client);

		Mockito.verify(scopeService, Mockito.atLeastOnce()).removeReservedScopes(Matchers.anySet());

		assertThat(client.getScope().contains(SystemScopeService.OFFLINE_ACCESS), is(equalTo(false)));
	}
}
