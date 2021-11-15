/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
/**
 *
 */
package cz.muni.ics.openid.connect.assertion;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import java.io.IOException;
import java.text.ParseException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.BadClientCredentialsException;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Filter to check client authentication via JWT Bearer assertions.
 *
 * @author jricher
 *
 */
public class JWTBearerClientAssertionTokenEndpointFilter extends AbstractAuthenticationProcessingFilter {

	private final AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();

	public JWTBearerClientAssertionTokenEndpointFilter(RequestMatcher additionalMatcher) {
		super(new ClientAssertionRequestMatcher(additionalMatcher));
		// If authentication fails the type is "Form"
		((OAuth2AuthenticationEntryPoint) authenticationEntryPoint).setTypeName("Form");
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				if (exception instanceof BadCredentialsException) {
					exception = new BadCredentialsException(exception.getMessage(), new BadClientCredentialsException());
				}
				authenticationEntryPoint.commence(request, response, exception);
			}
		});
		setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				// no-op - just allow filter chain to continue to token endpoint
			}
		});
	}

	/**
	 * Pull the assertion out of the request and send it up to the auth manager for processing.
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

		// check for appropriate parameters
		String assertionType = request.getParameter("client_assertion_type");
		String assertion = request.getParameter("client_assertion");

		try {
			JWT jwt = JWTParser.parse(assertion);

			String clientId = jwt.getJWTClaimsSet().getSubject();

			Authentication authRequest = new JWTBearerAssertionAuthenticationToken(jwt);

			return this.getAuthenticationManager().authenticate(authRequest);
		} catch (ParseException e) {
			throw new BadCredentialsException("Invalid JWT credential: " + assertion);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain, Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
		chain.doFilter(request, response);
	}

	private static class ClientAssertionRequestMatcher implements RequestMatcher {

		private final RequestMatcher additionalMatcher;

		public ClientAssertionRequestMatcher(RequestMatcher additionalMatcher) {
			this.additionalMatcher = additionalMatcher;
		}

		@Override
		public boolean matches(HttpServletRequest request) {
			// check for appropriate parameters
			String assertionType = request.getParameter("client_assertion_type");
			String assertion = request.getParameter("client_assertion");

			if (Strings.isNullOrEmpty(assertionType) || Strings.isNullOrEmpty(assertion)) {
				return false;
			} else if (!assertionType.equals("urn:ietf:params:oauth:client-assertion-type:jwt-bearer")) {
				return false;
			}

			return additionalMatcher.matches(request);
		}

	}



}
