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
	
	public Set<SystemScope> getDefaults();
	
	public Set<SystemScope> getDynReg();

	public SystemScope getById(Long id);
	
	public SystemScope getByValue(String value);
	
	public void remove(SystemScope scope);
	
	public SystemScope save(SystemScope scope);
	
}
