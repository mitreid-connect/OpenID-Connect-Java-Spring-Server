/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
package org.mitre.openid.connect.client;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * @author nemonik
 * 
 */
public class OpenIdConnectAuthenticationProvider implements
		AuthenticationProvider, InitializingBean {

	private AuthenticationUserDetailsService<OpenIdConnectAuthenticationToken> userDetailsService;
	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.userDetailsService,
				"The userDetailsService must be set");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication authentication)
			throws AuthenticationException {

		if (!supports(authentication.getClass())) {
			return null;
		}

		if (authentication instanceof OpenIdConnectAuthenticationToken) {

			OpenIdConnectAuthenticationToken token = (OpenIdConnectAuthenticationToken) authentication;

			UserDetails userDetails = userDetailsService.loadUserDetails(token);

			return new OpenIdConnectAuthenticationToken(userDetails,
					authoritiesMapper.mapAuthorities(userDetails.getAuthorities()), token.getUserId(),
					token.getIdTokenValue(), token.getAccessTokenValue(), token.getRefreshTokenValue());
		}

		return null;
	}

	/**
	 * @param authoritiesMapper
	 */
	public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
		this.authoritiesMapper = authoritiesMapper;
	}

	public void setUserDetailsService(
			AuthenticationUserDetailsService<OpenIdConnectAuthenticationToken> userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.authentication.AuthenticationProvider#supports
	 * (java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return OpenIdConnectAuthenticationToken.class
				.isAssignableFrom(authentication);
	}
}
