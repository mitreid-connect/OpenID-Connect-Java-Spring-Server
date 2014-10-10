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
package org.mitre.openid.connect.client.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * @author wkim
 *
 */
public class TestPlainAuthRequestUrlBuilder {

	private ServerConfiguration serverConfig;
	private RegisteredClient clientConfig;
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
	public void buildAuthRequestUrlWithNonce() {
		executeTestWithOrWithoutNonce(true);
	}
	@Test
	public void buildAuthRequestUrlWithoutNonce() {
		executeTestWithOrWithoutNonce(false);
	}

	private void executeTestWithOrWithoutNonce(boolean useNonce) {
		Map<String, String> options = ImmutableMap.of("foo", "bar");
		String actualUrl;
		StringBuilder expectedUrl = expectedURLbuilder();
		if(useNonce){
			expectedUrl.append("&nonce=34fasf3ds");
			actualUrl = urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "https://client.example.org/","34fasf3ds", "af0ifjsldkj", options);
		}else{
			actualUrl = urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "https://client.example.org/","af0ifjsldkj", options);
		}
		
		Mockito.when(serverConfig.isUseNonce()).thenReturn(useNonce);
		assertThat(actualUrl, equalTo(expectedUrl.toString()));
	}

	private StringBuilder expectedURLbuilder() {
		StringBuilder expectedUrl = new StringBuilder();
		expectedUrl.append("https://server.example.com/authorize?");
		expectedUrl.append("response_type=code");
		expectedUrl.append("&client_id=s6BhdRkqt3");
		expectedUrl.append("&scope=openid+profile"); // plus sign used for space per application/x-www-form-encoded standard
		expectedUrl.append("&redirect_uri=https%3A%2F%2Fclient.example.org%2F");
		expectedUrl.append("&state=af0ifjsldkj");
		expectedUrl.append("&foo=bar");
		return expectedUrl;
	}

	@Test(expected = AuthenticationServiceException.class)
	public void buildAuthRequestUrl_badUri() {

		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("e=mc^2");

		Map<String, String> options = ImmutableMap.of("foo", "bar");

		urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "example.com", "", "", options);
	}

}
