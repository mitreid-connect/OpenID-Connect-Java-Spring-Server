/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/

package org.mitre.uma.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.google.common.collect.ImmutableSet;

/**
 * 
 * Utility class to enforce OAuth scopes in authenticated requests.
 * 
 * @author jricher
 *
 */
public abstract class OAuthScopeEnforcementUtilities {
	
	/**
	 * Makes sure the authentication contains the given scope, throws an exception otherwise
	 * @param auth the authentication object to check
	 * @param scope TODO
	 * @param scope the scope to look for
	 * @throws InsufficientScopeException if the authentication does not contain that scope
	 */
	public static void ensureOAuthScope(Authentication auth, String scope) {
		// if auth is OAuth, make sure we've got the right scope
		if (auth instanceof OAuth2Authentication) {
			OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) auth;
			if (oAuth2Authentication.getOAuth2Request().getScope() == null
					|| !oAuth2Authentication.getOAuth2Request().getScope().contains(scope)) {
				throw new InsufficientScopeException("Insufficient scope", ImmutableSet.of(scope));
			}
		}
	}
	

}
