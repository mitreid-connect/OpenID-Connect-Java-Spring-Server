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

import java.util.ArrayList;
import java.util.Collection;

import org.mitre.openid.connect.model.IdToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * 
 * @author Michael Walsh, Justin Richer
 * 
 */
public class OpenIdConnectAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 22100073066377804L;
    
	private final Object principle;
	private final String idTokenValue; // string representation of the id token
	private final String accessTokenValue; // string representation of the access token
	private final String refreshTokenValue; // string representation of the refresh token
	private final String userId; // user id (parsed from the id token)

	/**
	 * Constructs OpenIdConnectAuthenticationToken provided
	 * 
	 * @param principle
	 * @param authorities
	 * @param userId
	 * @param idToken
	 */
	public OpenIdConnectAuthenticationToken(Object principle,
			Collection<? extends GrantedAuthority> authorities, String userId,
			String idTokenValue, String accessTokenValue, String refreshTokenValue) {

		super(authorities);

		this.principle = principle;
		this.userId = userId;
		this.idTokenValue = idTokenValue;
		this.accessTokenValue = accessTokenValue;
		this.refreshTokenValue = refreshTokenValue;

		setAuthenticated(true);
	}

	/**
	 * Constructs OpenIdConnectAuthenticationToken provided
	 * 
	 * @param idToken
	 * @param userId
	 */
	public OpenIdConnectAuthenticationToken(String userId, String idTokenValue, String accessTokenValue, String refreshTokenValue) {

		super(new ArrayList<GrantedAuthority>(0));

		this.principle = userId;
		this.userId = userId;
		this.idTokenValue = idTokenValue;
		this.accessTokenValue = accessTokenValue;
		this.refreshTokenValue = refreshTokenValue;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return principle;
	}

	public String getUserId() {
		return userId;
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
	
	
}