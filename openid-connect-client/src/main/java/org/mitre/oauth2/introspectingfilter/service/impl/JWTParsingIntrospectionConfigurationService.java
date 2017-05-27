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
package org.mitre.oauth2.introspectingfilter.service.impl;

import java.text.ParseException;

import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 *
 * Parses the incoming accesstoken as a JWT and determines the issuer based on
 * the "iss" field inside the JWT. Uses the ServerConfigurationService to determine
 * the introspection URL for that issuer.
 *
 * @author jricher
 *
 */
public class JWTParsingIntrospectionConfigurationService implements IntrospectionConfigurationService {

	private ServerConfigurationService serverConfigurationService;
	private ClientConfigurationService clientConfigurationService;

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @param clientConfigurationService the clientConfigurationService to set
	 */
	public void setClientConfigurationService(ClientConfigurationService clientConfigurationService) {
		this.clientConfigurationService = clientConfigurationService;
	}

	private String getIssuer(String accessToken) {
		try {
			JWT jwt = JWTParser.parse(accessToken);

			String issuer = jwt.getJWTClaimsSet().getIssuer();

			return issuer;

		} catch (ParseException e) {
			throw new IllegalArgumentException("Unable to parse JWT", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionConfigurationService#getIntrospectionUrl(java.lang.String)
	 */
	@Override
	public String getIntrospectionUrl(String accessToken) {
		String issuer = getIssuer(accessToken);
		if (!Strings.isNullOrEmpty(issuer)) {
			ServerConfiguration server = serverConfigurationService.getServerConfiguration(issuer);
			if (server != null) {
				if (!Strings.isNullOrEmpty(server.getIntrospectionEndpointUri())) {
					return server.getIntrospectionEndpointUri();
				} else {
					throw new IllegalArgumentException("Server does not have Introspection Endpoint defined");
				}
			} else {
				throw new IllegalArgumentException("Could not find server configuration for issuer " + issuer);
			}
		} else {
			throw new IllegalArgumentException("No issuer claim found in JWT");
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService#getClientConfiguration(java.lang.String)
	 */
	@Override
	public RegisteredClient getClientConfiguration(String accessToken) {

		String issuer = getIssuer(accessToken);
		if (!Strings.isNullOrEmpty(issuer)) {
			ServerConfiguration server = serverConfigurationService.getServerConfiguration(issuer);
			if (server != null) {
				RegisteredClient client = clientConfigurationService.getClientConfiguration(server);
				if (client != null) {
					return client;
				} else {
					throw new IllegalArgumentException("Could not find client configuration for issuer " + issuer);
				}
			} else {
				throw new IllegalArgumentException("Could not find server configuration for issuer " + issuer);
			}
		} else {
			throw new IllegalArgumentException("No issuer claim found in JWT");
		}

	}



}
