/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
package cz.muni.ics.openid.connect.service;

import com.nimbusds.jwt.JWT;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import java.util.Date;
import org.springframework.security.oauth2.provider.OAuth2Request;

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
	JWT createIdToken(ClientDetailsEntity client, OAuth2Request request, Date issueTime, String sub,
					  OAuth2AccessTokenEntity accessToken);

	/**
	 * Create a registration access token for the given client.
	 *
	 * @param client
	 * @return
	 */
	OAuth2AccessTokenEntity createRegistrationAccessToken(ClientDetailsEntity client);

	/**
	 * Create a resource access token for the given client (protected resource).
	 *
	 * @param client
	 * @return
	 */
	OAuth2AccessTokenEntity createResourceAccessToken(ClientDetailsEntity client);

	/**
	 * Rotate the registration or resource token for a client
	 * @param client
	 * @return
	 */
	OAuth2AccessTokenEntity rotateRegistrationAccessTokenForClient(ClientDetailsEntity client);

}
