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
package org.mitre.openid.connect.client.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mock;
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
public class TestStaticServerConfigurationService {


	private StaticServerConfigurationService service;

	private String issuer = "https://www.example.com/";

	@Mock
	private ServerConfiguration mockServerConfig;

	@Before
	public void prepare() {

		service = new StaticServerConfigurationService();

		Map<String, ServerConfiguration> servers = new HashMap<>();
		servers.put(issuer, mockServerConfig);

		service.setServers(servers);
	}

	@Test
	public void getServerConfiguration_success() {

		ServerConfiguration result = service.getServerConfiguration(issuer);

		assertThat(mockServerConfig, is(notNullValue()));
		assertEquals(mockServerConfig, result);
	}

	/**
	 * Checks the behavior when the issuer is not known.
	 */
	@Test
	public void getClientConfiguration_noIssuer() {

		ServerConfiguration result = service.getServerConfiguration("www.badexample.net");

		assertThat(result, is(nullValue()));
	}

}
