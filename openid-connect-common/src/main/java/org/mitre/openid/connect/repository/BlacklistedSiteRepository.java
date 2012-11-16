/**
 * 
 */
package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.openid.connect.model.BlacklistedSite;

/**
 * @author jricher
 *
 */
public interface BlacklistedSiteRepository {

	public Collection<BlacklistedSite> getAll();
	
	public BlacklistedSite getById(Long id);
	
	public void remove(BlacklistedSite blacklistedSite);
	
	public BlacklistedSite save(BlacklistedSite blacklistedSite);
	
	public BlacklistedSite update(BlacklistedSite oldBlacklistedSite, BlacklistedSite blacklistedSite);
	
}
