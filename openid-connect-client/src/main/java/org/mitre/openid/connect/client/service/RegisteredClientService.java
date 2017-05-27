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
package org.mitre.openid.connect.client.service;

import org.mitre.oauth2.model.RegisteredClient;

/**
 * @author jricher
 *
 */
public interface RegisteredClientService {

	/**
	 * Get a remembered client (if one exists) to talk to the given issuer. This
	 * client likely doesn't have its full configuration information but contains
	 * the information needed to fetch it.
	 * @param issuer
	 * @return
	 */
	RegisteredClient getByIssuer(String issuer);

	/**
	 * Save this client's information for talking to the given issuer. This will
	 * save only enough information to fetch the client's full configuration from
	 * the server.
	 * @param client
	 */
	void save(String issuer, RegisteredClient client);

}
