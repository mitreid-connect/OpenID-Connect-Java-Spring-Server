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

package org.mitre.oauth2.introspectingfilter.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.gson.JsonObject;

import static org.junit.Assert.assertTrue;

/**
 * @author jricher
 *
 */
public class TestScopeBasedIntrospectionAuthoritiesGranter {

	private JsonObject introspectionResponse;

	private ScopeBasedIntrospectionAuthoritiesGranter granter = new ScopeBasedIntrospectionAuthoritiesGranter();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		introspectionResponse = new JsonObject();
	}

	/**
	 * Test method for {@link org.mitre.oauth2.introspectingfilter.service.impl.ScopeBasedIntrospectionAuthoritiesGranter#getAuthorities(com.google.gson.JsonObject)}.
	 */
	@Test
	public void testGetAuthoritiesJsonObject_withScopes() {
		introspectionResponse.addProperty("scope", "foo bar baz batman");

		List<GrantedAuthority> expected = new ArrayList<>();
		expected.add(new SimpleGrantedAuthority("ROLE_API"));
		expected.add(new SimpleGrantedAuthority("OAUTH_SCOPE_foo"));
		expected.add(new SimpleGrantedAuthority("OAUTH_SCOPE_bar"));
		expected.add(new SimpleGrantedAuthority("OAUTH_SCOPE_baz"));
		expected.add(new SimpleGrantedAuthority("OAUTH_SCOPE_batman"));

		List<GrantedAuthority> authorities = granter.getAuthorities(introspectionResponse);

		assertTrue(authorities.containsAll(expected));
		assertTrue(expected.containsAll(authorities));
	}

	/**
	 * Test method for {@link org.mitre.oauth2.introspectingfilter.service.impl.ScopeBasedIntrospectionAuthoritiesGranter#getAuthorities(com.google.gson.JsonObject)}.
	 */
	@Test
	public void testGetAuthoritiesJsonObject_withoutScopes() {

		List<GrantedAuthority> expected = new ArrayList<>();
		expected.add(new SimpleGrantedAuthority("ROLE_API"));

		List<GrantedAuthority> authorities = granter.getAuthorities(introspectionResponse);

		assertTrue(authorities.containsAll(expected));
		assertTrue(expected.containsAll(authorities));
	}

}
