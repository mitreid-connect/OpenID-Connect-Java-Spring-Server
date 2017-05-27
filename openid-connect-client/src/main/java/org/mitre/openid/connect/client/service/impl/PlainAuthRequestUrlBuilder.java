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

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.AuthRequestUrlBuilder;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 *
 * Builds an auth request redirect URI with normal query parameters.
 *
 * @author jricher
 *
 */
public class PlainAuthRequestUrlBuilder implements AuthRequestUrlBuilder {

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.AuthRequestUrlBuilder#buildAuthRequest(javax.servlet.http.HttpServletRequest, org.mitre.openid.connect.config.ServerConfiguration, org.springframework.security.oauth2.provider.ClientDetails)
	 */
	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, RegisteredClient clientConfig, String redirectUri, String nonce, String state, Map<String, String> options, String loginHint) {
		try {

			URIBuilder uriBuilder = new URIBuilder(serverConfig.getAuthorizationEndpointUri());
			uriBuilder.addParameter("response_type", "code");
			uriBuilder.addParameter("client_id", clientConfig.getClientId());
			uriBuilder.addParameter("scope", Joiner.on(" ").join(clientConfig.getScope()));

			uriBuilder.addParameter("redirect_uri", redirectUri);

			uriBuilder.addParameter("nonce", nonce);

			uriBuilder.addParameter("state", state);

			// Optional parameters:
			for (Entry<String, String> option : options.entrySet()) {
				uriBuilder.addParameter(option.getKey(), option.getValue());
			}

			// if there's a login hint, send it
			if (!Strings.isNullOrEmpty(loginHint)) {
				uriBuilder.addParameter("login_hint", loginHint);
			}

			return uriBuilder.build().toString();

		} catch (URISyntaxException e) {
			throw new AuthenticationServiceException("Malformed Authorization Endpoint Uri", e);

		}



	}

}
