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

}
