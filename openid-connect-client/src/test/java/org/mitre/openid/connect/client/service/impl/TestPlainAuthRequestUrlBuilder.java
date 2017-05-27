/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.openid.connect.client.service.impl;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertThat;

/**
 * @author wkim
 *
 */
public class TestPlainAuthRequestUrlBuilder {

	// Test fixture:
	ServerConfiguration serverConfig;
	RegisteredClient clientConfig;

	private PlainAuthRequestUrlBuilder urlBuilder = new PlainAuthRequestUrlBuilder();

	@Before
	public void prepare() {

		serverConfig = Mockito.mock(ServerConfiguration.class);
		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("https://server.example.com/authorize");

		clientConfig = Mockito.mock(RegisteredClient.class);
		Mockito.when(clientConfig.getClientId()).thenReturn("s6BhdRkqt3");
		Mockito.when(clientConfig.getScope()).thenReturn(Sets.newHashSet("openid", "profile"));
	}

	@Test
	public void buildAuthRequestUrl() {

		String expectedUrl = "https://server.example.com/authorize?" +
				"response_type=code" +
				"&client_id=s6BhdRkqt3" +
				"&scope=openid+profile" + // plus sign used for space per application/x-www-form-encoded standard
				"&redirect_uri=https%3A%2F%2Fclient.example.org%2F" +
				"&nonce=34fasf3ds" +
				"&state=af0ifjsldkj" +
				"&foo=bar";

		Map<String, String> options = ImmutableMap.of("foo", "bar");

		String actualUrl = urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "https://client.example.org/", "34fasf3ds", "af0ifjsldkj", options, null);

		assertThat(actualUrl, equalTo(expectedUrl));
	}

	@Test
	public void buildAuthRequestUrl_withLoginHint() {

		String expectedUrl = "https://server.example.com/authorize?" +
				"response_type=code" +
				"&client_id=s6BhdRkqt3" +
				"&scope=openid+profile" + // plus sign used for space per application/x-www-form-encoded standard
				"&redirect_uri=https%3A%2F%2Fclient.example.org%2F" +
				"&nonce=34fasf3ds" +
				"&state=af0ifjsldkj" +
				"&foo=bar" +
				"&login_hint=bob";

		Map<String, String> options = ImmutableMap.of("foo", "bar");

		String actualUrl = urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "https://client.example.org/", "34fasf3ds", "af0ifjsldkj", options, "bob");

		assertThat(actualUrl, equalTo(expectedUrl));
	}

	@Test(expected = AuthenticationServiceException.class)
	public void buildAuthRequestUrl_badUri() {

		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("e=mc^2");

		Map<String, String> options = ImmutableMap.of("foo", "bar");

		urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "example.com", "", "", options, null);
	}

}
