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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mockito.Mockito;

/**
 * @author wkim
 *
 */
public class TestWebfingerIssuerService {
	
	// Test fixture:
	private HttpServletRequest request;

	// WebfingerIssuerService.parameterName = "identifier";
	private String identifier = "user@example.org";

	private String loginPageUrl = "https://www.example.com/account";

	private WebfingerIssuerService service = new WebfingerIssuerService();

	@Before
	public void prepare() {
		
		BasicConfigurator.configure();

		service.setLoginPageUrl(loginPageUrl);

		request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("identifier")).thenReturn(identifier);
	}
	
	@After
	public void cleanup() {
		
		BasicConfigurator.resetConfiguration();
	}

	//@Test
	public void getIssuer_validIdentifier() {
		
		IssuerServiceResponse response = service.getIssuer(request);
		
		String expectedIssuer = "https://example.org/.well-known/webfinger";
		
		assertThat(response.getIssuer(), equalTo(expectedIssuer));
	}

}
