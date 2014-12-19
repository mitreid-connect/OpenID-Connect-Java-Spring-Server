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
 ******************************************************************************/
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

/**
 * 
 * Builds an auth request redirect URI with normal query parameters.
 * 
 * @author jricher
 *
 */
public class PlainAuthRequestUrlBuilder implements AuthRequestUrlBuilder {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mitre.openid.connect.client.service.AuthRequestUrlBuilder#
	 * buildAuthRequest(javax.servlet.http.HttpServletRequest,
	 * org.mitre.openid.connect.config.ServerConfiguration,
	 * org.springframework.security.oauth2.provider.ClientDetails)
	 */
	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig,RegisteredClient clientConfig, String redirectUri, String nonce,String state, Map<String, String> options) {
		try {
			URIBuilder uriBuilder = createURIforAuthorizationEndpoint(serverConfig,clientConfig, redirectUri, state, options);
			uriBuilder.addParameter("nonce", nonce);

			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			throw createAuthenticationServiceExceptionMalformedAuthorizationEndpoint(e);
		}
	}
	private AuthenticationServiceException createAuthenticationServiceExceptionMalformedAuthorizationEndpoint(
			URISyntaxException e) {
		return new AuthenticationServiceException(
				"Malformed Authorization Endpoint Uri", e);
	}

	/**
	 * It builds the URI to access the IdP authorization endpoint.
	 * @param serverConfig
	 * @param clientConfig
	 * @param redirectUri
	 * @param state
	 * @param options
	 * @return
	 * @throws URISyntaxException
	 */
	private URIBuilder createURIforAuthorizationEndpoint(ServerConfiguration serverConfig,RegisteredClient clientConfig, String redirectUri, String state,Map<String, String> options) throws URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(serverConfig.getAuthorizationEndpointUri());
		uriBuilder.addParameter("response_type", "code");
		uriBuilder.addParameter("client_id", clientConfig.getClientId());
		uriBuilder.addParameter("scope", Joiner.on(" ").join(clientConfig.getScope()));
		uriBuilder.addParameter("redirect_uri", redirectUri);
		uriBuilder.addParameter("state", state);

		addOptionalParameters(options, uriBuilder);
		return uriBuilder;
	}
	protected void addOptionalParameters(Map<String, String> options,
			URIBuilder uriBuilder) {
		for (Entry<String, String> option : options.entrySet()) {
			uriBuilder.addParameter(option.getKey(), option.getValue());
		}
	}

	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig,RegisteredClient clientConfig, String redirectUri, String state,Map<String, String> options) {
		try {
			URIBuilder uriBuilder = createURIforAuthorizationEndpoint(serverConfig,clientConfig, redirectUri, state, options);

			return uriBuilder.build().toString();
			} catch (URISyntaxException e) {
				throw createAuthenticationServiceExceptionMalformedAuthorizationEndpoint(e);

		}
	}

}
