/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
 ******************************************************************************/
package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.signer.AbstractJwtSigner;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.jwt.signer.service.impl.KeyStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * JWT Signer using either the RSA SHA-256, SHA-384, SHA-512 hash algorithm
 * 
 * @author AANGANES, nemonik
 * 
 */
public class RsaSigner extends AbstractJwtSigner implements InitializingBean {

	private static Log logger = LogFactory.getLog(RsaSigner.class);

	public static final String KEYPAIR_ALGORITHM = "RSA";
	public static final String DEFAULT_PASSWORD = "changeit";

	public static final String DEFAULT_ALGORITHM = JwsAlgorithm.RS256.toString();
	
	private KeyStore keystore;
	private String alias;
	private String password = DEFAULT_PASSWORD;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Signature signer;

	/**
	 * Default constructor
	 */
	public RsaSigner() {
		super(DEFAULT_ALGORITHM);
	}

	/**
	 * Creates an RsaSigner from an algorithm name, a Java Keystore, an alias
	 * for the key pair, and the default password to access. Key pairs created
	 * with larger bit sizes obviously create larger signatures.
	 * 
	 * @param algorithmName
	 *            The algorithm name
	 * @param keystore
	 *            A Java Keystore containing the key pair
	 * @param alias
	 *            The alias for the key pair
	 * @throws GeneralSecurityException
	 */
	public RsaSigner(String algorithmName, KeyStore keystore, String alias)
			throws GeneralSecurityException {
		this(algorithmName, keystore, alias, DEFAULT_PASSWORD);
	}

	/**
	 * Creates an RsaSigner from an algorithm name, a Java Keystore, an alias
	 * for the key pair, and the password to access. Key pairs created with
	 * larger bit sizes obviously create larger signatures.
	 * 
	 * @param algorithmName
	 *            The algorithm name
	 * @param keystore
	 *            A Java Keystore containing the key pair
	 * @param alias
	 *            The alias for the key pair
	 * @param password
	 *            The password used to access and retrieve the key pair.
	 * @throws GeneralSecurityException
	 */
	public RsaSigner(String algorithmName, KeyStore keystore, String alias, String password) throws GeneralSecurityException {
		super(algorithmName);

		setKeystore(keystore);
		setAlias(alias);
		setPassword(password);

		loadKeysFromKeystore();
	}

	/**
	 * Creates an RsaSigner from an algorithm name, and key pair. Key pairs
	 * created with larger bit sizes obviously create larger signatures.
	 * 
	 * @param algorithmName
	 *            The algorithm name
	 * @param publicKey
	 *            The public key
	 * @param privateKey
	 *            The private key
	 */
	public RsaSigner(String algorithmName, PublicKey publicKey, PrivateKey privateKey) {
		super(algorithmName);

		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws NoSuchAlgorithmException, GeneralSecurityException {

		// unsupported algorithm will throw a NoSuchAlgorithmException
		signer = Signature.getInstance(JwsAlgorithm.getByName(super.getAlgorithm()).getStandardName()); // ,PROVIDER);

		loadKeysFromKeystore();
		
		logger.debug(JwsAlgorithm.getByName(getAlgorithm()).getStandardName() + " RSA Signer ready for business");

	}

	/**
	 * Load the public and private keys from the keystore, identified with the configured alias and accessed with the configured password.
	 * @throws GeneralSecurityException
	 */
	private void loadKeysFromKeystore() throws GeneralSecurityException {
		Assert.notNull(keystore, "An keystore must be supplied");
		Assert.notNull(alias, "A alias must be supplied");
		Assert.notNull(password, "A password must be supplied");

	    KeyPair keyPair = keystore.getKeyPairForAlias(alias, password);

		Assert.notNull(keyPair, "Either alias and/or password is not correct for keystore");
		
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.AbstractJwtSigner#generateSignature(java.lang.String
	 * )
	 */
	@Override
	public String generateSignature(String signatureBase) throws NoSuchAlgorithmException {

		String sig = null;
		try {
			initializeSigner();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			signer.initSign(privateKey);
			signer.update(signatureBase.getBytes("UTF-8"));

			byte[] sigBytes = signer.sign();

			sig = (new String(Base64.encodeBase64URLSafe(sigBytes))).replace("=", "");
		} catch (GeneralSecurityException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}

		return sig;
	}

	public String getAlias() {
		return alias;
	}

	public KeyStore getKeystore() {
		return keystore;
	}

	public String getPassword() {
		return password;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setKeystore(KeyStore keyStore) {
		this.keystore = keyStore;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPrivateKey(RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
	public void initializeSigner() throws NoSuchAlgorithmException{
		signer = Signature.getInstance(JwsAlgorithm.getByName(super.getAlgorithm()).getStandardName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RsaSigner [keystore=" + keystore + ", alias=" + alias
				+ ", password=" + password + ", privateKey=" + privateKey
				+ ", publicKey=" + publicKey + ", signer=" + signer + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mitre.jwt.signer.AbstractJwtSigner#verify(java.lang.String)
	 */
	@Override
	public boolean verify(String jwtString) {

		boolean value = false;

		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(
				jwtString));

		if (parts.size() != 3) {
			throw new IllegalArgumentException("Invalid JWT format.");
		}

		String h64 = parts.get(0);
		String c64 = parts.get(1);
		String s64 = parts.get(2);

		String signingInput = h64 + "." + c64;

		try {
			initializeSigner();
			signer.initVerify(publicKey);
			signer.update(signingInput.getBytes("UTF-8"));
			value = signer.verify(Base64.decodeBase64(s64));
		} catch (GeneralSecurityException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}

		return value;
	}
}
