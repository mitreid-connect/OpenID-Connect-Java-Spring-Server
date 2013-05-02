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

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.nimbusds.jwt.JWT;

/**
 * @author jricher
 *
 */
public class JwtBearerAssertionAuthenticationToken extends AbstractAuthenticationToken {

	private String clientId;
	private JWT jwt;

	/**
	 * Create an unauthenticated token with the given client ID and jwt
	 * @param clientId
	 * @param jwt
	 */
	public JwtBearerAssertionAuthenticationToken(String clientId, JWT jwt) {
		super(null);
		this.clientId = clientId;
		this.jwt = jwt;
		setAuthenticated(false);
	}

	/**
	 * Create an authenticated token with the given clientID, jwt, and authorities set
	 * @param clientId
	 * @param jwt
	 * @param authorities
	 */
	public JwtBearerAssertionAuthenticationToken(String clientId, JWT jwt, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.clientId = clientId;
		this.jwt = jwt;
		setAuthenticated(true);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return jwt;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		return clientId;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the jwt
	 */
	public JWT getJwt() {
		return jwt;
	}

	/**
	 * @param jwt the jwt to set
	 */
	public void setJwt(JWT jwt) {
		this.jwt = jwt;
	}

	/**
	 * Clear out the JWT that this token holds.
	 */
	@Override
	public void eraseCredentials() {
		super.eraseCredentials();
		setJwt(null);
	}



}
