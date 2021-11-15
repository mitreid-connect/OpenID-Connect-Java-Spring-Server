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
package cz.muni.ics.jwt.encryption.service.impl;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import cz.muni.ics.jose.keystore.JWKSetKeyStore;
import cz.muni.ics.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author wkim
 */
@Slf4j
public class DefaultJWTEncryptionAndDecryptionService implements JWTEncryptionAndDecryptionService {

	private final Map<String, JWEEncrypter> encrypters = new HashMap<>();
	private final Map<String, JWEDecrypter> decrypters = new HashMap<>();
	private String defaultEncryptionKeyId;
	private String defaultDecryptionKeyId;
	private JWEAlgorithm defaultAlgorithm;
	private Map<String, JWK> keys = new HashMap<>();

	/**
	 * Build this service based on the keys given. All public keys will be used to make encrypters,
	 * all private keys will be used to make decrypters.
	 *
	 * @param keys Map of keys
	 * @throws JOSEException Javascript Object Signing and Encryption (JOSE) exception.
	 */
	public DefaultJWTEncryptionAndDecryptionService(Map<String, JWK> keys) throws JOSEException {
		this.keys = keys;
		buildEncryptersAndDecrypters();
	}

	/**
	 * Build this service based on the given keystore. All keys must have a key
	 * id ({@code kid}) field in order to be used.
	 *
	 * @param keyStore JWK KeyStore
	 * @throws JOSEException Javascript Object Signing and Encryption (JOSE) exception.
	 */
	public DefaultJWTEncryptionAndDecryptionService(JWKSetKeyStore keyStore) throws JOSEException {
		for (JWK key : keyStore.getKeys()) {
			if (!StringUtils.isEmpty(key.getKeyID())) {
				this.keys.put(key.getKeyID(), key);
			} else {
				throw new IllegalArgumentException("Tried to load a key from a keystore without a 'kid' field: " + key);
			}
		}

		buildEncryptersAndDecrypters();
	}

	public String getDefaultEncryptionKeyId() {
		if (defaultEncryptionKeyId != null) {
			return defaultEncryptionKeyId;
		} else if (keys.size() == 1) {
			// if there's only one key in the map, it's the default
			return keys.keySet().iterator().next();
		} else {
			return null;
		}
	}

	public void setDefaultEncryptionKeyId(String defaultEncryptionKeyId) {
		this.defaultEncryptionKeyId = defaultEncryptionKeyId;
	}

	public String getDefaultDecryptionKeyId() {
		if (defaultDecryptionKeyId != null) {
			return defaultDecryptionKeyId;
		} else if (keys.size() == 1) {
			// if there's only one key in the map, it's the default
			return keys.keySet().iterator().next();
		} else {
			return null;
		}
	}

	public void setDefaultDecryptionKeyId(String defaultDecryptionKeyId) {
		this.defaultDecryptionKeyId = defaultDecryptionKeyId;
	}

	public JWEAlgorithm getDefaultAlgorithm() {
		return defaultAlgorithm;
	}

	public void setDefaultAlgorithm(JWEAlgorithm defaultAlgorithm) {
		this.defaultAlgorithm = defaultAlgorithm;
	}

	@PostConstruct
	public void afterPropertiesSet() {
		if (keys == null) {
			throw new IllegalArgumentException("Encryption and decryption service must have at least one key configured.");
		}

		try {
			buildEncryptersAndDecrypters();
		} catch (JOSEException e) {
			throw new IllegalArgumentException("Encryption and decryption service was unable to process JOSE object.");
		}
	}

	@Override
	public void encryptJwt(JWEObject jwt) {
		if (getDefaultEncryptionKeyId() == null) {
			throw new IllegalStateException("Tried to call default encryption with no default encrypter ID set");
		}

		JWEEncrypter encrypter = encrypters.get(getDefaultEncryptionKeyId());

		try {
			jwt.encrypt(encrypter);
		} catch (JOSEException e) {
			log.error("Failed to encrypt JWT, error was: ", e);
		}
	}

	@Override
	public void decryptJwt(JWEObject jwt) {
		if (getDefaultDecryptionKeyId() == null) {
			throw new IllegalStateException("Tried to call default decryption with no default decrypter ID set");
		}

		JWEDecrypter decrypter = decrypters.get(getDefaultDecryptionKeyId());

		try {
			jwt.decrypt(decrypter);
		} catch (JOSEException e) {
			log.error("Failed to decrypt JWT, error was: ", e);
		}
	}

	@Override
	public Collection<JWEAlgorithm> getAllEncryptionAlgsSupported() {
		Set<JWEAlgorithm> algs = new HashSet<>();

		for (JWEEncrypter encrypter : encrypters.values()) {
			algs.addAll(encrypter.supportedJWEAlgorithms());
		}

		for (JWEDecrypter decrypter : decrypters.values()) {
			algs.addAll(decrypter.supportedJWEAlgorithms());
		}

		return algs;
	}

	@Override
	public Collection<EncryptionMethod> getAllEncryptionEncsSupported() {
		Set<EncryptionMethod> encs = new HashSet<>();

		for (JWEEncrypter encrypter : encrypters.values()) {
			encs.addAll(encrypter.supportedEncryptionMethods());
		}

		for (JWEDecrypter decrypter : decrypters.values()) {
			encs.addAll(decrypter.supportedEncryptionMethods());
		}

		return encs;
	}

	@Override
	public Map<String, JWK> getAllPublicKeys() {
		Map<String, JWK> pubKeys = new HashMap<>();

		for (String keyId : keys.keySet()) {
			JWK key = keys.get(keyId);
			JWK pub = key.toPublicJWK();
			if (pub != null) {
				pubKeys.put(keyId, pub);
			}
		}

		return pubKeys;
	}

	/**
	 * Builds all the encrypters and decrypters for this service based on the key map.
	 * @throws
	 * @throws JOSEException
	 */
	private void buildEncryptersAndDecrypters() throws JOSEException {
		for (Map.Entry<String, JWK> jwkEntry : keys.entrySet()) {
			String id = jwkEntry.getKey();
			JWK jwk = jwkEntry.getValue();

			if (jwk instanceof RSAKey) {
				handleRSAKey(id, jwk);
			} else if (jwk instanceof ECKey) {
				handleECKey(id, jwk);
			} else if (jwk instanceof OctetSequenceKey) {
				handleOctetSeqKey(id, jwk);
			} else {
				log.warn("Unknown key type: {}", jwk);
			}
		}
	}

	private void handleOctetSeqKey(String id, JWK jwk) throws KeyLengthException {
		DirectEncrypter encrypter = new DirectEncrypter((OctetSequenceKey) jwk);
		encrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
		DirectDecrypter decrypter = new DirectDecrypter((OctetSequenceKey) jwk);
		decrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());

		encrypters.put(id, encrypter);
		decrypters.put(id, decrypter);
	}

	private void handleECKey(String id, JWK jwk) throws JOSEException {
		ECDHEncrypter encrypter = new ECDHEncrypter((ECKey) jwk);
		encrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
		encrypters.put(id, encrypter);

		if (jwk.isPrivate()) { // we can decrypt too
			ECDHDecrypter decrypter = new ECDHDecrypter((ECKey) jwk);
			decrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
			decrypters.put(id, decrypter);
		} else {
			log.warn("No private key for key #{}", jwk.getKeyID());
		}
	}

	private void handleRSAKey(String id, JWK jwk) throws JOSEException {
		RSAEncrypter encrypter = new RSAEncrypter((RSAKey) jwk);
		encrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
		encrypters.put(id, encrypter);

		if (jwk.isPrivate()) { // we can decrypt!
			RSADecrypter decrypter = new RSADecrypter((RSAKey) jwk);
			decrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
			decrypters.put(id, decrypter);
		} else {
			log.warn("No private key for key #{}", jwk.getKeyID());
		}
	}

}
