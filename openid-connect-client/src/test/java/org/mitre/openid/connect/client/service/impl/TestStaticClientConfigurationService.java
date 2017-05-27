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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestStaticClientConfigurationService {

	private StaticClientConfigurationService service;

	private String issuer = "https://www.example.com/";

	@Mock
	private RegisteredClient mockClient;

	@Mock
	private ServerConfiguration mockServerConfig;

	@Before
	public void prepare() {

		service = new StaticClientConfigurationService();

		Map<String, RegisteredClient> clients = new HashMap<>();
		clients.put(issuer, mockClient);

		service.setClients(clients);

		Mockito.when(mockServerConfig.getIssuer()).thenReturn(issuer);
	}

	@Test
	public void getClientConfiguration_success() {

		RegisteredClient result = service.getClientConfiguration(mockServerConfig);

		assertThat(mockClient, is(notNullValue()));
		assertEquals(mockClient, result);
	}

	/**
	 * Checks the behavior when the issuer is not known.
	 */
	@Test
	public void getClientConfiguration_noIssuer() {
		Mockito.when(mockServerConfig.getIssuer()).thenReturn("www.badexample.net");

		RegisteredClient actualClient = service.getClientConfiguration(mockServerConfig);

		assertThat(actualClient, is(nullValue()));
	}

}
