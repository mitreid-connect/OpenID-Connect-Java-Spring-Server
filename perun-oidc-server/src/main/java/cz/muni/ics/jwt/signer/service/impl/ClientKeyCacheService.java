/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

package cz.muni.ics.jwt.signer.service.impl;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import cz.muni.ics.jose.keystore.JWKSetKeyStore;
import cz.muni.ics.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import cz.muni.ics.jwt.encryption.service.impl.DefaultJWTEncryptionAndDecryptionService;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Takes in a client and returns the appropriate validator or encrypter for
 * that client's registered key types.
 *
 * @author jricher
 */
@Service
@Slf4j
public class ClientKeyCacheService {

	private JWKSetCacheService jwksUriCache;
	private SymmetricKeyJWTValidatorCacheService symmetricCache;
	private LoadingCache<JWKSet, JWTSigningAndValidationService> jwksValidators;
	private LoadingCache<JWKSet, JWTEncryptionAndDecryptionService> jwksEncrypters;

	@Autowired
	public ClientKeyCacheService(JWKSetCacheService jwksUriCache, SymmetricKeyJWTValidatorCacheService symmetricCache) {
		this.jwksValidators = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS)
				.maximumSize(100)
				.build(new JWKSetVerifierBuilder());
		this.jwksEncrypters = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS)
				.maximumSize(100)
				.build(new JWKSetEncryptorBuilder());
		if (jwksUriCache == null) {
			this.jwksUriCache = new JWKSetCacheService();
		} else {
			this.jwksUriCache = jwksUriCache;
		}

		if (symmetricCache == null) {
			this.symmetricCache = new SymmetricKeyJWTValidatorCacheService();
		} else {
			this.symmetricCache = symmetricCache;
		}
	}
	
	public JWTSigningAndValidationService getValidator(ClientDetailsEntity client, JWSAlgorithm alg) {
		Set<JWSAlgorithm> asymmetric = new HashSet<>(Arrays.asList(JWSAlgorithm.RS256, JWSAlgorithm.RS384,
			JWSAlgorithm.RS512, JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512, JWSAlgorithm.PS256,
			JWSAlgorithm.PS384, JWSAlgorithm.PS512));

		Set<JWSAlgorithm> symmetric = new HashSet<>(Arrays.asList(JWSAlgorithm.HS256, JWSAlgorithm.HS384,
			JWSAlgorithm.HS512));

		try {
			if (asymmetric.contains(alg)) {
				if (client.getJwks() != null) {
					return jwksValidators.get(client.getJwks());
				} else if (!Strings.isNullOrEmpty(client.getJwksUri())) {
					return jwksUriCache.getValidator(client.getJwksUri());
				} else {
					return null;
				}
			} else if (symmetric.contains(alg)) {
				return symmetricCache.getSymmetricValidator(client);
			} else {
				return null;
			}
		} catch (UncheckedExecutionException | ExecutionException e) {
			log.error("Problem loading client validator", e);
			return null;
		}
	}

	public JWTEncryptionAndDecryptionService getEncrypter(ClientDetailsEntity client) {
		try {
			if (client.getJwks() != null) {
				return jwksEncrypters.get(client.getJwks());
			} else if (!StringUtils.isEmpty(client.getJwksUri())) {
				return jwksUriCache.getEncrypter(client.getJwksUri());
			} else {
				return null;
			}
		} catch (UncheckedExecutionException | ExecutionException e) {
			log.error("Problem loading client encrypter", e);
			return null;
		}
	}

	private static class JWKSetEncryptorBuilder extends CacheLoader<JWKSet, JWTEncryptionAndDecryptionService> {
		@Override
		public JWTEncryptionAndDecryptionService load(JWKSet key) throws Exception {
			return new DefaultJWTEncryptionAndDecryptionService(new JWKSetKeyStore(key));
		}
	}

	private static class JWKSetVerifierBuilder extends CacheLoader<JWKSet, JWTSigningAndValidationService> {
		@Override
		public JWTSigningAndValidationService load(JWKSet key) throws Exception {
			return new DefaultJWTSigningAndValidationService(new JWKSetKeyStore(key));
		}
	}

}
