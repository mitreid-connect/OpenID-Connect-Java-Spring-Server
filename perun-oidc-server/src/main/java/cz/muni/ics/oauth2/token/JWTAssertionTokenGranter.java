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
/**
 *
 */
package cz.muni.ics.oauth2.token;

import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import java.text.ParseException;

import cz.muni.ics.jwt.assertion.AssertionValidator;
import cz.muni.ics.oauth2.assertion.AssertionOAuth2RequestFactory;
import cz.muni.ics.oauth2.service.OAuth2TokenEntityService;
import cz.muni.ics.openid.connect.assertion.JWTBearerAssertionAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * @author jricher
 *
 */
@Component("jwtAssertionTokenGranter")
@Slf4j
public class JWTAssertionTokenGranter extends AbstractTokenGranter {

	private static final String grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer";

	@Autowired
	@Qualifier("jwtAssertionValidator")
	private AssertionValidator validator;

	@Autowired
	private AssertionOAuth2RequestFactory assertionFactory;

	@Autowired
	public JWTAssertionTokenGranter(OAuth2TokenEntityService tokenServices, ClientDetailsEntityService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getOAuth2Authentication(org.springframework.security.oauth2.provider.AuthorizationRequest)
	 */
	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) throws AuthenticationException, InvalidTokenException {
		// read and load up the existing token
		try {
			String incomingAssertionValue = tokenRequest.getRequestParameters().get("assertion");
			JWT assertion = JWTParser.parse(incomingAssertionValue);

			if (validator.isValid(assertion)) {

				// our validator says it's OK, time to make a token from it
				// the real work happens in the assertion factory and the token services
				return new OAuth2Authentication(assertionFactory.createOAuth2Request(client, tokenRequest, assertion),
						new JWTBearerAssertionAuthenticationToken(assertion, client.getAuthorities()));

			} else {
				log.warn("Incoming assertion did not pass validator, rejecting");
				return null;
			}

		} catch (ParseException e) {
			log.warn("Unable to parse incoming assertion");
		}

		// if we had made a token, we'd have returned it by now, so return null here to close out with no created token
		return null;

	}



}
