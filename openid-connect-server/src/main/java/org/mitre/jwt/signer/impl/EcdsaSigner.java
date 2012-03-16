package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.signer.AbstractJwtSigner;
import org.mitre.jwt.signer.service.impl.KeyStore;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * JWT Signer using either the ECDSA SHA-256, SHA-384, SHA-512 hash algorithm
 * 
 * @author AANGANES, nemonik
 * 
 * Requires static install of BC
 *
 */
public class EcdsaSigner extends AbstractJwtSigner implements InitializingBean {

	/**
	 * an enum for mapping a JWS name to standard algorithm name
	 * 
	 * @author nemonik
	 *
	 */
	public enum Algorithm {

		//Algorithm constants
		ES256("SHA256withECDSA"),
		ES384("SHA384withECDSA"),
		ES512("SHA512withECDSA");

		public static final String DEFAULT = Algorithm.ES256.toString();
		public static final String PREPEND = "ES";
		
		/**
    	 * Returns the Algorithm for the name
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
    		throw new IllegalArgumentException("Algorithm name does not have a corresponding Algorithm");
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
    	 * @return
    	 */
    	public String getStandardName() {
    		return standardName;
    	}
	};	
	
	static final String PROVIDER = "BC";
	
	private static Log logger = LogFactory.getLog(EcdsaSigner.class);
	
	public static final String KEYPAIR_ALGORITHM = "EC";	
	public static final String DEFAULT_PASSWORD = "changeit";	
	
	private KeyStore keystore;
	private String alias;
	private String password;
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Signature signer;	
	
	/**
	 * Default constructor
	 */
	public EcdsaSigner() {
		this(Algorithm.DEFAULT, null, null, DEFAULT_PASSWORD);
	}
	
	/**
	 * @param algorithmName
	 * @param keystore
	 * @param alias
	 */
	public EcdsaSigner(String algorithmName, KeyStore keystore, String alias) {
		this(algorithmName, keystore, alias, DEFAULT_PASSWORD);
	}
	
	/**
	 * @param algorithmName
	 * @param keystore
	 * @param alias
	 * @param password
	 */
	public EcdsaSigner(String algorithmName, KeyStore keystore, String alias, String password) {
		super(algorithmName);

		setKeystore(keystore);
		setAlias(alias);
		setPassword(password);

		try {
			signer = Signature.getInstance(Algorithm.getByName(algorithmName).getStandardName(), PROVIDER);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * @param algorithmName
	 * @param publicKey
	 * @param privateKey
	 */
	public EcdsaSigner(String algorithmName, PublicKey publicKey, PrivateKey privateKey) {
		super(algorithmName);
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		KeyPair keyPair = keystore.getKeyPairForAlias(alias, password);
		
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		
		logger.debug( Algorithm.getByName(getAlgorithm()).getStandardName() + " ECDSA Signer ready for business");
	}

	@Override
	protected String generateSignature(String signatureBase) {

		try {			
			signer.initSign(privateKey);
			signer.update(signatureBase.getBytes("UTF-8"));
		} catch (GeneralSecurityException e) {
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
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EcdsaSigner [keystore=" + keystore + ", alias=" + alias
				+ ", password=" + password + ", privateKey=" + privateKey
				+ ", publicKey=" + publicKey + ", signer=" + signer + "]";
	}

	/* (non-Javadoc)
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}	

}