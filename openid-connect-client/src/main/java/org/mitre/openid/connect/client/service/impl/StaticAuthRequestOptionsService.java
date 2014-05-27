/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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

import javax.servlet.http.HttpServletRequest;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.AuthRequestOptionsService;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * 
 * Always returns the same set of options.
 * 
 * @author jricher
 *
 */
public class StaticAuthRequestOptionsService implements AuthRequestOptionsService {

	private Map<String, String> options = new HashMap<String, String>();

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.AuthRequestOptionsService#getOptions(org.mitre.openid.connect.config.ServerConfiguration, org.mitre.oauth2.model.RegisteredClient, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public Map<String, String> getOptions(ServerConfiguration server, RegisteredClient client, HttpServletRequest request) {
		return options;
	}

	/**
	 * @return the options
	 */
	public Map<String, String> getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}



}
