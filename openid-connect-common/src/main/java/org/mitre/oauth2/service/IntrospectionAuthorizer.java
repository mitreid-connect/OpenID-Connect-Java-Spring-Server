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
package org.mitre.oauth2.service;

import java.util.Set;

import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * Strategy interface used for authorizing token introspection.
 */
public interface IntrospectionAuthorizer {

	/**
	 * @param authClient the authenticated client wanting to perform token introspection
	 * @param tokenClient the client the token was issued to
	 * @param tokenScope the scope associated with the token
	 * @return {@code true} in case introspection is permitted; {@code false} otherwise
	 */
	boolean isIntrospectionPermitted(ClientDetails authClient, ClientDetails tokenClient, Set<String> tokenScope);

}
