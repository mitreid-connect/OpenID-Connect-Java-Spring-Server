package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RsaJwtDecrypter {
	
	PublicKey publicKey;
	byte[] wrappedKey;
	String algorithm;
	
	public RsaJwtDecrypter(String algorithm, PublicKey publicKey, byte[] wrappedKey){
		setPublicKey(publicKey);
		setWrappedKey(wrappedKey);
		setAlgorithm(algorithm);
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public byte[] getWrappedKey() {
		return wrappedKey;
	}

	public void setWrappedKey(byte[] wrappedKey) {
		this.wrappedKey = wrappedKey;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public Key keyDecrypter() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.UNWRAP_MODE, publicKey);
		Key unwrappedKey = cipher.unwrap(wrappedKey, algorithm, Cipher.PRIVATE_KEY);
		
		return unwrappedKey;
	}

}
