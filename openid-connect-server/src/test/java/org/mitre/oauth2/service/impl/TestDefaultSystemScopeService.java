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

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

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
	private SystemScope extraScope1;
	private SystemScope structuredScope1;
	private SystemScope structuredScope1Value;

	private String defaultDynScope1String = "defaultDynScope1";
	private String defaultDynScope2String = "defaultDynScope2";
	private String defaultScope1String = "defaultScope1";
	private String defaultScope2String = "defaultScope2";
	private String dynScope1String = "dynScope1";
	private String extraScope1String = "extraScope1";
	private String structuredScope1String = "structuredScope1";
	private String structuredValue = "structuredValue";

	private Set<SystemScope> allScopes;
	private Set<String> allScopeStrings;

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

		// two default and dynamically registerable scopes
		defaultDynScope1 = new SystemScope(defaultDynScope1String);
		defaultDynScope2 = new SystemScope(defaultDynScope2String);
		defaultDynScope1.setAllowDynReg(true);
		defaultDynScope2.setAllowDynReg(true);
		defaultDynScope1.setDefaultScope(true);
		defaultDynScope2.setDefaultScope(true);

		// two strictly default scopes (isAllowDynReg false)
		defaultScope1 = new SystemScope(defaultScope1String);
		defaultScope2 = new SystemScope(defaultScope2String);
		defaultScope1.setDefaultScope(true);
		defaultScope2.setDefaultScope(true);

		// one strictly dynamically registerable scope (isDefault false)
		dynScope1 = new SystemScope(dynScope1String);
		dynScope1.setAllowDynReg(true);

		// extraScope1 : extra scope that is neither (defaults to false/false)
		extraScope1 = new SystemScope(extraScope1String);
		
		// structuredScope1 : structured scope
		structuredScope1 = new SystemScope(structuredScope1String);
		structuredScope1.setStructured(true);
		
		// structuredScope1Value : structured scope with value
		structuredScope1Value = new SystemScope(structuredScope1String);
		structuredScope1Value.setStructured(true);
		structuredScope1Value.setStructuredValue(structuredValue);

		allScopes = Sets.newHashSet(defaultDynScope1, defaultDynScope2, defaultScope1, defaultScope2, dynScope1, extraScope1, structuredScope1, structuredScope1Value);
		allScopeStrings = Sets.newHashSet(defaultDynScope1String, defaultDynScope2String, defaultScope1String, defaultScope2String, dynScope1String, extraScope1String, structuredScope1String, structuredScope1String + ":" + structuredValue);

		Mockito.when(repository.getByValue(defaultDynScope1String)).thenReturn(defaultDynScope1);
		Mockito.when(repository.getByValue(defaultDynScope2String)).thenReturn(defaultDynScope2);
		Mockito.when(repository.getByValue(defaultScope1String)).thenReturn(defaultScope1);
		Mockito.when(repository.getByValue(defaultScope2String)).thenReturn(defaultScope2);
		Mockito.when(repository.getByValue(dynScope1String)).thenReturn(dynScope1);
		Mockito.when(repository.getByValue(extraScope1String)).thenReturn(extraScope1);
		// we re-use this value so we've got to use thenAnswer instead
		Mockito.when(repository.getByValue(structuredScope1String)).thenAnswer(new Answer<SystemScope>() {
			@Override
            public SystemScope answer(InvocationOnMock invocation) throws Throwable {
				SystemScope s = new SystemScope(structuredScope1String);
				s.setStructured(true);
				return s;
            }
			
		});
		
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
	public void getDynReg() {

		Set<SystemScope> dynReg = Sets.newHashSet(defaultDynScope1, defaultDynScope2, dynScope1);

		assertThat(service.getDynReg(), equalTo(dynReg));
	}

	@Test
	public void fromStrings() {

		// check null condition
		assertThat(service.fromStrings(null), is(nullValue()));

		assertThat(service.fromStrings(allScopeStrings), equalTo(allScopes));
	}

	@Test
	public void toStrings() {

		// check null condition
		assertThat(service.toStrings(null), is(nullValue()));

		assertThat(service.toStrings(allScopes), equalTo(allScopeStrings));
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
	
	@Test
	public void scopesMatch_structured() {
		Set<String> expected = Sets.newHashSet("foo", "bar", "baz");
		Set<String> actualGood = Sets.newHashSet("foo:value", "baz", "bar");
		Set<String> actualBad = Sets.newHashSet("foo:value", "bar:value");
		
		// note: we have to use "thenAnswer" here to mimic the repository not serializing the structuredValue field
		Mockito.when(repository.getByValue("foo")).thenAnswer(new Answer<SystemScope>() {
			@Override
            public SystemScope answer(InvocationOnMock invocation) throws Throwable {
				SystemScope foo = new SystemScope("foo");
				foo.setStructured(true);
				return foo;
            }
			
		});
		
		assertThat(service.scopesMatch(expected, actualGood), is(true));
		
		assertThat(service.scopesMatch(expected, actualBad), is(false));
	}
}
