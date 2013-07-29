/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
 ******************************************************************************/
/**
 * 
 */
package org.mitre.oauth2.introspectingfilter;

import java.text.ParseException;

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
public class JWTParsingIntrospectionUrlProvider implements IntrospectionUrlProvider {

	private ServerConfigurationService serverConfigurationService;

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

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionUrlProvider#getIntrospectionUrl(java.lang.String)
	 */
	@Override
	public String getIntrospectionUrl(String accessToken) {

		try {
			JWT jwt = JWTParser.parse(accessToken);

			String issuer = jwt.getJWTClaimsSet().getIssuer();
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

		} catch (ParseException e) {
			throw new IllegalArgumentException("Unable to parse JWT", e);
		}

	}

}
