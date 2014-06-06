/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
package org.mitre.oauth2.token;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.impl.DefaultClientUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;

import com.google.common.base.Strings;

/**
 * Filter to check for public clients at the token endpoint
 * 
 * @author jmandel
 * 
 */
public class PublicClientTokenEndpointFilter extends ClientCredentialsTokenEndpointFilter {
	@Autowired
	ClientDetailsEntityService clientDetails;
	
	@Autowired
	DefaultClientUserDetailsService clientUserDetailsService;

	/**
	 * Check to see if if this is a public client getting a token with no authentication.
	 * We should only allow public clients to access the /token endpoint when:
	 * 1. they ask for "No Authentication" (AuthMethod.NONE) and 
	 * 2. they haven't, in fact, supplied any authentication information
	 */
	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {	

		if (!Strings.isNullOrEmpty(request.getParameter("client_secret"))){
			return false;
		}
		if (!Strings.isNullOrEmpty(request.getHeader("Authorization"))){
			return false;
		}
		
		ClientDetailsEntity client = getClientForRequest(request);
		if (client == null){
			return false;
		}
		if (client.getTokenEndpointAuthMethod() != ClientDetailsEntity.AuthMethod.NONE){
			return false;
		}

		return super.requiresAuthentication(request, response);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {
        return new UsernamePasswordAuthenticationToken(getClientUserDetails(request), null, null);
	}

	private ClientDetailsEntity getClientForRequest(HttpServletRequest request) {
		String clientId = request.getParameter("client_id");
		if (Strings.isNullOrEmpty(clientId)){
			return null;
		}
		clientId = clientId.trim();
		return clientDetails.loadClientByClientId(clientId);
	}

	private UserDetails getClientUserDetails(HttpServletRequest request) {
		String clientId = request.getParameter("client_id");
		if (Strings.isNullOrEmpty(clientId)){
			return null;
		}
		return clientUserDetailsService.loadUserByUsername(clientId);
	}

}
