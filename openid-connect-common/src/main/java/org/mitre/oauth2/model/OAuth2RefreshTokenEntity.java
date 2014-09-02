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

import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import com.nimbusds.jwt.JWT;

/**
 * @author jricher
 *
 */
public interface OAuth2RefreshTokenEntity extends OAuth2RefreshToken {

	/**
	 * @return the id
	 */
	Long getId();

	/**
	 * @param id the id to set
	 */
	void setId(Long id);

	/**
	 * The authentication in place when the original access token was
	 * created
	 * 
	 * @return the authentication
	 */
	AuthenticationHolderEntity getAuthenticationHolder();

	/**
	 * @param authentication the authentication to set
	 */
	void setAuthenticationHolder(AuthenticationHolderEntity authenticationHolder);

	/**
	 * Set the value of this token as a string. Parses the string into a JWT.
	 * @param value
	 * @throws ParseException if the value is not a valid JWT string
	 */
	void setValue(String value) throws ParseException;

	Date getExpiration();

	void setExpiration(Date expiration);

	/**
	 * Has this token expired?
	 * @return true if it has a timeout set and the timeout has passed
	 */
	boolean isExpired();

	/**
	 * @return the client
	 */
	ClientDetailsEntity getClient();

	/**
	 * @param client the client to set
	 */
	void setClient(ClientDetailsEntity client);

	/**
	 * Get the JWT object directly
	 * @return the jwt
	 */
	JWT getJwt();

	/**
	 * @param jwt the jwt to set
	 */
	void setJwt(JWT jwt);
	
}
