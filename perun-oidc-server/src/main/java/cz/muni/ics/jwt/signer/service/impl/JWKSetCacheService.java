/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.jwt.signer.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.gson.JsonParseException;
import com.nimbusds.jose.jwk.JWKSet;
import cz.muni.ics.jose.keystore.JWKSetKeyStore;
import cz.muni.ics.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import cz.muni.ics.jwt.encryption.service.impl.DefaultJWTEncryptionAndDecryptionService;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Creates a caching map of JOSE signers/validators and encrypters/decryptors
 * keyed on the JWK Set URI. Dynamically loads JWK Sets to create the services.
 *
 * @author jricher
 */
@Service
@Slf4j
public class JWKSetCacheService {

	private final LoadingCache<String, JWTSigningAndValidationService> validators;
	private final LoadingCache<String, JWTEncryptionAndDecryptionService> encrypters;

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

	public JWTSigningAndValidationService getValidator(String jwksUri) {
		try {
			return validators.get(jwksUri);
		} catch (UncheckedExecutionException | ExecutionException e) {
			log.warn("Couldn't load JWK Set from {}: {}", jwksUri, e.getMessage());
			return null;
		}
	}

	public JWTEncryptionAndDecryptionService getEncrypter(String jwksUri) {
		try {
			return encrypters.get(jwksUri);
		} catch (UncheckedExecutionException | ExecutionException e) {
			log.warn("Couldn't load JWK Set from {}: {}", jwksUri, e.getMessage());
			return null;
		}
	}

	private static class JWKSetVerifierFetcher extends CacheLoader<String, JWTSigningAndValidationService> {
		private final RestTemplate restTemplate;

		JWKSetVerifierFetcher(HttpClient httpClient) {
			HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			this.restTemplate = new RestTemplate(httpFactory);
		}

		@Override
		public JWTSigningAndValidationService load(String key) throws Exception {
			String jsonString = restTemplate.getForObject(key, String.class);
			JWKSet jwkSet = JWKSet.parse(jsonString);
			JWKSetKeyStore keyStore = new JWKSetKeyStore(jwkSet);
			return new DefaultJWTSigningAndValidationService(keyStore);
		}
	}

	private static class JWKSetEncryptorFetcher extends CacheLoader<String, JWTEncryptionAndDecryptionService> {
		private final RestTemplate restTemplate;

		public JWKSetEncryptorFetcher(HttpClient httpClient) {
			HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			this.restTemplate = new RestTemplate(httpFactory);
		}

		@Override
		public JWTEncryptionAndDecryptionService load(String key) throws Exception {
			try {
				String jsonString = restTemplate.getForObject(key, String.class);
				JWKSet jwkSet = JWKSet.parse(jsonString);
				JWKSetKeyStore keyStore = new JWKSetKeyStore(jwkSet);
				return new DefaultJWTEncryptionAndDecryptionService(keyStore);
			} catch (JsonParseException | RestClientException e) {
				throw new IllegalArgumentException("Unable to load JWK Set");
			}
		}
	}

}
