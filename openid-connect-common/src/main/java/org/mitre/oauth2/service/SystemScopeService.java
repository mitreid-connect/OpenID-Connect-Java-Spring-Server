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
/**
 * 
 */
package org.mitre.oauth2.service;

import java.util.Set;

import org.mitre.oauth2.model.SystemScope;

/**
 * @author jricher
 *
 */
public interface SystemScopeService {

	public Set<SystemScope> getAll();

	/**
	 * Get all scopes that are defaulted to new clients on this system
	 * @return
	 */
	public Set<SystemScope> getDefaults();

	/**
	 * Get all scopes that are allowed for dynamic registration on this system
	 * @return
	 */
	public Set<SystemScope> getDynReg();

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
	 * Test whether the scopes in both sets are compatible, with special
	 * processing for structured scopes. All scopes in "actual" must exist in
	 * "expected". If a scope in "expected" is structured and has a value, it
	 * must be matched exactly by its corresponding scope in "actual". If a
	 * scope in "expected" is structured but has no value, it may be matched by
	 * a scope with or without a value in "actual".
	 */
	public boolean scopesMatch(Set<String> expected, Set<String> actual);
}
