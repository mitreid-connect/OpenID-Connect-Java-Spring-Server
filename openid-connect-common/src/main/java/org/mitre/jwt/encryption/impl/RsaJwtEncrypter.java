package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class RsaJwtEncrypter {
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String algorithm;
	
	public RsaJwtEncrypter(String algorithm, RSAPublicKey pubKey, RSAPrivateKey privateKey){
		setAlgorithm(algorithm);
		setPublicKey(pubKey);
		setPrivateKey(privateKey);
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey pubKey) {
		this.publicKey = pubKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public Key createEncryptedKey() {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = cipher.wrap(privateKey);
			
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			SecretKeySpec keySpec = new SecretKeySpec(wrappedKey, algorithm);
			privateKey = keyFactory.generatePrivate(keySpec);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return privateKey;
	}

}
