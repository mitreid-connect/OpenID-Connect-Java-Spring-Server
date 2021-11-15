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
package cz.muni.ics.oauth2.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.repository.SystemScopeRepository;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultSystemScopeService {

	// test fixture
	private SystemScope defaultDynScope1;
	private SystemScope defaultDynScope2;
	private SystemScope defaultScope1;
	private SystemScope defaultScope2;
	private SystemScope dynScope1;
	private SystemScope restrictedScope1;

	private final String defaultDynScope1String = "defaultDynScope1";
	private final String defaultDynScope2String = "defaultDynScope2";
	private final String defaultScope1String = "defaultScope1";
	private final String defaultScope2String = "defaultScope2";
	private final String dynScope1String = "dynScope1";
	private final String restrictedScope1String = "restrictedScope1";

	private Set<SystemScope> allScopes;
	private Set<String> allScopeStrings;
	private Set<SystemScope> allScopesWithValue;
	private Set<String> allScopeStringsWithValue;

	@Mock
	private SystemScopeRepository repository;

	@InjectMocks
	private DefaultSystemScopeService service;

	/**
	 * Assumes these SystemScope defaults: isDefaultScope=false and isAllowDynReg=false.
	 */
	@Before
	public void prepare() {

		Mockito.reset(repository);

		// two default and dynamically registerable scopes (unrestricted)
		defaultDynScope1 = new SystemScope(defaultDynScope1String);
		defaultDynScope2 = new SystemScope(defaultDynScope2String);
		defaultDynScope1.setDefaultScope(true);
		defaultDynScope2.setDefaultScope(true);

		// two strictly default scopes (restricted)
		defaultScope1 = new SystemScope(defaultScope1String);
		defaultScope2 = new SystemScope(defaultScope2String);
		defaultScope1.setRestricted(true);
		defaultScope2.setRestricted(true);
		defaultScope1.setDefaultScope(true);
		defaultScope2.setDefaultScope(true);

		// one strictly dynamically registerable scope (isDefault false)
		dynScope1 = new SystemScope(dynScope1String);

		// extraScope1 : extra scope that is neither restricted nor default (defaults to false/false)
		restrictedScope1 = new SystemScope(restrictedScope1String);
		restrictedScope1.setRestricted(true);


		allScopes = Sets.newHashSet(defaultDynScope1, defaultDynScope2, defaultScope1, defaultScope2, dynScope1, restrictedScope1);
		allScopeStrings = Sets.newHashSet(defaultDynScope1String, defaultDynScope2String, defaultScope1String, defaultScope2String, dynScope1String, restrictedScope1String);

		allScopesWithValue = Sets.newHashSet(defaultDynScope1, defaultDynScope2, defaultScope1, defaultScope2, dynScope1, restrictedScope1);
		allScopeStringsWithValue = Sets.newHashSet(defaultDynScope1String, defaultDynScope2String, defaultScope1String, defaultScope2String, dynScope1String, restrictedScope1String);

		Mockito.when(repository.getByValue(defaultDynScope1String)).thenReturn(defaultDynScope1);
		Mockito.when(repository.getByValue(defaultDynScope2String)).thenReturn(defaultDynScope2);
		Mockito.when(repository.getByValue(defaultScope1String)).thenReturn(defaultScope1);
		Mockito.when(repository.getByValue(defaultScope2String)).thenReturn(defaultScope2);
		Mockito.when(repository.getByValue(dynScope1String)).thenReturn(dynScope1);
		Mockito.when(repository.getByValue(restrictedScope1String)).thenReturn(restrictedScope1);

		Mockito.when(repository.getAll()).thenReturn(allScopes);
	}

	@Test
	public void getAll() {

		assertThat(service.getAll(), equalTo(allScopes));
	}

	@Test
	public void getDefaults() {

		Set<SystemScope> defaults = Sets.newHashSet(defaultDynScope1, defaultDynScope2, defaultScope1, defaultScope2);

		assertThat(service.getDefaults(), equalTo(defaults));
	}

	@Test
	public void getUnrestricted() {

		Set<SystemScope> unrestricted = Sets.newHashSet(defaultDynScope1, defaultDynScope2, dynScope1);

		assertThat(service.getUnrestricted(), equalTo(unrestricted));
	}

	@Test
	public void getRestricted() {
		Set<SystemScope> restricted = Sets.newHashSet(defaultScope1, defaultScope2, restrictedScope1);

		assertThat(service.getRestricted(), equalTo(restricted));

	}

	@Test
	public void fromStrings() {

		// check null condition
		assertThat(service.fromStrings(null), is(nullValue()));

		assertThat(service.fromStrings(allScopeStrings), equalTo(allScopes));

		assertThat(service.fromStrings(allScopeStringsWithValue), equalTo(allScopesWithValue));
	}

	@Test
	public void toStrings() {

		// check null condition
		assertThat(service.toStrings(null), is(nullValue()));

		assertThat(service.toStrings(allScopes), equalTo(allScopeStrings));

		assertThat(service.toStrings(allScopesWithValue), equalTo(allScopeStringsWithValue));
	}

	@Test
	public void scopesMatch() {

		Set<String> expected = Sets.newHashSet("foo", "bar", "baz");
		Set<String> actualGood = Sets.newHashSet("foo", "baz", "bar");
		Set<String> actualGood2 = Sets.newHashSet("foo", "bar");
		Set<String> actualBad = Sets.newHashSet("foo", "bob", "bar");

		// same scopes, different order
		assertThat(service.scopesMatch(expected, actualGood), is(true));

		// subset
		assertThat(service.scopesMatch(expected, actualGood2), is(true));

		// extra scope (fail)
		assertThat(service.scopesMatch(expected, actualBad), is(false));
	}

}
