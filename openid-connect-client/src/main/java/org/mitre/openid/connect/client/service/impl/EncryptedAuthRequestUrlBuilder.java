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
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.AuthRequestUrlBuilder#buildAuthRequestUrl(org.mitre.openid.connect.config.ServerConfiguration, org.mitre.oauth2.model.RegisteredClient, java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, RegisteredClient clientConfig, String redirectUri, String nonce, String state, Map<String, String> options) {

		JWEAlgorithm alg = null;
		EncryptionMethod enc = null;
		
		if (serverConfig.getRequestObjectEncryptionAlgValuesSupported() != null || !serverConfig.getRequestObjectEncryptionAlgValuesSupported().isEmpty()) {
			alg = serverConfig.getRequestObjectEncryptionAlgValuesSupported().get(0); // get the first alg value in the list
			if (serverConfig.getRequestObjectEncryptionEncValuesSupported() != null || !serverConfig.getRequestObjectEncryptionEncValuesSupported().isEmpty()) {
				enc = serverConfig.getRequestObjectEncryptionEncValuesSupported().get(0); // get the first enc value in the list
			}
		}
		
		if (alg == null || enc == null) {
			throw new IllegalArgumentException("No encryption algorithms found for server " + serverConfig);
		}
		
		
		// create our signed JWT for the request object
		JWTClaimsSet claims = new JWTClaimsSet();

		//set parameters to JwtClaims
		claims.setClaim("response_type", "code");
		claims.setClaim("client_id", clientConfig.getClientId());
		claims.setClaim("scope", Joiner.on(" ").join(clientConfig.getScope()));

		// build our redirect URI
		claims.setClaim("redirect_uri", redirectUri);

		// this comes back in the id token
		claims.setClaim("nonce", nonce);

		// this comes back in the auth request return
		claims.setClaim("state", state);
		
		// Optional parameters
		for (Entry<String, String> option : options.entrySet()) {
			claims.setClaim(option.getKey(), option.getValue());
		}

		EncryptedJWT jwt = new EncryptedJWT(new JWEHeader(alg, enc), claims);
		
		JwtEncryptionAndDecryptionService encryptor = encrypterService.getEncrypter(serverConfig.getJwksUri());
		
		encryptor.encryptJwt(jwt);
		
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

}
