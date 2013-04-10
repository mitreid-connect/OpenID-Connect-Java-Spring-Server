/**
 * 
 */
package org.mitre.jwt.signer.service.impl;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.jose.keystore.JWKSetKeyStore;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * 
 * Creates a 
 * 
 * @author jricher
 *
 */
@Service
public class JWKSetSigningAndValidationServiceCacheService {

	private LoadingCache<String, JwtSigningAndValidationService> cache;

	public JWKSetSigningAndValidationServiceCacheService() {
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(100)
				.build(new JWKSetVerifierFetcher());
	}
	
	/**
	 * @param key
	 * @return
	 * @throws ExecutionException
	 * @see com.google.common.cache.Cache#get(java.lang.Object)
	 */
    public JwtSigningAndValidationService get(String key) {
	    try {
	        return cache.get(key);
        } catch (ExecutionException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        return null;
        }
    }

	/**
     * @author jricher
     *
     */
    private class JWKSetVerifierFetcher extends CacheLoader<String, JwtSigningAndValidationService> {
    	private HttpClient httpClient = new DefaultHttpClient();
    	private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    	private RestTemplate restTemplate = new RestTemplate(httpFactory);
    	
    	/**
    	 * Load the JWK Set and build the appropriate signing service.
    	 */
        @Override
        public JwtSigningAndValidationService load(String key) throws Exception {

        	String jsonString = restTemplate.getForObject(key, String.class);
        	JWKSet jwkSet = JWKSet.parse(jsonString);
        	
        	JWKSetKeyStore keyStore = new JWKSetKeyStore(jwkSet);
        	
        	JwtSigningAndValidationService service = new DefaultJwtSigningAndValidationService(keyStore);

        	return service;
        	
        }

    }

}
