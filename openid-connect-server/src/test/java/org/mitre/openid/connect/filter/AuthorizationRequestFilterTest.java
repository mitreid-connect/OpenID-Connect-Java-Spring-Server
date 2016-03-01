/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
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
/**
 *
 */
package org.mitre.openid.connect.filter;

import org.junit.Before;
import org.junit.Test;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * @author dpaniagua
 */
public class AuthorizationRequestFilterTest {

	@InjectMocks
	private AuthorizationRequestFilter authorizationRequestFilter;

	@Mock
	private OAuth2RequestFactory oAuth2RequestFactory;

	@Mock
	private ClientDetails clientDetails;

	@Mock
	private ClientDetailsEntityService clientDetailsService;

	@Mock
	private FilterChain springSecurityFilterChain;

	@Mock
	AuthorizationRequest authorizationRequest;

	ArgumentCaptor<Map> argumentCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		argumentCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.when(oAuth2RequestFactory.createAuthorizationRequest(argumentCaptor.capture())).thenReturn
				(authorizationRequest);
	}

	@Test()
	public void testDoFilter_outsideRootServletPath() throws Exception {

		// given
		String baseUrl = "https://server.example.com/oidc/authorize";

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(baseUrl);
		requestBuilder.servletPath("/oidc")
				.param("response_type", "code")
				.param("scope", "openid")
				.param("redirect_uri", "https://client.example.org/");
		MockHttpServletRequest request = requestBuilder.buildRequest(null);

		//when
		authorizationRequestFilter.doFilter(request, null, springSecurityFilterChain);
		//then
		ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(oAuth2RequestFactory, times(1)).createAuthorizationRequest(argumentCaptor.capture());
		Mockito.verify(springSecurityFilterChain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse
				.class));
	}

	@Test()
	public void testDoFilter_RootServletPath() throws Exception {

		// given
		// Values Taken from spec sample: http://openid.net/specs/openid-connect-core-1_0.html
		String baseUrl = "https://server.example.com/authorize";

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(baseUrl);

		requestBuilder.servletPath("/authorize")
				.param("response_type", "code")
				.param("scope", "openid")
				.param("redirect_uri", "https://client.example.org/");
		MockHttpServletRequest request = requestBuilder.buildRequest(null);

		//when
		authorizationRequestFilter.doFilter(request, null, springSecurityFilterChain);

		//then
		assertThat(request.getServletPath(), is(equalTo("/authorize")));
		Mockito.verify(oAuth2RequestFactory, times(1)).createAuthorizationRequest(any(Map.class));
		Mockito.verify(springSecurityFilterChain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse
				.class));
	}

	@Test()
	public void testDoFilter_withInValidUrl() throws Exception {

		// given
		String baseUrl = "https://server.example.com/authorize/something/else";

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(baseUrl);
		requestBuilder.param("response_type", "code")
				.param("scope", "openid")
				.servletPath("/authorize")
				.param("redirect_uri", "https://client.example.org/");
		MockHttpServletRequest request = requestBuilder.buildRequest(null);

		//when
		authorizationRequestFilter.doFilter(request, null, springSecurityFilterChain);

		//then
		Mockito.verify(oAuth2RequestFactory, times(1)).createAuthorizationRequest(any(Map.class));
		Mockito.verify(springSecurityFilterChain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse
				.class));
	}
}