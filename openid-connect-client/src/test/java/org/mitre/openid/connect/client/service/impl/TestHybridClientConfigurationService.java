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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHybridClientConfigurationService {

	@Mock
	private StaticClientConfigurationService mockStaticService;

	@Mock
	private DynamicRegistrationClientConfigurationService mockDynamicService;

	@InjectMocks
	private HybridClientConfigurationService hybridService;

	// test fixture

	@Mock
	private RegisteredClient mockClient;

	@Mock
	private ServerConfiguration mockServerConfig;

	private String issuer = "https://www.example.com/";

	@Before
	public void prepare() {

		Mockito.reset(mockDynamicService, mockStaticService);

		Mockito.when(mockServerConfig.getIssuer()).thenReturn(issuer);

	}

	@Test
	public void getClientConfiguration_useStatic() {

		Mockito.when(mockStaticService.getClientConfiguration(mockServerConfig)).thenReturn(mockClient);

		RegisteredClient result = hybridService.getClientConfiguration(mockServerConfig);

		Mockito.verify(mockStaticService).getClientConfiguration(mockServerConfig);
		Mockito.verify(mockDynamicService, Mockito.never()).getClientConfiguration(Matchers.any(ServerConfiguration.class));
		assertEquals(mockClient, result);
	}

	@Test
	public void getClientConfiguration_useDynamic() {

		Mockito.when(mockStaticService.getClientConfiguration(mockServerConfig)).thenReturn(null);
		Mockito.when(mockDynamicService.getClientConfiguration(mockServerConfig)).thenReturn(mockClient);

		RegisteredClient result = hybridService.getClientConfiguration(mockServerConfig);

		Mockito.verify(mockStaticService).getClientConfiguration(mockServerConfig);
		Mockito.verify(mockDynamicService).getClientConfiguration(mockServerConfig);
		assertEquals(mockClient, result);
	}

	/**
	 * Checks the behavior when the issuer is not known.
	 */
	@Test
	public void getClientConfiguration_noIssuer() {

		// The mockServerConfig is known to both services
		Mockito.when(mockStaticService.getClientConfiguration(mockServerConfig)).thenReturn(mockClient);
		Mockito.when(mockDynamicService.getClientConfiguration(mockServerConfig)).thenReturn(mockClient);

		// But oh noes! We're going to ask it to find us some other issuer
		ServerConfiguration badIssuer = Mockito.mock(ServerConfiguration.class);
		Mockito.when(badIssuer.getIssuer()).thenReturn("www.badexample.com");

		RegisteredClient result = hybridService.getClientConfiguration(badIssuer);

		Mockito.verify(mockStaticService).getClientConfiguration(badIssuer);
		Mockito.verify(mockDynamicService).getClientConfiguration(badIssuer);
		assertThat(result, is(nullValue()));
	}
}
