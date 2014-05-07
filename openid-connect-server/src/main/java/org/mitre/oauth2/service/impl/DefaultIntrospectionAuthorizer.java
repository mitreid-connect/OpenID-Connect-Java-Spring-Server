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
package org.mitre.oauth2.service.impl;

import java.util.Set;

import org.mitre.oauth2.service.IntrospectionAuthorizer;
import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Service;

@Service
public class DefaultIntrospectionAuthorizer implements IntrospectionAuthorizer {

	@Autowired
	private SystemScopeService scopeService;

	@Override
	public boolean isIntrospectionPermitted(ClientDetails authClient,
			ClientDetails tokenClient, Set<String> tokenScope) {
		// permit introspection if it's the same client that the token was
		// issued to, or it at least has all the scopes the token was issued
		// with
		return authClient.getClientId().equals(tokenClient.getClientId())
				|| scopeService.scopesMatch(authClient.getScope(), tokenScope);
	}

}
