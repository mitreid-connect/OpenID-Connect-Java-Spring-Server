package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
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
 * JWT Signer using either the ECDSA SHA-256, SHA-384, SHA-512 hash algorithm
 * 
 * @author AANGANES, nemonik
 * 
 *         Requires static install of BC
 * 
 */
public class EcdsaSigner extends AbstractJwtSigner implements InitializingBean {

	private static Log logger = LogFactory.getLog(EcdsaSigner.class);

	public static final String KEYPAIR_ALGORITHM = "EC";
	public static final String DEFAULT_PASSWORD = "changeit";

	private KeyStore keystore;
	private String alias;
	private String password = DEFAULT_PASSWORD;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Signature signer;

	public static final String DEFAULT_ALGORITHM = JwsAlgorithm.ES256.toString();
	//public static final String PREPEND = "ES";

	/**
	 * Default constructor
	 */
	public EcdsaSigner() {
		super(DEFAULT_ALGORITHM);
	}

	/**
	 * Creates an EcdsaSigner from an algorithm name, a Java Keystore, an alias
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
	public EcdsaSigner(String algorithmName, KeyStore keystore, String alias)
			throws GeneralSecurityException {
		this(algorithmName, keystore, alias, DEFAULT_PASSWORD);
	}

	/**
	 * Creates an EcdsaSigner from an algorithm name, a Java Keystore, an alias
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
	public EcdsaSigner(String algorithmName, KeyStore keystore, String alias,
			String password) throws GeneralSecurityException {
		super(algorithmName);

		Assert.notNull(keystore, "A keystore must be supplied");
		Assert.notNull(alias, "A alias must be supplied");		
		Assert.notNull(password, "A password must be supplied");
		
		setKeystore(keystore);
		setAlias(alias);
		setPassword(password);

		KeyPair keyPair = keystore.getKeyPairForAlias(alias, password);

		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();		
		
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
	public EcdsaSigner(String algorithmName, PublicKey publicKey, PrivateKey privateKey) {
		super(algorithmName);
		
		Assert.notNull(publicKey, "A publicKey must be supplied");	
		Assert.notNull(privateKey, "A privateKey must be supplied");	
		
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
	public void afterPropertiesSet() throws Exception {
		
		// Can throw a GeneralException
		signer = Signature.getInstance(JwsAlgorithm.getByName(super.getAlgorithm()).getStandardName()); // PROVIDER);
		
		logger.debug(JwsAlgorithm.getByName(getAlgorithm()).getStandardName() + " ECDSA Signer ready for business");
	}

	/* (non-Javadoc)
	 * @see org.mitre.jwt.signer.AbstractJwtSigner#generateSignature(java.lang.String)
	 */
	@Override
	public String generateSignature(String signatureBase) {

		String sig = null;

		try {

			signer.initSign(privateKey);
			signer.update(signatureBase.getBytes("UTF-8"));

			byte[] sigBytes = signer.sign();
			sig = new String(Base64.encodeBase64URLSafe(sigBytes));

			// strip off any padding
			sig = sig.replace("=", "");

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EcdsaSigner [keystore=" + keystore + ", alias=" + alias
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
			signer.initVerify(publicKey);
			signer.update(signingInput.getBytes("UTF-8"));
			signer.verify(s64.getBytes("UTF-8"));
		} catch (GeneralSecurityException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}

		return true;
	}
}