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

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class TestDefaultIntrospectionResultAssembler {

    private DefaultIntrospectionResultAssembler assembler = new DefaultIntrospectionResultAssembler();

    @Test
    public void shouldAssembleExpectedResultForAccessToken() {

        // given
        OAuth2AccessTokenEntity accessToken = accessToken(new Date(123), scopes("foo", "bar"), "Bearer",
                authentication("name", request("clientId")));

        UserInfo userInfo = userInfo("sub");

        // when
        Map<String, Object> result = assembler.assembleFrom(accessToken, userInfo);


        // then
        Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
                .put("sub", "sub")
                .put("exp", new Date(123))
                .put("scope", "bar foo")
                .put("active", Boolean.TRUE)
                .put("user_id", "name")
                .put("client_id", "clientId")
                .put("token_type", "Bearer")
                .build();
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void shouldAssembleExpectedResultForAccessTokenWithoutUserInfo() {

        // given
        OAuth2AccessTokenEntity accessToken = accessToken(new Date(123), scopes("foo", "bar"), "Bearer",
                authentication("name", request("clientId")));

        // when
        Map<String, Object> result = assembler.assembleFrom(accessToken, null);


        // then
        Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
                .put("sub", "name")
                .put("exp", new Date(123))
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
        OAuth2AccessTokenEntity accessToken = accessToken(null, scopes("foo", "bar"), "Bearer",
                authentication("name", request("clientId")));

        UserInfo userInfo = userInfo("sub");

        // when
        Map<String, Object> result = assembler.assembleFrom(accessToken, userInfo);


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
    public void shouldAssembleExpectedResultForRefreshToken() {

        // given
        OAuth2RefreshTokenEntity refreshToken = refreshToken(new Date(123),
                authentication("name", request("clientId", scopes("foo",  "bar"))));

        UserInfo userInfo = userInfo("sub");

        // when
        Map<String, Object> result = assembler.assembleFrom(refreshToken, userInfo);


        // then
        Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
                .put("sub", "sub")
                .put("exp", new Date(123))
                .put("scope", "bar foo")
                .put("active", Boolean.TRUE)
                .put("user_id", "name")
                .put("client_id", "clientId")
                .build();
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void shouldAssembleExpectedResultForRefreshTokenWithoutUserInfo() {

        // given
        OAuth2RefreshTokenEntity refreshToken = refreshToken(new Date(123),
                authentication("name", request("clientId", scopes("foo",  "bar"))));

        // when
        Map<String, Object> result = assembler.assembleFrom(refreshToken, null);


        // then
        Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
                .put("sub", "name")
                .put("exp", new Date(123))
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

        // when
        Map<String, Object> result = assembler.assembleFrom(refreshToken, userInfo);


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

    private OAuth2AccessTokenEntity accessToken(Date exp, Set<String> scopes, String tokenType, OAuth2Authentication authentication) {
        OAuth2AccessTokenEntity accessToken = mock(OAuth2AccessTokenEntity.class, RETURNS_DEEP_STUBS);
        given(accessToken.getExpiration()).willReturn(exp);
        given(accessToken.getScope()).willReturn(scopes);
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
}
