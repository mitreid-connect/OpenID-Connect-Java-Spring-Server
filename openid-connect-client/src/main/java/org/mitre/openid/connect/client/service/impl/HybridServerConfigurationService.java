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

	private StaticServerConfigurationService staticServerService = new StaticServerConfigurationService();

	private DynamicServerConfigurationService dynamicServerService = new DynamicServerConfigurationService();


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
