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

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;
import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.AuthRequestUrlBuilder;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
public class SignedAuthRequestUrlBuilder implements AuthRequestUrlBuilder {

	private JWTSigningAndValidationService signingAndValidationService;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.AuthRequestUrlBuilder#buildAuthRequestUrl(org.mitre.openid.connect.config.ServerConfiguration, org.springframework.security.oauth2.provider.ClientDetails, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, RegisteredClient clientConfig, String redirectUri, String nonce, String state, Map<String, String> options, String loginHint) {

		// create our signed JWT for the request object
		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder();

		//set parameters to JwtClaims
		claims.claim("response_type", "code");
		claims.claim("client_id", clientConfig.getClientId());
		claims.claim("scope", Joiner.on(" ").join(clientConfig.getScope()));

		// build our redirect URI
		claims.claim("redirect_uri", redirectUri);

		// this comes back in the id token
		claims.claim("nonce", nonce);

		// this comes back in the auth request return
		claims.claim("state", state);

		// Optional parameters
		for (Entry<String, String> option : options.entrySet()) {
			claims.claim(option.getKey(), option.getValue());
		}

		// if there's a login hint, send it
		if (!Strings.isNullOrEmpty(loginHint)) {
			claims.claim("login_hint", loginHint);
		}

		JWSAlgorithm alg = clientConfig.getRequestObjectSigningAlg();
		if (alg == null) {
			alg = signingAndValidationService.getDefaultSigningAlgorithm();
		}

		SignedJWT jwt = new SignedJWT(new JWSHeader(alg), claims.build());

		signingAndValidationService.signJwt(jwt, alg);

		try {
			URIBuilder uriBuilder = new URIBuilder(serverConfig.getAuthorizationEndpointUri());
			uriBuilder.addParameter("request", jwt.serialize());

			// build out the URI
			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			throw new AuthenticationServiceException("Malformed Authorization Endpoint Uri", e);
		}
	}

	/**
	 * @return the signingAndValidationService
	 */
	public JWTSigningAndValidationService getSigningAndValidationService() {
		return signingAndValidationService;
	}

	/**
	 * @param signingAndValidationService the signingAndValidationService to set
	 */
	public void setSigningAndValidationService(JWTSigningAndValidationService signingAndValidationService) {
		this.signingAndValidationService = signingAndValidationService;
	}

}
