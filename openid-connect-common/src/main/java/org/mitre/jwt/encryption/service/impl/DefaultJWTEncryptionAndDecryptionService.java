/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.jwt.encryption.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.mitre.jose.keystore.JWKSetKeyStore;
import org.mitre.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEObject;
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

/**
 * @author wkim
 *
 */
public class DefaultJWTEncryptionAndDecryptionService implements JWTEncryptionAndDecryptionService {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(DefaultJWTEncryptionAndDecryptionService.class);

	// map of identifier to encrypter
	private Map<String, JWEEncrypter> encrypters = new HashMap<>();

	// map of identifier to decrypter
	private Map<String, JWEDecrypter> decrypters = new HashMap<>();

	private String defaultEncryptionKeyId;

	private String defaultDecryptionKeyId;

	private JWEAlgorithm defaultAlgorithm;

	// map of identifier to key
	private Map<String, JWK> keys = new HashMap<>();

	/**
	 * Build this service based on the keys given. All public keys will be used to make encrypters,
	 * all private keys will be used to make decrypters.
	 *
	 * @param keys
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws JOSEException
	 */
	public DefaultJWTEncryptionAndDecryptionService(Map<String, JWK> keys) throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
		this.keys = keys;
		buildEncryptersAndDecrypters();
	}

	/**
	 * Build this service based on the given keystore. All keys must have a key
	 * id ({@code kid}) field in order to be used.
	 *
	 * @param keyStore
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws JOSEException
	 */
	public DefaultJWTEncryptionAndDecryptionService(JWKSetKeyStore keyStore) throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {

		// convert all keys in the keystore to a map based on key id
		for (JWK key : keyStore.getKeys()) {
			if (!Strings.isNullOrEmpty(key.getKeyID())) {
				this.keys.put(key.getKeyID(), key);
			} else {
				throw new IllegalArgumentException("Tried to load a key from a keystore without a 'kid' field: " + key);
			}
		}

		buildEncryptersAndDecrypters();

	}


	@PostConstruct
	public void afterPropertiesSet() {

		if (keys == null) {
			throw new IllegalArgumentException("Encryption and decryption service must have at least one key configured.");
		}
		try {
			buildEncryptersAndDecrypters();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Encryption and decryption service could not find given algorithm.");
		} catch (InvalidKeySpecException e) {
			throw new IllegalArgumentException("Encryption and decryption service saw an invalid key specification.");
		} catch (JOSEException e) {
			throw new IllegalArgumentException("Encryption and decryption service was unable to process JOSE object.");
		}
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

	/* (non-Javadoc)
	 * @see org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService#encryptJwt(com.nimbusds.jwt.EncryptedJWT)
	 */
	@Override
	public void encryptJwt(JWEObject jwt) {
		if (getDefaultEncryptionKeyId() == null) {
			throw new IllegalStateException("Tried to call default encryption with no default encrypter ID set");
		}

		JWEEncrypter encrypter = encrypters.get(getDefaultEncryptionKeyId());

		try {
			jwt.encrypt(encrypter);
		} catch (JOSEException e) {

			logger.error("Failed to encrypt JWT, error was: ", e);
		}

	}

	/* (non-Javadoc)
	 * @see org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService#decryptJwt(com.nimbusds.jwt.EncryptedJWT)
	 */
	@Override
	public void decryptJwt(JWEObject jwt) {
		if (getDefaultDecryptionKeyId() == null) {
			throw new IllegalStateException("Tried to call default decryption with no default decrypter ID set");
		}

		JWEDecrypter decrypter = decrypters.get(getDefaultDecryptionKeyId());

		try {
			jwt.decrypt(decrypter);
		} catch (JOSEException e) {

			logger.error("Failed to decrypt JWT, error was: ", e);
		}

	}

	/**
	 * Builds all the encrypters and decrypters for this service based on the key map.
	 * @throws
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws JOSEException
	 */
	private void buildEncryptersAndDecrypters() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {

		for (Map.Entry<String, JWK> jwkEntry : keys.entrySet()) {

			String id = jwkEntry.getKey();
			JWK jwk = jwkEntry.getValue();

			if (jwk instanceof RSAKey) {
				// build RSA encrypters and decrypters

				RSAEncrypter encrypter = new RSAEncrypter((RSAKey) jwk); // there should always at least be the public key
				encrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
				encrypters.put(id, encrypter);

				if (jwk.isPrivate()) { // we can decrypt!
					RSADecrypter decrypter = new RSADecrypter((RSAKey) jwk);
					decrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
					decrypters.put(id, decrypter);
				} else {
					logger.warn("No private key for key #" + jwk.getKeyID());
				}
			} else if (jwk instanceof ECKey) {

				// build EC Encrypters and decrypters

				ECDHEncrypter encrypter = new ECDHEncrypter((ECKey) jwk);
				encrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
				encrypters.put(id, encrypter);

				if (jwk.isPrivate()) { // we can decrypt too
					ECDHDecrypter decrypter = new ECDHDecrypter((ECKey) jwk);
					decrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
					decrypters.put(id, decrypter);
				} else {
					logger.warn("No private key for key # " + jwk.getKeyID());
				}

			} else if (jwk instanceof OctetSequenceKey) {
				// build symmetric encrypters and decrypters

				DirectEncrypter encrypter = new DirectEncrypter((OctetSequenceKey) jwk);
				encrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
				DirectDecrypter decrypter = new DirectDecrypter((OctetSequenceKey) jwk);
				decrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());

				encrypters.put(id, encrypter);
				decrypters.put(id, decrypter);

			} else {
				logger.warn("Unknown key type: " + jwk);
			}

		}
	}

	@Override
	public Map<String, JWK> getAllPublicKeys() {
		Map<String, JWK> pubKeys = new HashMap<>();

		// pull out all public keys
		for (String keyId : keys.keySet()) {
			JWK key = keys.get(keyId);
			JWK pub = key.toPublicJWK();
			if (pub != null) {
				pubKeys.put(keyId, pub);
			}
		}

		return pubKeys;
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

	/* (non-Javadoc)
	 * @see org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService#getAllEncryptionEncsSupported()
	 */
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


}
