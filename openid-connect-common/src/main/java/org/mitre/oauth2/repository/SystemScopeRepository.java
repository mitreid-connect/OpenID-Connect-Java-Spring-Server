/**
 * 
 */
package org.mitre.oauth2.repository;

import java.util.Set;

import org.mitre.oauth2.model.SystemScope;

/**
 * @author jricher
 *
 */
public interface SystemScopeRepository {

	public Set<SystemScope> getAll();

	public SystemScope getById(Long id);

	public SystemScope getByValue(String value);

	public void remove(SystemScope scope);

	public SystemScope save(SystemScope scope);

}
