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
package org.mitre.oauth2.introspectingfilter.service.impl;

import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;

/**
 *
 * Always provides the (configured) IntrospectionURL and RegisteredClient regardless
 * of token. Useful for talking to a single, trusted authorization server.
 *
 * @author jricher
 *
 */
public class StaticIntrospectionConfigurationService implements IntrospectionConfigurationService {

	private String introspectionUrl;
	private RegisteredClient clientConfiguration;

	/**
	 * @return the clientConfiguration
	 */
	public RegisteredClient getClientConfiguration() {
		return clientConfiguration;
	}

	/**
	 * @param clientConfiguration the clientConfiguration to set
	 */
	public void setClientConfiguration(RegisteredClient client) {
		this.clientConfiguration = client;
	}

	/**
	 * @return the introspectionUrl
	 */
	public String getIntrospectionUrl() {
		return introspectionUrl;
	}

	/**
	 * @param introspectionUrl the introspectionUrl to set
	 */
	public void setIntrospectionUrl(String introspectionUrl) {
		this.introspectionUrl = introspectionUrl;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionConfigurationService#getIntrospectionUrl(java.lang.String)
	 */
	@Override
	public String getIntrospectionUrl(String accessToken) {
		return getIntrospectionUrl();
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService#getClientConfiguration(java.lang.String)
	 */
	@Override
	public RegisteredClient getClientConfiguration(String accessToken) {
		return getClientConfiguration();
	}

}
