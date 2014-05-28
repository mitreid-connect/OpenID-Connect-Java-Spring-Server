/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.service.SystemScopeService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.provider.ClientDetails;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultIntrospectionAuthorizer {

	@InjectMocks
	private DefaultIntrospectionAuthorizer introspectionPermitter;

	@Mock
	private SystemScopeService scopeService;

	@Test
	public void shouldPermitIntrospectionToSameClientTheTokenWasIssuedTo() {

		// given
		String sameClient = "same";

		// when
		boolean permitted = introspectionPermitter.isIntrospectionPermitted(
				clientWithId(sameClient), clientWithId(sameClient),
				scope("scope"));

		// then
		assertThat(permitted, is(true));
	}

	@Test
	public void shouldPermitIntrospectionToDifferentClientIfScopesMatch() {

		// given
		String authClient = "auth";
		String tokenClient = "token";
		Set<String> authScope = scope("scope1", "scope2", "scope3");
		Set<String> tokenScope = scope("scope1", "scope2");
		given(scopeService.scopesMatch(authScope, tokenScope)).willReturn(true);

		// when
		boolean permitted = introspectionPermitter.isIntrospectionPermitted(
				clientWithIdAndScope(authClient, authScope),
				clientWithId(tokenClient), tokenScope);

		// then
		assertThat(permitted, is(true));
	}

	@Test
	public void shouldNotPermitIntrospectionToDifferentClientIfScopesDontMatch() {

		// given
		String authClient = "auth";
		String tokenClient = "token";
		Set<String> authScope = scope("scope1", "scope2");
		Set<String> tokenScope = scope("scope1", "scope2", "scope3");
		given(scopeService.scopesMatch(authScope, tokenScope))
				.willReturn(false);

		// when
		boolean permitted = introspectionPermitter.isIntrospectionPermitted(
				clientWithIdAndScope(authClient, authScope),
				clientWithId(tokenClient), tokenScope);

		// then
		assertThat(permitted, is(false));
	}

	private ClientDetails clientWithId(String clientId) {
		ClientDetails client = mock(ClientDetails.class);
		given(client.getClientId()).willReturn(clientId);
		return client;
	}

	private ClientDetails clientWithIdAndScope(String clientId,
			Set<String> scope) {
		ClientDetails client = clientWithId(clientId);
		given(client.getScope()).willReturn(scope);
		return client;
	}

	private Set<String> scope(String... scopeItems) {
		return newHashSet(scopeItems);
	}
}
