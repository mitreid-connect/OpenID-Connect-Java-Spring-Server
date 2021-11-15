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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.util.Base64URL;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Creates and caches symmetrical validators for clients based on client secrets.
 *
 * @author jricher
 */
@Service
@Slf4j
public class SymmetricKeyJWTValidatorCacheService {

	private final LoadingCache<String, JWTSigningAndValidationService> validators;

	public SymmetricKeyJWTValidatorCacheService() {
		validators = CacheBuilder.newBuilder()
				.expireAfterAccess(24, TimeUnit.HOURS)
				.maximumSize(100)
				.build(new SymmetricValidatorBuilder());
	}

	public JWTSigningAndValidationService getSymmetricValidator(ClientDetailsEntity client) {
		if (client == null) {
			log.error("Couldn't create symmetric validator for null client");
			return null;
		} else if (StringUtils.isEmpty(client.getClientSecret())) {
			log.error("Couldn't create symmetric validator for client {} without a client secret", client.getClientId());
			return null;
		}

		try {
			return validators.get(client.getClientSecret());
		} catch (UncheckedExecutionException | ExecutionException ue) {
			log.error("Problem loading client validator", ue);
			return null;
		}
	}

	public static class SymmetricValidatorBuilder extends CacheLoader<String, JWTSigningAndValidationService> {
		@Override
		public JWTSigningAndValidationService load(String key) {
			String id = "SYMMETRIC-KEY";
			JWK jwk = new OctetSequenceKey.Builder(Base64URL.encode(key))
				.keyUse(KeyUse.SIGNATURE)
				.keyID(id)
				.build();
			Map<String, JWK> keys = ImmutableMap.of(id, jwk);

			return new DefaultJWTSigningAndValidationService(keys);
		}
	}

}
