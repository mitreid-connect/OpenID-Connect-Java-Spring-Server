/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

package org.mitre.uma.service.impl;

import static org.mockito.Matchers.anySetOf;

import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.repository.PermissionRepository;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;

import com.google.common.collect.ImmutableSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import static org.mockito.Mockito.when;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author jricher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultPermissionService {

	@Mock
	private PermissionRepository permissionRepository;

	@Mock
	private SystemScopeService scopeService;

	@InjectMocks
	private DefaultPermissionService permissionService;

	private Set<String> scopes1 = ImmutableSet.of("foo", "bar", "baz");
	private Set<String> scopes2 = ImmutableSet.of("alpha", "beta", "betest");

	private ResourceSet rs1;
	private ResourceSet rs2;

	private String rs1Name = "resource set 1";
	private String rs1Owner = "resource set owner 1";
	private Long rs1Id = 1L;

	private String rs2Name = "resource set 2";
	private String rs2Owner = "resource set owner 2";
	private Long rs2Id = 2L;


	@Before
	public void prepare() {
		rs1 = new ResourceSet();
		rs1.setName(rs1Name);
		rs1.setOwner(rs1Owner);
		rs1.setId(rs1Id );
		rs1.setScopes(scopes1);

		rs2 = new ResourceSet();
		rs2.setName(rs2Name);
		rs2.setOwner(rs2Owner);
		rs2.setId(rs2Id);
		rs2.setScopes(scopes2);

		// have the repository just pass the argument through
		when(permissionRepository.save(Matchers.any(PermissionTicket.class))).then(AdditionalAnswers.returnsFirstArg());

		when(scopeService.scopesMatch(anySetOf(String.class), anySetOf(String.class))).then(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				@SuppressWarnings("unchecked")
				Set<String> expected = (Set<String>) arguments[0];
				@SuppressWarnings("unchecked")
				Set<String> actual = (Set<String>) arguments[1];

				return expected.containsAll(actual);
			}
		});

	}


	/**
	 * Test method for {@link org.mitre.uma.service.impl.DefaultPermissionService#createTicket(org.mitre.uma.model.ResourceSet, java.util.Set)}.
	 */
	@Test
	public void testCreate_ticket() {

		PermissionTicket perm = permissionService.createTicket(rs1, scopes1);

		// we want there to be a non-null ticket
		assertNotNull(perm.getTicket());
	}

	@Test
	public void testCreate_uuid() {
		PermissionTicket perm = permissionService.createTicket(rs1, scopes1);

		// we expect this to be a UUID
		UUID uuid = UUID.fromString(perm.getTicket());

		assertNotNull(uuid);

	}

	@Test
	public void testCreate_differentTicketsSameClient() {

		PermissionTicket perm1 = permissionService.createTicket(rs1, scopes1);
		PermissionTicket perm2 = permissionService.createTicket(rs1, scopes1);

		assertNotNull(perm1.getTicket());
		assertNotNull(perm2.getTicket());

		// make sure these are different from each other
		assertThat(perm1.getTicket(), not(equalTo(perm2.getTicket())));

	}

	@Test
	public void testCreate_differentTicketsDifferentClient() {

		PermissionTicket perm1 = permissionService.createTicket(rs1, scopes1);
		PermissionTicket perm2 = permissionService.createTicket(rs2, scopes2);

		assertNotNull(perm1.getTicket());
		assertNotNull(perm2.getTicket());

		// make sure these are different from each other
		assertThat(perm1.getTicket(), not(equalTo(perm2.getTicket())));

	}

	@Test(expected = InsufficientScopeException.class)
	public void testCreate_scopeMismatch() {
		@SuppressWarnings("unused")
		// try to get scopes outside of what we're allowed to do, this should throw an exception
		PermissionTicket perm = permissionService.createTicket(rs1, scopes2);
	}

}
