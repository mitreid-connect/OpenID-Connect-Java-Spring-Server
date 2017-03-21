/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.oauth2.service;

import java.util.Set;

import org.mitre.oauth2.model.SystemScope;

import com.google.common.collect.Sets;

/**
 * @author jricher
 *
 */
public interface SystemScopeService {

	public static final String OFFLINE_ACCESS = "offline_access";
	public static final String OPENID_SCOPE = "openid";
	public static final String REGISTRATION_TOKEN_SCOPE = "registration-token"; // this scope manages dynamic client registrations
	public static final String RESOURCE_TOKEN_SCOPE = "resource-token"; // this scope manages client-style protected resources
	public static final String UMA_PROTECTION_SCOPE = "uma_protection";
	public static final String UMA_AUTHORIZATION_SCOPE = "uma_authorization";

	public static final Set<SystemScope> reservedScopes =
			Sets.newHashSet(
					new SystemScope(REGISTRATION_TOKEN_SCOPE),
					new SystemScope(RESOURCE_TOKEN_SCOPE)
					);

	public Set<SystemScope> getAll();

	/**
	 * Get all scopes that are defaulted to new clients on this system
	 * @return
	 */
	public Set<SystemScope> getDefaults();

	/**
	 * Get all the reserved system scopes. These can't be used
	 * by clients directly, but are instead tied to special system
	 * tokens like id tokens and registration access tokens.
	 *
	 * @return
	 */
	public Set<SystemScope> getReserved();

	/**
	 * Get all the registered scopes that are restricted.
	 * @return
	 */
	public Set<SystemScope> getRestricted();

	/**
	 * Get all the registered scopes that aren't restricted.
	 * @return
	 */
	public Set<SystemScope> getUnrestricted();

	public SystemScope getById(Long id);

	public SystemScope getByValue(String value);

	public void remove(SystemScope scope);

	public SystemScope save(SystemScope scope);

	/**
	 * Translate the set of scope strings into a set of SystemScope objects.
	 * @param scope
	 * @return
	 */
	public Set<SystemScope> fromStrings(Set<String> scope);

	/**
	 * Pluck the scope values from the set of SystemScope objects and return a list of strings
	 * @param scope
	 * @return
	 */
	public Set<String> toStrings(Set<SystemScope> scope);

	/**
	 * Test whether the scopes in both sets are compatible. All scopes in "actual" must exist in "expected".
	 */
	public boolean scopesMatch(Set<String> expected, Set<String> actual);

	/**
	 * Remove any system-reserved or registered restricted scopes from the
	 * set and return the result.
	 * @param scopes
	 * @return
	 */
	public Set<SystemScope> removeRestrictedAndReservedScopes(Set<SystemScope> scopes);

	/**
	 * Remove any system-reserved scopes from the set and return the result.
	 * @param scopes
	 * @return
	 */
	public Set<SystemScope> removeReservedScopes(Set<SystemScope> scopes);

}
