/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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
package org.mitre.openid.connect.client;

import java.util.Collection;

import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.PendingOIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;

/**
 * @author nemonik, Justin Richer
 *
 */
public class OIDCAuthenticationProvider implements AuthenticationProvider {

	private static Logger logger = LoggerFactory.getLogger(OIDCAuthenticationProvider.class);

	private UserInfoFetcher userInfoFetcher = new UserInfoFetcher();

	private OIDCAuthoritiesMapper authoritiesMapper = new NamedAdminAuthoritiesMapper();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

		if (!supports(authentication.getClass())) {
			return null;
		}

		if (authentication instanceof PendingOIDCAuthenticationToken) {

			PendingOIDCAuthenticationToken token = (PendingOIDCAuthenticationToken) authentication;

			// get the ID Token value out
			JWT idToken = token.getIdToken();

			// load the user info if we can
			UserInfo userInfo = userInfoFetcher.loadUserInfo(token);

			if (userInfo == null) {
				// user info not found -- could be an error, could be fine
			} else {
				// if we found userinfo, double check it
				if (!Strings.isNullOrEmpty(userInfo.getSub()) && !userInfo.getSub().equals(token.getSub())) {
					// the userinfo came back and the user_id fields don't match what was in the id_token
					throw new UsernameNotFoundException("user_id mismatch between id_token and user_info call: " + token.getSub() + " / " + userInfo.getSub());
				}
			}

			return createAuthenticationToken(token, authoritiesMapper.mapAuthorities(idToken, userInfo), userInfo);
		}

		return null;
	}

	/**
	 * Override this function to return a different kind of Authentication, processes the authorities differently,
	 * or do post-processing based on the UserInfo object.
	 *
	 * @param token
	 * @param authorities
	 * @param userInfo
	 * @return
	 */
	protected Authentication createAuthenticationToken(PendingOIDCAuthenticationToken token, Collection<? extends GrantedAuthority> authorities, UserInfo userInfo) {
		return new OIDCAuthenticationToken(token.getSub(),
				token.getIssuer(),
				userInfo, authorities,
				token.getIdToken(), token.getAccessTokenValue(), token.getRefreshTokenValue());
	}

	/**
	 * @param userInfoFetcher
	 */
	public void setUserInfoFetcher(UserInfoFetcher userInfoFetcher) {
		this.userInfoFetcher = userInfoFetcher;
	}

	/**
	 * @param authoritiesMapper
	 */
	public void setAuthoritiesMapper(OIDCAuthoritiesMapper authoritiesMapper) {
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
		return PendingOIDCAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
