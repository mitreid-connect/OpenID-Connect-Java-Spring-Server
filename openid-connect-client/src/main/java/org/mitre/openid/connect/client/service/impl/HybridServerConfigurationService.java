/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.Map;
import java.util.Set;

import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * Houses both a static server configuration and a dynamic server configuration
 * service in one object. Checks the static service first, then falls through to
 * the dynamic service.
 * 
 * Provides configuration passthrough to the dynamic service's whitelist and blacklist, 
 * and to the static service's server map.
 * 
 * 
 * @author jricher
 *
 */
public class HybridServerConfigurationService implements ServerConfigurationService {
	
	private StaticServerConfigurationService staticServerService;
	
	private DynamicServerConfigurationService dynamicServerService;
	

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.ServerConfigurationService#getServerConfiguration(java.lang.String)
	 */
	@Override
	public ServerConfiguration getServerConfiguration(String issuer) {
		ServerConfiguration server = staticServerService.getServerConfiguration(issuer);
		if (server != null) {
			return server;
		} else {
			return dynamicServerService.getServerConfiguration(issuer);
		}
	}


	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.StaticServerConfigurationService#getServers()
	 */
    public Map<String, ServerConfiguration> getServers() {
	    return staticServerService.getServers();
    }


	/**
	 * @param servers
	 * @see org.mitre.openid.connect.client.service.impl.StaticServerConfigurationService#setServers(java.util.Map)
	 */
    public void setServers(Map<String, ServerConfiguration> servers) {
	    staticServerService.setServers(servers);
    }


	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService#getWhitelist()
	 */
    public Set<String> getWhitelist() {
	    return dynamicServerService.getWhitelist();
    }


	/**
	 * @param whitelist
	 * @see org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService#setWhitelist(java.util.Set)
	 */
    public void setWhitelist(Set<String> whitelist) {
	    dynamicServerService.setWhitelist(whitelist);
    }


	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService#getBlacklist()
	 */
    public Set<String> getBlacklist() {
	    return dynamicServerService.getBlacklist();
    }


	/**
	 * @param blacklist
	 * @see org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService#setBlacklist(java.util.Set)
	 */
    public void setBlacklist(Set<String> blacklist) {
	    dynamicServerService.setBlacklist(blacklist);
    }

}
