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

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;
import org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.AuthRequestUrlBuilder;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.base.Joiner;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * @author jricher
 *
 */
public class EncryptedAuthRequestUrlBuilder implements AuthRequestUrlBuilder {

	private JWKSetCacheService encrypterService;

	private JWEAlgorithm alg;
	private EncryptionMethod enc;


	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.AuthRequestUrlBuilder#buildAuthRequestUrl(org.mitre.openid.connect.config.ServerConfiguration, org.mitre.oauth2.model.RegisteredClient, java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, RegisteredClient clientConfig, String redirectUri, String nonce, String state, Map<String, String> options) {
		JWTClaimsSet claims = createJWTforRequestObject(clientConfig,redirectUri,state, options);
		claims.setClaim("nonce", nonce);

		EncryptedJWT jwt = createEncryptedJWT(serverConfig, claims);
		return createURIforAuthorizationEndpoint(serverConfig, jwt);
	}

	/**
	 * It builds the URI to access the IdP authorization endpoint.
	 * @param serverConfig
	 * @param jwt
	 */
	private String createURIforAuthorizationEndpoint(ServerConfiguration serverConfig, EncryptedJWT jwt) {
		try {
			URIBuilder uriBuilder = new URIBuilder(serverConfig.getAuthorizationEndpointUri());
			uriBuilder.addParameter("request", jwt.serialize());

			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			throw new AuthenticationServiceException("Malformed Authorization Endpoint Uri", e);
		}
	}

	/**
	 * encrypt the specified JWT
	 * @param claim
	 * @return
	 */
	private EncryptedJWT createEncryptedJWT(ServerConfiguration serverConfig, JWTClaimsSet claims) {
		EncryptedJWT jwt = new EncryptedJWT(new JWEHeader(alg, enc), claims);

		JwtEncryptionAndDecryptionService encryptor = encrypterService.getEncrypter(serverConfig.getJwksUri());
		encryptor.encryptJwt(jwt);
		return jwt;
	}
	
	/**
	 * It creates the JWT base for request to the IdP
	 * @param clientConfig
	 * @param redirectUri
	 * @param state
	 * @param options
	 */
	private JWTClaimsSet createJWTforRequestObject(RegisteredClient clientConfig, String redirectUri,String state, Map<String, String> options) {
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
	 * @return the encrypterService
	 */
	public JWKSetCacheService getEncrypterService() {
		return encrypterService;
	}

	/**
	 * @param encrypterService the encrypterService to set
	 */
	public void setEncrypterService(JWKSetCacheService encrypterService) {
		this.encrypterService = encrypterService;
	}

	/**
	 * @return the alg
	 */
	public JWEAlgorithm getAlg() {
		return alg;
	}

	/**
	 * @param alg the alg to set
	 */
	public void setAlg(JWEAlgorithm alg) {
		this.alg = alg;
	}

	/**
	 * @return the enc
	 */
	public EncryptionMethod getEnc() {
		return enc;
	}

	/**
	 * @param enc the enc to set
	 */
	public void setEnc(EncryptionMethod enc) {
		this.enc = enc;
	}

	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, RegisteredClient clientConfig, String redirectUri, String state, Map<String, String> options) {
				JWTClaimsSet claims = createJWTforRequestObject(clientConfig,redirectUri,state, options);
				EncryptedJWT jwt = createEncryptedJWT(serverConfig, claims);

				return createURIforAuthorizationEndpoint(serverConfig, jwt);
			}
	}


