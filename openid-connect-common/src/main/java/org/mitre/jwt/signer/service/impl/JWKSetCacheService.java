/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
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
package org.mitre.jwt.signer.service.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mitre.jose.keystore.JWKSetKeyStore;
import org.mitre.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import org.mitre.jwt.encryption.service.impl.DefaultJWTEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.gson.JsonParseException;
import com.nimbusds.jose.jwk.JWKSet;

/**
 * 
 * Creates a caching map of JOSE signers/validators and encrypters/decryptors
 * keyed on the JWK Set URI. Dynamically loads JWK Sets to create the services.
 * 
 * @author jricher
 * 
 */
@Service
public class JWKSetCacheService {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(JWKSetCacheService.class);

	// map of jwk set uri -> signing/validation service built on the keys found in that jwk set
	private LoadingCache<String, JWTSigningAndValidationService> validators;

	// map of jwk set uri -> encryption/decryption service built on the keys found in that jwk set
	private LoadingCache<String, JWTEncryptionAndDecryptionService> encrypters;

	public JWKSetCacheService() {
		this.validators = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS) // expires 1 hour after fetch
				.maximumSize(100)
				.build(new JWKSetVerifierFetcher(HttpClientBuilder.create().useSystemProperties().build()));
		this.encrypters = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS) // expires 1 hour after fetch
				.maximumSize(100)
				.build(new JWKSetEncryptorFetcher(HttpClientBuilder.create().useSystemProperties().build()));
	}

	/**
	 * @param jwksUri
	 * @return
	 * @throws ExecutionException
	 * @see com.google.common.cache.Cache#get(java.lang.Object)
	 */
	public JWTSigningAndValidationService getValidator(String jwksUri) {
		try {
			return validators.get(jwksUri);
		} catch (UncheckedExecutionException | ExecutionException e) {
			logger.warn("Couldn't load JWK Set from " + jwksUri + ": " + e.getMessage());
			return null;
		}
	}

	public JWTEncryptionAndDecryptionService getEncrypter(String jwksUri) {
		try {
			return encrypters.get(jwksUri);
		} catch (UncheckedExecutionException | ExecutionException e) {
			logger.warn("Couldn't load JWK Set from " + jwksUri + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * @author jricher
	 *
	 */
	private class JWKSetVerifierFetcher extends CacheLoader<String, JWTSigningAndValidationService> {
		private HttpComponentsClientHttpRequestFactory httpFactory;
		private RestTemplate restTemplate;

		JWKSetVerifierFetcher(HttpClient httpClient) {
			this.httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			this.restTemplate = new RestTemplate(httpFactory);
		}

		/**
		 * Load the JWK Set and build the appropriate signing service.
		 */
		@Override
		public JWTSigningAndValidationService load(String key) throws Exception {
			String jsonString = restTemplate.getForObject(key, String.class);
			JWKSet jwkSet = JWKSet.parse(jsonString);

			JWKSetKeyStore keyStore = new JWKSetKeyStore(jwkSet);

			JWTSigningAndValidationService service = new DefaultJWTSigningAndValidationService(keyStore);

			return service;
		}

	}

	/**
	 * @author jricher
	 *
	 */
	private class JWKSetEncryptorFetcher extends CacheLoader<String, JWTEncryptionAndDecryptionService> {
		private HttpComponentsClientHttpRequestFactory httpFactory;
		private RestTemplate restTemplate;

		public JWKSetEncryptorFetcher(HttpClient httpClient) {
			this.httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			this.restTemplate = new RestTemplate(httpFactory);
		}

		/* (non-Javadoc)
		 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
		 */
		@Override
		public JWTEncryptionAndDecryptionService load(String key) throws Exception {
			try {
				String jsonString = restTemplate.getForObject(key, String.class);
				JWKSet jwkSet = JWKSet.parse(jsonString);
	
				JWKSetKeyStore keyStore = new JWKSetKeyStore(jwkSet);
	
				JWTEncryptionAndDecryptionService service = new DefaultJWTEncryptionAndDecryptionService(keyStore);
	
				return service;
			} catch (JsonParseException | RestClientException e) {
				throw new IllegalArgumentException("Unable to load JWK Set");
			}
		}
	}

}
