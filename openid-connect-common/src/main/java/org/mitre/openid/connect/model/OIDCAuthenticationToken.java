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
package org.mitre.openid.connect.model;

import java.util.ArrayList;
import java.util.Collection;

import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * @author Michael Walsh, Justin Richer
 * 
 */
public class OIDCAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 22100073066377804L;

	private final ImmutableMap<String, String> principal;
	private final String idTokenValue; // string representation of the id token
	private final String accessTokenValue; // string representation of the access token
	private final String refreshTokenValue; // string representation of the refresh token
	private final String issuer; // issuer URL (parsed from the id token)
	private final String sub; // user id (parsed from the id token)

	private final transient ServerConfiguration serverConfiguration; // server configuration used to fulfill this token, don't serialize it
	private final transient UserInfo userInfo; // user info container, don't serialize it b/c it might be huge and can be re-fetched

	/**
	 * Constructs OIDCAuthenticationToken with a full set of authorities, marking this as authenticated.
	 * 
	 * Set to authenticated.
	 * 
	 * Constructs a Principal out of the subject and issuer.
	 * @param subject
	 * @param authorities
	 * @param principal
	 * @param idToken
	 */
	public OIDCAuthenticationToken(String subject, String issuer,
			UserInfo userInfo, Collection<? extends GrantedAuthority> authorities,
			String idTokenValue, String accessTokenValue, String refreshTokenValue) {

		super(authorities);

		this.principal = ImmutableMap.of("sub", subject, "iss", issuer);
		this.userInfo = userInfo;
		this.sub = subject;
		this.issuer = issuer;
		this.idTokenValue = idTokenValue;
		this.accessTokenValue = accessTokenValue;
		this.refreshTokenValue = refreshTokenValue;

		this.serverConfiguration = null; // we don't need a server config anymore

		setAuthenticated(true);
	}

	/**
	 * Constructs OIDCAuthenticationToken for use as a data shuttle from the filter to the auth provider.
	 * 
	 * Set to not-authenticated.
	 * 
	 * Constructs a Principal out of the subject and issuer.
	 * @param sub
	 * @param idToken
	 */
	public OIDCAuthenticationToken(String subject, String issuer,
			ServerConfiguration serverConfiguration,
			String idTokenValue, String accessTokenValue, String refreshTokenValue) {

		super(new ArrayList<GrantedAuthority>(0));

		this.principal = ImmutableMap.of("sub", subject, "iss", issuer);
		this.sub = subject;
		this.issuer = issuer;
		this.idTokenValue = idTokenValue;
		this.accessTokenValue = accessTokenValue;
		this.refreshTokenValue = refreshTokenValue;

		this.userInfo = null; // we don't have a UserInfo yet

		this.serverConfiguration = serverConfiguration;


		setAuthenticated(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return accessTokenValue;
	}

	/**
	 * Get the principal of this object, an immutable map of the subject and issuer.
	 */
	@Override
	public Object getPrincipal() {
		return principal;
	}

	public String getSub() {
		return sub;
	}

	/**
	 * @return the idTokenValue
	 */
	public String getIdTokenValue() {
		return idTokenValue;
	}

	/**
	 * @return the accessTokenValue
	 */
	public String getAccessTokenValue() {
		return accessTokenValue;
	}

	/**
	 * @return the refreshTokenValue
	 */
	public String getRefreshTokenValue() {
		return refreshTokenValue;
	}

	/**
	 * @return the serverConfiguration
	 */
	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}

	/**
	 * @return the userInfo
	 */
	public UserInfo getUserInfo() {
		return userInfo;
	}


}
