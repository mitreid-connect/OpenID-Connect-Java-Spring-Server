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
package org.mitre.oauth2.introspectingfilter.service;

import org.mitre.oauth2.model.RegisteredClient;

/**
 * @author jricher
 *
 */
public interface IntrospectionConfigurationService {

	/**
	 * Get the introspection URL based on the access token.
	 * @param accessToken
	 * @return
	 */
	public String getIntrospectionUrl(String accessToken);


	/**
	 * Get the client configuration to use to connect to the
	 * introspection endpoint. In particular, this cares about
	 * the clientId, clientSecret, and tokenEndpointAuthMethod
	 * fields.
	 */
	public RegisteredClient getClientConfiguration(String accessToken);

}
