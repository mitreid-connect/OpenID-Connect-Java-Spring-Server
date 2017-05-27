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


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class TestHybridServerConfigurationService {

	@Mock
	private StaticServerConfigurationService mockStaticService;

	@Mock
	private DynamicServerConfigurationService mockDynamicService;

	@InjectMocks
	private HybridServerConfigurationService hybridService;

	@Mock
	private ServerConfiguration mockServerConfig;

	private String issuer = "https://www.example.com/";

	@Before
	public void prepare() {

		Mockito.reset(mockDynamicService, mockStaticService);

	}


	@Test
	public void getServerConfiguration_useStatic() {

		Mockito.when(mockStaticService.getServerConfiguration(issuer)).thenReturn(mockServerConfig);

		ServerConfiguration result = hybridService.getServerConfiguration(issuer);

		Mockito.verify(mockStaticService).getServerConfiguration(issuer);
		Mockito.verify(mockDynamicService, Mockito.never()).getServerConfiguration(Matchers.anyString());
		assertEquals(mockServerConfig, result);
	}

	@Test
	public void getServerConfiguration_useDynamic() {

		Mockito.when(mockStaticService.getServerConfiguration(issuer)).thenReturn(null);
		Mockito.when(mockDynamicService.getServerConfiguration(issuer)).thenReturn(mockServerConfig);

		ServerConfiguration result = hybridService.getServerConfiguration(issuer);

		Mockito.verify(mockStaticService).getServerConfiguration(issuer);
		Mockito.verify(mockDynamicService).getServerConfiguration(issuer);
		assertEquals(mockServerConfig, result);
	}

	/**
	 * Checks the behavior when the issuer is not known.
	 */
	@Test
	public void getServerConfiguration_noIssuer() {

		Mockito.when(mockStaticService.getServerConfiguration(issuer)).thenReturn(mockServerConfig);
		Mockito.when(mockDynamicService.getServerConfiguration(issuer)).thenReturn(mockServerConfig);

		String badIssuer = "www.badexample.com";

		ServerConfiguration result = hybridService.getServerConfiguration(badIssuer);

		Mockito.verify(mockStaticService).getServerConfiguration(badIssuer);
		Mockito.verify(mockDynamicService).getServerConfiguration(badIssuer);
		assertThat(result, is(nullValue()));
	}
}
