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
/**
 * 
 */
package org.mitre.openid.connect.assertion;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * Filter to check client authentication via JWT Bearer assertions.
 * 
 * @author jricher
 *
 */
public class JwtBearerClientAssertionTokenEndpointFilter extends ClientCredentialsTokenEndpointFilter {

	public JwtBearerClientAssertionTokenEndpointFilter() {
		super();
	}

	public JwtBearerClientAssertionTokenEndpointFilter(String path) {
		super(path);
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

			Authentication authRequest = new JwtBearerAssertionAuthenticationToken(clientId, jwt);

			return this.getAuthenticationManager().authenticate(authRequest);
		} catch (ParseException e) {
			throw new BadCredentialsException("Invalid JWT credential: " + assertion);
		}
	}

	/**
	 * Check to see if the "client_assertion_type" and "client_assertion" parameters are present and contain the right values.
	 */
	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		// check for appropriate parameters
		String assertionType = request.getParameter("client_assertion_type");
		String assertion = request.getParameter("client_assertion");

		if (Strings.isNullOrEmpty(assertionType) || Strings.isNullOrEmpty(assertion)) {
			return false;
		} else if (!assertionType.equals("urn:ietf:params:oauth:client-assertion-type:jwt-bearer")) {
			return false;
		}


		// Can't call to superclass here b/c client creds would break for lack of client_id
		//	    return super.requiresAuthentication(request, response);

		String uri = request.getRequestURI();
		int pathParamIndex = uri.indexOf(';');

		if (pathParamIndex > 0) {
			// strip everything after the first semi-colon
			uri = uri.substring(0, pathParamIndex);
		}

		if ("".equals(request.getContextPath())) {
			return uri.endsWith(getFilterProcessesUrl());
		}

		return uri.endsWith(request.getContextPath() + getFilterProcessesUrl());

	}




}
