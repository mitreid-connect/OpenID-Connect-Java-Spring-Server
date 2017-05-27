/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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

import java.util.HashMap;
import java.util.Map;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.RegisteredClientService;

/**
 * @author jricher
 *
 */
public class InMemoryRegisteredClientService implements RegisteredClientService {

	private Map<String, RegisteredClient> clients = new HashMap<>();

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.RegisteredClientService#getByIssuer(java.lang.String)
	 */
	@Override
	public RegisteredClient getByIssuer(String issuer) {
		return clients.get(issuer);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.RegisteredClientService#save(org.mitre.oauth2.model.RegisteredClient)
	 */
	@Override
	public void save(String issuer, RegisteredClient client) {
		clients.put(issuer, client);
	}

}
