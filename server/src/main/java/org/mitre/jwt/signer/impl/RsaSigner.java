package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mitre.jwt.signer.AbstractJwtSigner;
import org.mitre.jwt.signer.service.impl.KeyStore;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * JWT Signer using either the RSA SHA-256, SHA-384, SHA-512 hash algorithm
 * 
 * @author AANGANES, nemonik
 * 
 */
public class RsaSigner extends AbstractJwtSigner implements InitializingBean {

	/**
	 * an enum for mapping a JWS name to standard algorithm name
	 * 
	 * @author nemonik
	 * 
	 */
	public enum Algorithm {

		// Algorithm constants
		RS256("SHA256withRSA"), RS384("SHA384withRSA"), RS512("SHA512withRSA");

		public static final String DEFAULT = Algorithm.RS256.toString();

		/**
		 * Returns the Algorithm for the name
		 * 
		 * @param name
		 * @return
		 */
		public static Algorithm getByName(String name) {
			for (Algorithm correspondingType : Algorithm.values()) {
				if (correspondingType.toString().equals(name)) {
					return correspondingType;
				}
			}

			// corresponding type not found
			throw new IllegalArgumentException(
					"Algorithm name does not have a corresponding Algorithm");
		}

		private final String standardName;

		/**
		 * Constructor of Algorithm
		 * 
		 * @param standardName
		 */
		Algorithm(String standardName) {
			this.standardName = standardName;
		}

		/**
		 * Return the Java standard algorithm name
		 * 
		 * @return
		 */
		public String getStandardName() {
			return standardName;
		}
	};

	private static Log logger = LogFactory.getLog(RsaSigner.class);

	public static final String DEFAULT_PASSWORD = "changeit";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private KeyStore keystore;
	private String alias;
	private String password;

	private RSAPrivateKey privateKey;
	private RSAPublicKey publicKey;
	private Signature signer;

	/**
	 * Default constructor
	 */
	public RsaSigner() {
		this(Algorithm.DEFAULT, null, null, DEFAULT_PASSWORD);
	}

	/**
	 * @param algorithmName
	 * @param keystore
	 * @param alias
	 */
	public RsaSigner(String algorithmName, KeyStore keystore, String alias) {
		this(algorithmName, keystore, alias, DEFAULT_PASSWORD);
	}

	/**
	 * @param algorithmName
	 * @param keystore
	 * @param alias
	 * @param password
	 */
	public RsaSigner(String algorithmName, KeyStore keystore, String alias,
			String password) {
		super(algorithmName);

		setKeystore(keystore);
		setAlias(alias);
		setPassword(password);

		try {
			signer = Signature.getInstance(Algorithm.getByName(algorithmName).getStandardName(), "BC");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		KeyPair keyPair = keystore.getKeyPairForAlias(alias, password);

		publicKey = (RSAPublicKey) keyPair.getPublic();
		privateKey = (RSAPrivateKey) keyPair.getPrivate();

		logger.debug("RSA Signer ready for business");

	}

	@Override
	protected String generateSignature(String signatureBase) {

		try {
			signer.initSign(privateKey);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			signer.update(signatureBase.getBytes("UTF-8"));
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] sigBytes;
		String sig = "";

		try {
			sigBytes = signer.sign();
			sig = new String(Base64.encodeBase64URLSafe(sigBytes));
			// strip off any padding
			sig = sig.replace("=", "");
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return "RsaSigner [keystore=" + keystore + ", alias=" + alias
				+ ", password=" + password + ", privateKey=" + privateKey
				+ ", publicKey=" + publicKey + ", signer=" + signer + "]";
	}

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
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		try {
			signer.update(signingInput.getBytes("UTF-8"));
		} catch (SignatureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			signer.verify(s64.getBytes("UTF-8"));
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
