/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

package cz.muni.ics.oauth2.assertion.impl;

import com.google.common.collect.Sets;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import cz.muni.ics.oauth2.assertion.AssertionOAuth2RequestFactory;
import java.text.ParseException;
import java.util.Set;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;

/**
 * Takes an assertion from a trusted source, looks for the fields:
 *
 *  - scope, space-separated list of strings
 *  - aud, array of audience IDs
 *
 * @author jricher
 */
public class DirectCopyRequestFactory implements AssertionOAuth2RequestFactory {

	@Override
	public OAuth2Request createOAuth2Request(ClientDetails client, TokenRequest tokenRequest, JWT assertion) {
		try {
			JWTClaimsSet claims = assertion.getJWTClaimsSet();
			Set<String> scope = OAuth2Utils.parseParameterList(claims.getStringClaim("scope"));

			Set<String> resources = Sets.newHashSet(claims.getAudience());

			return new OAuth2Request(tokenRequest.getRequestParameters(), client.getClientId(),
				client.getAuthorities(), true, scope, resources, null,
				null, null);
		} catch (ParseException e) {
			return null;
		}
	}

}
