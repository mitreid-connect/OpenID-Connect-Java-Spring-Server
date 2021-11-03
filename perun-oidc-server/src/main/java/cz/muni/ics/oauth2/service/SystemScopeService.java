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
package cz.muni.ics.oauth2.service;

import cz.muni.ics.oauth2.model.SystemScope;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jricher
 */
public interface SystemScopeService {

	String OFFLINE_ACCESS = "offline_access";
	String OPENID_SCOPE = "openid";
	String REGISTRATION_TOKEN_SCOPE = "registration-token"; // this scope manages dynamic client registrations
	String RESOURCE_TOKEN_SCOPE = "resource-token"; // this scope manages client-style protected resources
	String UMA_PROTECTION_SCOPE = "uma_protection";
	String UMA_AUTHORIZATION_SCOPE = "uma_authorization";

	Set<SystemScope> reservedScopes = new HashSet<>(
		Arrays.asList(new SystemScope(REGISTRATION_TOKEN_SCOPE), new SystemScope(RESOURCE_TOKEN_SCOPE)));

	Set<SystemScope> getAll();

	/**
	 * Get all scopes that are defaulted to new clients on this system
	 * @return
	 */
	Set<SystemScope> getDefaults();

	/**
	 * Get all the reserved system scopes. These can't be used
	 * by clients directly, but are instead tied to special system
	 * tokens like id tokens and registration access tokens.
	 *
	 * @return
	 */
	Set<SystemScope> getReserved();

	/**
	 * Get all the registered scopes that are restricted.
	 * @return
	 */
	Set<SystemScope> getRestricted();

	/**
	 * Get all the registered scopes that aren't restricted.
	 * @return
	 */
	Set<SystemScope> getUnrestricted();

	SystemScope getById(Long id);

	SystemScope getByValue(String value);

	void remove(SystemScope scope);

	SystemScope save(SystemScope scope);

	/**
	 * Translate the set of scope strings into a set of SystemScope objects.
	 * @param scope
	 * @return
	 */
	Set<SystemScope> fromStrings(Set<String> scope);

	/**
	 * Pluck the scope values from the set of SystemScope objects and return a list of strings
	 * @param scope
	 * @return
	 */
	Set<String> toStrings(Set<SystemScope> scope);

	/**
	 * Test whether the scopes in both sets are compatible. All scopes in "actual" must exist in "expected".
	 */
	boolean scopesMatch(Set<String> expected, Set<String> actual);

	/**
	 * Remove any system-reserved or registered restricted scopes from the
	 * set and return the result.
	 * @param scopes
	 * @return
	 */
	Set<SystemScope> removeRestrictedAndReservedScopes(Set<SystemScope> scopes);

	/**
	 * Remove any system-reserved scopes from the set and return the result.
	 * @param scopes
	 * @return
	 */
	Set<SystemScope> removeReservedScopes(Set<SystemScope> scopes);

}
