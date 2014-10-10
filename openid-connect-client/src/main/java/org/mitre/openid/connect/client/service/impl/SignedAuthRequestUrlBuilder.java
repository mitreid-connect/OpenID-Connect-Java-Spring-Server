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
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.AuthRequestUrlBuilder;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.base.Joiner;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
public class SignedAuthRequestUrlBuilder implements AuthRequestUrlBuilder {

	private JwtSigningAndValidationService signingAndValidationService;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.AuthRequestUrlBuilder#buildAuthRequestUrl(org.mitre.openid.connect.config.ServerConfiguration, org.springframework.security.oauth2.provider.ClientDetails, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, RegisteredClient clientConfig, String redirectUri, String nonce, String state, Map<String, String> options) {
		JWTClaimsSet claims = createJWTforRequestObject(clientConfig,redirectUri, state, options);
		claims.setClaim("nonce", nonce);
		
		SignedJWT jwt = createSignedJWT(claims);
		return createURIforAuthorizationEndpoint(serverConfig, jwt);
	}
	/**
	 * mount the final uri for access the IdP authorization endpoint
	 * @param serverConfig
	 * @param jwt
	 * @return
	 */

	private String createURIforAuthorizationEndpoint(ServerConfiguration serverConfig, SignedJWT jwt) {
		try {
			URIBuilder uriBuilder = new URIBuilder(serverConfig.getAuthorizationEndpointUri());
			uriBuilder.addParameter("request", jwt.serialize());

			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			throw new AuthenticationServiceException("Malformed Authorization Endpoint Uri", e);
		}
	}
	
	/**
	 * sign the specified JWT
	 * @param claims
	 * @return
	 */
	private SignedJWT createSignedJWT(JWTClaimsSet claims) {
		SignedJWT jwt = new SignedJWT(new JWSHeader(signingAndValidationService.getDefaultSigningAlgorithm()), claims);

		signingAndValidationService.signJwt(jwt);
		return jwt;
	}
	/**
	 * create the JWT base for request to the IdP
	 * @param clientConfig
	 * @param redirectUri
	 * @param state
	 * @param options
	 * @return
	 */
	private JWTClaimsSet createJWTforRequestObject(RegisteredClient clientConfig, String redirectUri, String state,Map<String, String> options) {
		JWTClaimsSet claims = new JWTClaimsSet();
		claims.setClaim("response_type", "code");
		claims.setClaim("client_id", clientConfig.getClientId());
		claims.setClaim("scope", Joiner.on(" ").join(clientConfig.getScope()));
		claims.setClaim("redirect_uri", redirectUri);
		claims.setClaim("state", state);

		addOptionalParameters(options, claims);
		return claims;
	}
	private void addOptionalParameters(Map<String, String> options,
			JWTClaimsSet claims) {
		for (Entry<String, String> option : options.entrySet()) {
			claims.setClaim(option.getKey(), option.getValue());
		}
	}
	
	/**
	 * @return the signingAndValidationService
	 */
	public JwtSigningAndValidationService getSigningAndValidationService() {
		return signingAndValidationService;
	}
	
	

	/**
	 * @param signingAndValidationService the signingAndValidationService to set
	 */
	public void setSigningAndValidationService(JwtSigningAndValidationService signingAndValidationService) {
		this.signingAndValidationService = signingAndValidationService;
	}

	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig,RegisteredClient clientConfig, String redirectUri, String state,Map<String, String> options) {
		JWTClaimsSet claims = createJWTforRequestObject(clientConfig,redirectUri, state, options);		
		
		SignedJWT jwt = createSignedJWT(claims);
		return createURIforAuthorizationEndpoint(serverConfig, jwt);
	}

}
