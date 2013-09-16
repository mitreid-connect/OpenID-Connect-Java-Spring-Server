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

}
