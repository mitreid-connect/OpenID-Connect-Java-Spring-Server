/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSignedAuthRequestUrlBuilder {
	
	// Test fixture:
	ServerConfiguration serverConfig;
	RegisteredClient clientConfig;
	
	@Mock
	private JwtSigningAndValidationService signingAndValidationService;
	
	@InjectMocks
	private SignedAuthRequestUrlBuilder urlBuilder = new SignedAuthRequestUrlBuilder();

	@Before
	public void prepare() {
		
		Mockito.reset(signingAndValidationService);
		
		serverConfig = Mockito.mock(ServerConfiguration.class);
		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("https://server.example.com/authorize");
		
		clientConfig = Mockito.mock(RegisteredClient.class);
		Mockito.when(clientConfig.getClientId()).thenReturn("s6BhdRkqt3");
		Mockito.when(clientConfig.getScope()).thenReturn(Sets.newHashSet("openid", "profile"));
	}
	
	@Test //TODO
	public void test() {
		assertTrue("Not yet implemented", true);
	}

}
