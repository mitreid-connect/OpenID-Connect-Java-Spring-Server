/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.Map;

import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * Houses both a static server configuration and a dynamic server configuration
 * service in one object. Checks the static service first, then falls through to
 * the dynamic service.
 * 
 * Provides configuration passthrough for the template and the static
 * client map.
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

}
