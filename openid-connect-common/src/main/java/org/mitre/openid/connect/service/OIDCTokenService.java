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
package org.mitre.openid.connect.service;

import java.util.Date;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.nimbusds.jose.JWSAlgorithm;

/**
 * Service to create specialty OpenID Connect tokens.
 * 
 * @author Amanda Anganes
 *
 */
public interface OIDCTokenService {

	/**
	 * Create an id token with the information provided.
	 * 
	 * @param client
	 * @param request
	 * @param issueTime
	 * @param sub
	 * @param signingAlg
	 * @param accessToken
	 * @return
	 */
	public OAuth2AccessTokenEntity createIdToken(
			ClientDetailsEntity client, OAuth2Request request, Date issueTime,
			String sub, JWSAlgorithm signingAlg,
			OAuth2AccessTokenEntity accessToken);

	/**
	 * Create a registration access token for the given client.
	 * 
	 * @param client
	 * @return
	 */
	public OAuth2AccessTokenEntity createRegistrationAccessToken(ClientDetailsEntity client);
	
}