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
package org.mitre.openid.connect.client;

import java.util.Collection;

import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * @author nemonik
 * 
 */
public class OIDCAuthenticationProvider implements AuthenticationProvider {

	private UserInfoFetcher userInfoFetcher = new UserInfoFetcher();

	private GrantedAuthoritiesMapper authoritiesMapper = new NamedAdminAuthoritiesMapper();

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

		if (authentication instanceof OIDCAuthenticationToken) {

			OIDCAuthenticationToken token = (OIDCAuthenticationToken) authentication;

			Collection<SubjectIssuerGrantedAuthority> authorities = Lists.newArrayList(new SubjectIssuerGrantedAuthority(token.getSub(), token.getIssuer()));

			UserInfo userInfo = userInfoFetcher.loadUserInfo(token);

			if (userInfo == null) {
				// TODO: user Info not found -- error?
			} else {
				if (!Strings.isNullOrEmpty(userInfo.getSub()) && !userInfo.getSub().equals(token.getSub())) {
					// the userinfo came back and the user_id fields don't match what was in the id_token
					throw new UsernameNotFoundException("user_id mismatch between id_token and user_info call: " + token.getSub() + " / " + userInfo.getSub());
				}
			}

			return new OIDCAuthenticationToken(token.getSub(),
					token.getIssuer(),
					userInfo, authoritiesMapper.mapAuthorities(authorities),
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.authentication.AuthenticationProvider#supports
	 * (java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return OIDCAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
