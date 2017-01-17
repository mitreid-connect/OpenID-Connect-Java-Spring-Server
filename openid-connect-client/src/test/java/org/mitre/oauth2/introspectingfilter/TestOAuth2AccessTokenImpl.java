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
package org.mitre.oauth2.introspectingfilter;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class TestOAuth2AccessTokenImpl {

	private static String tokenString = "thisisatokenstring";

	private static Set<String> scopes = ImmutableSet.of("bar", "foo");
	private static String scopeString = "foo bar";

	private static Date exp = new Date(123 * 1000L);
	private static Long expVal = 123L;

	@Test
	public void testFullToken() {


		JsonObject tokenObj = new JsonObject();
		tokenObj.addProperty("active", true);
		tokenObj.addProperty("scope", scopeString);
		tokenObj.addProperty("exp", expVal);
		tokenObj.addProperty("sub", "subject");
		tokenObj.addProperty("client_id", "123-456-789");

		OAuth2AccessTokenImpl tok = new OAuth2AccessTokenImpl(tokenObj, tokenString);

		assertThat(tok.getScope(), is(equalTo(scopes)));
		assertThat(tok.getExpiration(), is(equalTo(exp)));
	}

	@Test
	public void testNullExp() {


		JsonObject tokenObj = new JsonObject();
		tokenObj.addProperty("active", true);
		tokenObj.addProperty("scope", scopeString);
		tokenObj.addProperty("sub", "subject");
		tokenObj.addProperty("client_id", "123-456-789");

		OAuth2AccessTokenImpl tok = new OAuth2AccessTokenImpl(tokenObj, tokenString);

		assertThat(tok.getScope(), is(equalTo(scopes)));
		assertThat(tok.getExpiration(), is(equalTo(null)));
	}

	@Test
	public void testNullScopes() {


		JsonObject tokenObj = new JsonObject();
		tokenObj.addProperty("active", true);
		tokenObj.addProperty("exp", expVal);
		tokenObj.addProperty("sub", "subject");
		tokenObj.addProperty("client_id", "123-456-789");

		OAuth2AccessTokenImpl tok = new OAuth2AccessTokenImpl(tokenObj, tokenString);

		assertThat(tok.getScope(), is(equalTo(Collections.EMPTY_SET)));
		assertThat(tok.getExpiration(), is(equalTo(exp)));
	}

	@Test
	public void testNullScopesNullExp() {


		JsonObject tokenObj = new JsonObject();
		tokenObj.addProperty("active", true);
		tokenObj.addProperty("sub", "subject");
		tokenObj.addProperty("client_id", "123-456-789");

		OAuth2AccessTokenImpl tok = new OAuth2AccessTokenImpl(tokenObj, tokenString);

		assertThat(tok.getScope(), is(equalTo(Collections.EMPTY_SET)));
		assertThat(tok.getExpiration(), is(equalTo(null)));
	}

}
