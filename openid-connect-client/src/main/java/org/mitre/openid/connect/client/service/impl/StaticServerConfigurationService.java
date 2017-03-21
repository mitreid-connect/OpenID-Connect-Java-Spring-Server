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

import javax.annotation.PostConstruct;

import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * Statically configured server configuration service that maps issuer URLs to server configurations to use at that issuer.
 *
 * @author jricher
 *
 */
public class StaticServerConfigurationService implements ServerConfigurationService {

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

	@PostConstruct
	public void afterPropertiesSet() {
		if (servers == null || servers.isEmpty()) {
			throw new IllegalArgumentException("Servers map cannot be null or empty.");
		}

	}

}
