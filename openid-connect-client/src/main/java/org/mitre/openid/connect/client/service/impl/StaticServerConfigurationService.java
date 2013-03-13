/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.Map;

import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.beans.factory.InitializingBean;

/**
 * Statically configured server configuration service that maps issuer URLs to server configurations to use at that issuer.
 * 
 * @author jricher
 *
 */
public class StaticServerConfigurationService implements ServerConfigurationService, InitializingBean {

	// map of issuer url -> server configuration information
	private Map<String, ServerConfiguration> servers;

	/**
	 * @return the servers
	 */
	public Map<String, ServerConfiguration> getServers() {
		return servers;
	}

	/**
	 * @param servers the servers to set
	 */
	public void setServers(Map<String, ServerConfiguration> servers) {
		this.servers = servers;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.ServerConfigurationService#getServerConfiguration(java.lang.String)
	 */
	@Override
	public ServerConfiguration getServerConfiguration(String issuer) {
		return servers.get(issuer);
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
    @Override
    public void afterPropertiesSet() throws Exception {
	    if (servers == null || servers.isEmpty()) {
	    	throw new IllegalArgumentException("Servers map cannot be null or empty.");
	    }
	    
    }

}
