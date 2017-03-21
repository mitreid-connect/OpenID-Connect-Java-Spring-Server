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

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * Client configuration service that holds a static map from issuer URL to a ClientDetails object to use at that issuer.
 *
 * Designed to be configured as a bean.
 *
 * @author jricher
 *
 */
public class StaticClientConfigurationService implements ClientConfigurationService {

	// Map of issuer URL -> client configuration information
	private Map<String, RegisteredClient> clients;

	/**
	 * @return the clients
	 */
	public Map<String, RegisteredClient> getClients() {
		return clients;
	}

	/**
	 * @param clients the clients to set
	 */
	public void setClients(Map<String, RegisteredClient> clients) {
		this.clients = clients;
	}

	/**
	 * Get the client configured for this issuer
	 *
	 * @see org.mitre.openid.connect.client.service.ClientConfigurationService#getClientConfiguration(java.lang.String)
	 */
	@Override
	public RegisteredClient getClientConfiguration(ServerConfiguration issuer) {

		return clients.get(issuer.getIssuer());
	}

	@PostConstruct
	public void afterPropertiesSet() {
		if (clients == null || clients.isEmpty()) {
			throw new IllegalArgumentException("Clients map cannot be null or empty");
		}

	}

}
