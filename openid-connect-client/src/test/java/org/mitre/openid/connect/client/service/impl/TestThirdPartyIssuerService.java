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

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.junit.Assert.assertThat;

/**
 * @author wkim
 *
 */
public class TestThirdPartyIssuerService {

	// Test fixture:
	private HttpServletRequest request;

	private String iss = "https://server.example.org";
	private String login_hint = "I'm not telling you nothin!";
	private String target_link_uri = "https://www.example.com";
	private String redirect_uri = "https://www.example.com";

	private String accountChooserUrl = "https://www.example.com/account";

	private ThirdPartyIssuerService service = new ThirdPartyIssuerService();

	@Before
	public void prepare() {

		service.setAccountChooserUrl(accountChooserUrl);

		request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("iss")).thenReturn(iss);
		Mockito.when(request.getParameter("login_hint")).thenReturn(login_hint);
		Mockito.when(request.getParameter("target_link_uri")).thenReturn(target_link_uri);
		Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer(redirect_uri));
	}

	@Test
	public void getIssuer_hasIssuer() {

		IssuerServiceResponse response = service.getIssuer(request);

		assertThat(response.getIssuer(), equalTo(iss));
		assertThat(response.getLoginHint(), equalTo(login_hint));
		assertThat(response.getTargetLinkUri(), equalTo(target_link_uri));

		assertThat(response.getRedirectUrl(), nullValue());
	}

	@Test
	public void getIssuer_noIssuer() {

		Mockito.when(request.getParameter("iss")).thenReturn(null);

		IssuerServiceResponse response = service.getIssuer(request);

		assertThat(response.getIssuer(), nullValue());
		assertThat(response.getLoginHint(), nullValue());
		assertThat(response.getTargetLinkUri(), nullValue());

		String expectedRedirectUrl = accountChooserUrl + "?redirect_uri=" + "https%3A%2F%2Fwww.example.com"; // url-encoded string of the request url
		assertThat(response.getRedirectUrl(), equalTo(expectedRedirectUrl));
	}

	@Test
	public void getIssuer_isWhitelisted() {

		service.setWhitelist(Sets.newHashSet(iss));

		IssuerServiceResponse response = service.getIssuer(request);

		assertThat(response.getIssuer(), equalTo(iss));
		assertThat(response.getLoginHint(), equalTo(login_hint));
		assertThat(response.getTargetLinkUri(), equalTo(target_link_uri));

		assertThat(response.getRedirectUrl(), nullValue());
	}

	@Test(expected = AuthenticationServiceException.class)
	public void getIssuer_notWhitelisted() {

		service.setWhitelist(Sets.newHashSet("some.other.site"));

		service.getIssuer(request);
	}

	@Test(expected = AuthenticationServiceException.class)
	public void getIssuer_blacklisted() {

		service.setBlacklist(Sets.newHashSet(iss));

		service.getIssuer(request);
	}

	@Test(expected = AuthenticationServiceException.class)
	public void getIssuer_badUri() {

		Mockito.when(request.getParameter("iss")).thenReturn(null);
		service.setAccountChooserUrl("e=mc^2");

		service.getIssuer(request);
	}
}
