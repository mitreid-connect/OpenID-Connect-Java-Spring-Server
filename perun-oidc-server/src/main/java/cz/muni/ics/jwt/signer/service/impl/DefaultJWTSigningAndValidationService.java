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
package cz.muni.ics.jwt.signer.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSProvider;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.jose.keystore.JWKSetKeyStore;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class DefaultJWTSigningAndValidationService implements JWTSigningAndValidationService {

	private final Map<String, JWSSigner> signers = new HashMap<>();
	private final Map<String, JWSVerifier> verifiers = new HashMap<>();

	private String defaultSignerKeyId;
	private JWSAlgorithm defaultAlgorithm;
	private Map<String, JWK> keys = new HashMap<>();

	/**
	 * Build this service based on the keys given. All public keys will be used
	 * to make verifiers, all private keys will be used to make signers.
	 *
	 * @param keys A map of key identifier to key.
	 */
	public DefaultJWTSigningAndValidationService(Map<String, JWK> keys) {
		this.keys = keys;
		buildSignersAndVerifiers();
	}

	/**
	 * Build this service based on the given keystore. All keys must have a key
	 * id ({@code kid}) field in order to be used.
	 *
	 * @param keyStore The keystore to load all keys from.
	 */
	public DefaultJWTSigningAndValidationService(JWKSetKeyStore keyStore) {
		if (keyStore!= null && keyStore.getJwkSet() != null) {
			for (JWK key : keyStore.getKeys()) {
				if (!StringUtils.isEmpty(key.getKeyID())) {
					this.keys.put(key.getKeyID(), key);
				} else {
					String fakeKid = UUID.randomUUID().toString();
					this.keys.put(fakeKid, key);
				}
			}
		}
		buildSignersAndVerifiers();
	}

	@Override
	public String getDefaultSignerKeyId() {
		return defaultSignerKeyId;
	}

	public void setDefaultSignerKeyId(String defaultSignerId) {
		this.defaultSignerKeyId = defaultSignerId;
	}

	@Override
	public JWSAlgorithm getDefaultSigningAlgorithm() {
		return defaultAlgorithm;
	}

	public void setDefaultSigningAlgorithmName(String algName) {
		defaultAlgorithm = JWSAlgorithm.parse(algName);
	}

	public String getDefaultSigningAlgorithmName() {
		if (defaultAlgorithm != null) {
			return defaultAlgorithm.getName();
		} else {
			return null;
		}
	}

	@Override
	public void signJwt(SignedJWT jwt) {
		if (getDefaultSignerKeyId() == null) {
			throw new IllegalStateException("Tried to call default signing with no default signer ID set");
		}

		JWSSigner signer = signers.get(getDefaultSignerKeyId());

		try {
			jwt.sign(signer);
		} catch (JOSEException e) {
			log.error("Failed to sign JWT, error was: ", e);
		}
	}

	@Override
	public void signJwt(SignedJWT jwt, JWSAlgorithm alg) {
		JWSSigner signer = null;

		for (JWSSigner s : signers.values()) {
			if (s.supportedJWSAlgorithms().contains(alg)) {
				signer = s;
				break;
			}
		}

		if (signer == null) {
			log.error("No matching algorithm found for alg={}", alg);
		} else {
			try {
				jwt.sign(signer);
			} catch (JOSEException e) {
				log.error("Failed to sign JWT, error was: ", e);
			}
		}
	}

	@Override
	public boolean validateSignature(SignedJWT jwt) {
		for (JWSVerifier verifier : verifiers.values()) {
			try {
				return jwt.verify(verifier);
			} catch (JOSEException e) {
				log.error("Failed to validate signature with {} error message: {}", verifier, e.getMessage());
			}
		}

		return false;
	}

	@Override
	public Map<String, JWK> getAllPublicKeys() {
		Map<String, JWK> pubKeys = new HashMap<>();

		keys.keySet().forEach(keyId -> {
			JWK key = keys.get(keyId);
			JWK pub = key.toPublicJWK();
			if (pub != null) {
				pubKeys.put(keyId, pub);
			}
		});

		return pubKeys;
	}

	@Override
	public Collection<JWSAlgorithm> getAllSigningAlgsSupported() {
		Set<JWSAlgorithm> algs = new HashSet<>();
		signers.values().stream().map(JWSProvider::supportedJWSAlgorithms).forEach(algs::addAll);
		verifiers.values().stream().map(JWSProvider::supportedJWSAlgorithms).forEach(algs::addAll);

		return algs;
	}

	private void buildSignersAndVerifiers() {
		for (Map.Entry<String, JWK> jwkEntry : keys.entrySet()) {
			String id = jwkEntry.getKey();
			JWK jwk = jwkEntry.getValue();
			try {
				if (jwk instanceof RSAKey) {
					processRSAKey(signers, verifiers, jwk, id);
				} else if (jwk instanceof ECKey) {
					processECKey(signers, verifiers, jwk, id);
				} else if (jwk instanceof OctetSequenceKey) {
					processOctetKey(signers, verifiers, jwk, id);
				} else {
					log.warn("Unknown key type: {}", jwk);
				}
			} catch (JOSEException e) {
				log.warn("Exception loading signer/verifier", e);
			}
		}

		if (defaultSignerKeyId == null && keys.size() == 1) {
			setDefaultSignerKeyId(keys.keySet().iterator().next());
		}
	}

	private void processOctetKey(Map<String, JWSSigner> signers, Map<String, JWSVerifier> verifiers, JWK jwk, String id)
		throws JOSEException
	{
		if (jwk.isPrivate()) {
			MACSigner signer = new MACSigner((OctetSequenceKey) jwk);
			signers.put(id, signer);
		}

		MACVerifier verifier = new MACVerifier((OctetSequenceKey) jwk);
		verifiers.put(id, verifier);
	}

	private void processECKey(Map<String, JWSSigner> signers, Map<String, JWSVerifier> verifiers, JWK jwk, String id)
		throws JOSEException
	{
		if (jwk.isPrivate()) {
			ECDSASigner signer = new ECDSASigner((ECKey) jwk);
			signers.put(id, signer);
		}

		ECDSAVerifier verifier = new ECDSAVerifier((ECKey) jwk);
		verifiers.put(id, verifier);
	}

	private void processRSAKey(Map<String, JWSSigner> signers, Map<String, JWSVerifier> verifiers, JWK jwk, String id)
		throws JOSEException
	{
		if (jwk.isPrivate()) {
			RSASSASigner signer = new RSASSASigner((RSAKey) jwk);
			signers.put(id, signer);
		}

		RSASSAVerifier verifier = new RSASSAVerifier((RSAKey) jwk);
		verifiers.put(id, verifier);
	}

}
