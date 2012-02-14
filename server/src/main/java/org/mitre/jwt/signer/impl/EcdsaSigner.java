package org.mitre.jwt.signer.impl;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.mitre.jwt.signer.AbstractJwtSigner;
import org.mitre.jwt.signer.service.impl.KeyStore;
import org.springframework.beans.factory.InitializingBean;

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

		private static final String DEFAULT = Algorithm.ES256.toString();
		
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
	
	private KeyStore keystore;
	private String alias;
	private String password;
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Signature signer;	
	
	public EcdsaSigner() {
		this(Algorithm.DEFAULT, null, null, null);
	}

	public EcdsaSigner(String algorithmName, KeyStore keystore, String alias, String password) {
		super(algorithmName);

		setKeystore(keystore);
		setAlias(alias);
		setPassword(password);

		try {
			signer = Signature.getInstance(Algorithm.getByName(algorithmName).getStandardName());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	

	@Override
	public void afterPropertiesSet() throws Exception {
		KeyPair keyPair = keystore.getKeyPairForAlias(alias, password);
		
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
	}

	@Override
	protected String generateSignature(String signatureBase) {

		/*
		1) Generate a digital signature of the UTF-8 representation of the JWS Signing Input 
			using ECDSA P-256 SHA-256 with the desired private key. The output will be the 
			EC point (R, S), where R and S are unsigned integers.
		2) Turn R and S into byte arrays in big endian order. Each array will be 32 bytes long.
		3) Concatenate the two byte arrays in the order R and then S.
		4) Base64url encode the resulting 64 byte array.
		 */

		return null;
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

	@Override
	public boolean verify(String jwtString) {

		/*
		1) Take the Encoded JWS Signature and base64url decode it into a byte array. 
			If decoding fails, the signed content MUST be rejected.
		2) The output of the base64url decoding MUST be a 64 byte array.
		3) Split the 64 byte array into two 32 byte arrays. The first array will be R and 
			the second S. Remember that the byte arrays are in big endian byte order; 
			please check the ECDSA validator in use to see what byte order it requires.
		4) Submit the UTF-8 representation of the JWS Signing Input, R, S and the public 
			key (x, y) to the ECDSA P-256 SHA-256 validator.
		5) If the validation fails, the signed content MUST be rejected.
		*/

		return false;
	}	

}