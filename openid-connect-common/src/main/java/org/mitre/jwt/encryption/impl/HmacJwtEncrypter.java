package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class HmacJwtEncrypter {
	
	private byte[] passphrase;
	private String algorithm;
	private byte[] wrappedKey;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	public HmacJwtEncrypter(String algorithm, byte[] passphraseAsBytes){
		setPassphrase(passphraseAsBytes);
	}

	public byte[] getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(byte[] passphrase) {
		this.passphrase = passphrase;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
	public byte[] getWrappedKey() {
		return wrappedKey;
	}

	public void setWrappedKey(byte[] wrappedKey) {
		this.wrappedKey = wrappedKey;
	}

	public Key createEncryptedKey() {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			
			SecretKeySpec keySpec = new SecretKeySpec(passphrase, algorithm);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			privateKey = keyFactory.generatePrivate(keySpec);
			publicKey = keyFactory.generatePublic(keySpec);
	
			cipher.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = cipher.wrap(privateKey);
			
			SecretKeySpec wrappedKeySpec = new SecretKeySpec(wrappedKey, algorithm);
			privateKey = keyFactory.generatePrivate(wrappedKeySpec);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return privateKey;
	}

}
