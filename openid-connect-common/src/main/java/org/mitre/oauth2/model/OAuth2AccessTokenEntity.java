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

package org.mitre.oauth2.model;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.nimbusds.jwt.JWT;

/**
 * @author jricher
 *
 */
public interface OAuth2AccessTokenEntity extends OAuth2AccessToken {

	/**
	 * @return the id
	 */
	Long getId();

	/**
	 * @param id the id to set
	 */
	void setId(Long id);

	/**
	 * The authentication in place when this token was created.
	 * @return the authentication
	 */
	AuthenticationHolderEntity getAuthenticationHolder();

	/**
	 * @param authentication the authentication to set
	 */
	void setAuthenticationHolder(AuthenticationHolderEntity authenticationHolder);

	/**
	 * @return the client
	 */
	ClientDetailsEntity getClient();

	/**
	 * @param client the client to set
	 */
	void setClient(ClientDetailsEntity client);

	/**
	 * Set the "value" of this Access Token
	 * 
	 * @param value the JWT string
	 * @throws ParseException if "value" is not a properly formatted JWT string
	 */
	void setValue(String value) throws ParseException;

	void setExpiration(Date expiration);

	void setTokenType(String tokenType);

	void setRefreshToken(OAuth2RefreshTokenEntity refreshToken);
	
	@Override
	OAuth2RefreshTokenEntity getRefreshToken();
	
	void setScope(Set<String> scope);

	/**
	 * @return the idToken
	 */
	OAuth2AccessTokenEntity getIdToken();

	/**
	 * @param idToken the idToken to set
	 */
	void setIdToken(OAuth2AccessTokenEntity idToken);

	/**
	 * @return the idTokenString
	 */
	String getIdTokenString();

	/**
	 * @return the jwtValue
	 */
	JWT getJwt();

	/**
	 * @param jwtValue the jwtValue to set
	 */
	void setJwt(JWT jwt);

}