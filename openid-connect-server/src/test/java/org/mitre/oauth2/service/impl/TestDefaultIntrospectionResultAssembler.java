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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.swing.text.DateFormatter;

import org.junit.Test;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.IntrospectionResultAssembler;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.uma.model.Permission;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Sets.newHashSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import static org.junit.Assert.assertThat;

public class TestDefaultIntrospectionResultAssembler {

	private IntrospectionResultAssembler assembler = new DefaultIntrospectionResultAssembler();

	private static DateFormatter dateFormat = new DateFormatter(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));

	@Test
	public void shouldAssembleExpectedResultForAccessToken() throws ParseException {

		// given
		OAuth2AccessTokenEntity accessToken = accessToken(new Date(123 * 1000L), scopes("foo", "bar"), null, "Bearer",
				authentication("name", request("clientId")));

		UserInfo userInfo = userInfo("sub");
		
		Set<String> authScopes = scopes("foo", "bar", "baz");

		// when
		Map<String, Object> result = assembler.assembleFrom(accessToken, userInfo, authScopes);


		// then
		Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
				.put("sub", "sub")
				.put("exp", 123L)
				.put("expires_at", dateFormat.valueToString(new Date(123 * 1000L)))
				.put("scope", "bar foo")
				.put("active", Boolean.TRUE)
				.put("user_id", "name")
				.put("client_id", "clientId")
				.put("token_type", "Bearer")
				.build();
		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void shouldAssembleExpectedResultForAccessToken_withPermissions() throws ParseException {

		// given
		OAuth2AccessTokenEntity accessToken = accessToken(new Date(123 * 1000L), scopes("foo", "bar"), 
				permissions(permission(1L, "foo", "bar")),
				"Bearer", authentication("name", request("clientId")));

		UserInfo userInfo = userInfo("sub");
		
		Set<String> authScopes = scopes("foo", "bar", "baz");

		// when
		Map<String, Object> result = assembler.assembleFrom(accessToken, userInfo, authScopes);


		// then
		Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
				.put("sub", "sub")
				.put("exp", 123L)
				.put("expires_at", dateFormat.valueToString(new Date(123 * 1000L)))
				.put("permissions", new ImmutableSet.Builder<Object>()
						.add(new ImmutableMap.Builder<String, Object>()
								.put("resource_set_id", "1") // note that the resource ID comes out as a string
								.put("scopes", new ImmutableSet.Builder<>()
										.add("bar")
										.add("foo")
										.build())
								.build())
						.build())
				// note that scopes are not included if permissions are included
				.put("active", Boolean.TRUE)
				.put("user_id", "name")
				.put("client_id", "clientId")
				.put("token_type", "Bearer")
				.build();
		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void shouldAssembleExpectedResultForAccessTokenWithoutUserInfo() throws ParseException {

		// given
		OAuth2AccessTokenEntity accessToken = accessToken(new Date(123 * 1000L), scopes("foo", "bar"), null, "Bearer",
				authentication("name", request("clientId")));

		Set<String> authScopes = scopes("foo", "bar", "baz");

		// when
		Map<String, Object> result = assembler.assembleFrom(accessToken, null, authScopes);


		// then
		Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
				.put("sub", "name")
				.put("exp", 123L)
				.put("expires_at", dateFormat.valueToString(new Date(123 * 1000L)))
				.put("scope", "bar foo")
				.put("active", Boolean.TRUE)
				.put("user_id", "name")
				.put("client_id", "clientId")
				.put("token_type", "Bearer")
				.build();
		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void shouldAssembleExpectedResultForAccessTokenWithoutExpiry() {

		// given
		OAuth2AccessTokenEntity accessToken = accessToken(null, scopes("foo", "bar"), null, "Bearer",
				authentication("name", request("clientId")));

		UserInfo userInfo = userInfo("sub");

		Set<String> authScopes = scopes("foo", "bar", "baz");

		// when
		Map<String, Object> result = assembler.assembleFrom(accessToken, userInfo, authScopes);


		// then
		Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
				.put("sub", "sub")
				.put("scope", "bar foo")
				.put("active", Boolean.TRUE)
				.put("user_id", "name")
				.put("client_id", "clientId")
				.put("token_type", "Bearer")
				.build();
		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void shouldAssembleExpectedResultForRefreshToken() throws ParseException {

		// given
		OAuth2RefreshTokenEntity refreshToken = refreshToken(new Date(123 * 1000L),
				authentication("name", request("clientId", scopes("foo",  "bar"))));

		UserInfo userInfo = userInfo("sub");

		Set<String> authScopes = scopes("foo", "bar", "baz");

		// when
		Map<String, Object> result = assembler.assembleFrom(refreshToken, userInfo, authScopes);


		// then
		Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
				.put("sub", "sub")
				.put("exp", 123L)
				.put("expires_at", dateFormat.valueToString(new Date(123 * 1000L)))
				.put("scope", "bar foo")
				.put("active", Boolean.TRUE)
				.put("user_id", "name")
				.put("client_id", "clientId")
				.build();
		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void shouldAssembleExpectedResultForRefreshTokenWithoutUserInfo() throws ParseException {

		// given
		OAuth2RefreshTokenEntity refreshToken = refreshToken(new Date(123 * 1000L),
				authentication("name", request("clientId", scopes("foo",  "bar"))));

		Set<String> authScopes = scopes("foo", "bar", "baz");

		// when
		Map<String, Object> result = assembler.assembleFrom(refreshToken, null, authScopes);


		// then
		Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
				.put("sub", "name")
				.put("exp", 123L)
				.put("expires_at", dateFormat.valueToString(new Date(123 * 1000L)))
				.put("scope", "bar foo")
				.put("active", Boolean.TRUE)
				.put("user_id", "name")
				.put("client_id", "clientId")
				.build();
		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void shouldAssembleExpectedResultForRefreshTokenWithoutExpiry() {

		// given
		OAuth2RefreshTokenEntity refreshToken = refreshToken(null,
				authentication("name", request("clientId", scopes("foo",  "bar"))));

		UserInfo userInfo = userInfo("sub");

		Set<String> authScopes = scopes("foo", "bar", "baz");

		// when
		Map<String, Object> result = assembler.assembleFrom(refreshToken, userInfo, authScopes);


		// then
		Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
				.put("sub", "sub")
				.put("scope", "bar foo")
				.put("active", Boolean.TRUE)
				.put("user_id", "name")
				.put("client_id", "clientId")
				.build();
		assertThat(result, is(equalTo(expected)));
	}

	private UserInfo userInfo(String sub) {
		UserInfo userInfo = mock(UserInfo.class);
		given(userInfo.getSub()).willReturn(sub);
		return userInfo;
	}

	private OAuth2AccessTokenEntity accessToken(Date exp, Set<String> scopes, Set<Permission> permissions, String tokenType, OAuth2Authentication authentication) {
		OAuth2AccessTokenEntity accessToken = mock(OAuth2AccessTokenEntity.class, RETURNS_DEEP_STUBS);
		given(accessToken.getExpiration()).willReturn(exp);
		given(accessToken.getScope()).willReturn(scopes);
		given(accessToken.getPermissions()).willReturn(permissions);
		given(accessToken.getTokenType()).willReturn(tokenType);
		given(accessToken.getAuthenticationHolder().getAuthentication()).willReturn(authentication);
		return accessToken;
	}

	private OAuth2RefreshTokenEntity refreshToken(Date exp, OAuth2Authentication authentication) {
		OAuth2RefreshTokenEntity refreshToken = mock(OAuth2RefreshTokenEntity.class, RETURNS_DEEP_STUBS);
		given(refreshToken.getExpiration()).willReturn(exp);
		given(refreshToken.getAuthenticationHolder().getAuthentication()).willReturn(authentication);
		return refreshToken;
	}

	private OAuth2Authentication authentication(String name, OAuth2Request request) {
		OAuth2Authentication authentication = mock(OAuth2Authentication.class);
		given(authentication.getName()).willReturn(name);
		given(authentication.getOAuth2Request()).willReturn(request);
		return authentication;
	}

	private OAuth2Request request(String clientId) {
		return request(clientId, null);
	}

	private OAuth2Request request(String clientId, Set<String> scopes) {
		return new OAuth2Request(null, clientId, null, true, scopes, null, null, null, null);
	}

	private Set<String> scopes(String... scopes) {
		return newHashSet(scopes);
	}
	
	private Set<Permission> permissions(Permission... permissions) {
		return newHashSet(permissions);
	}
	
	private Permission permission(Long resourceSetId, String... scopes) {
		Permission permission = mock(Permission.class, RETURNS_DEEP_STUBS);
		given(permission.getResourceSet().getId()).willReturn(resourceSetId);
		given(permission.getScopes()).willReturn(scopes(scopes));
		return permission;
	}
}
