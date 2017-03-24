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
package org.mitre.jwt.signer.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.util.Base64URL;

/**
 * Creates and caches symmetrical validators for clients based on client secrets.
 *
 * @author jricher
 *
 */
@Service
public class SymmetricKeyJWTValidatorCacheService {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(SymmetricKeyJWTValidatorCacheService.class);

	private LoadingCache<String, JWTSigningAndValidationService> validators;


	public SymmetricKeyJWTValidatorCacheService() {
		validators = CacheBuilder.newBuilder()
				.expireAfterAccess(24, TimeUnit.HOURS)
				.maximumSize(100)
				.build(new SymmetricValidatorBuilder());
	}


	/**
	 * Create a symmetric signing and validation service for the given client
	 *
	 * @param client
	 * @return
	 */
	public JWTSigningAndValidationService getSymmetricValidtor(ClientDetailsEntity client) {

		if (client == null) {
			logger.error("Couldn't create symmetric validator for null client");
			return null;
		}

		if (Strings.isNullOrEmpty(client.getClientSecret())) {
			logger.error("Couldn't create symmetric validator for client " + client.getClientId() + " without a client secret");
			return null;
		}

		try {
			return validators.get(client.getClientSecret());
		} catch (UncheckedExecutionException ue) {
			logger.error("Problem loading client validator", ue);
			return null;
		} catch (ExecutionException e) {
			logger.error("Problem loading client validator", e);
			return null;
		}

	}

	public class SymmetricValidatorBuilder extends CacheLoader<String, JWTSigningAndValidationService> {
		@Override
		public JWTSigningAndValidationService load(String key) throws Exception {
			try {

				String id = "SYMMETRIC-KEY";

				JWK jwk = new OctetSequenceKey(Base64URL.encode(key), KeyUse.SIGNATURE, null, null, id, null, null, null, null);
				Map<String, JWK> keys = ImmutableMap.of(id, jwk);
				JWTSigningAndValidationService service = new DefaultJWTSigningAndValidationService(keys);

				return service;

			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				logger.error("Couldn't create symmetric validator for client", e);
			}

			throw new IllegalArgumentException("Couldn't create symmetric validator for client");
		}

	}

}
