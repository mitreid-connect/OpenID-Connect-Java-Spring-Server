package org.mitre.jwt.encryption;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.mitre.jwe.model.Jwe;
import org.springframework.security.crypto.codec.Base64;


public abstract class AbstractJweEncrypter implements JwtEncrypter {
	
	private byte[] encryptedKey; 
	
	private byte[] cipherText;
	
	private RSAPublicKey publicKey;
	
	public byte[] getEncryptecKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(byte[] encryptedKey) {
		this.encryptedKey = encryptedKey;
	}

	public byte[] getCipherText() {
		return cipherText;
	}

	public void setCipherText(byte[] cipherText) {
		this.cipherText = cipherText;
	}

	
	public byte[] encryptKey(Jwe jwe){
		
		//generate random content master key
		PublicKey contentMasterKey = null;
		
		try {
			
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(jwe.getHeader().getAlgorithm());
			SecureRandom random = SecureRandom.getInstance(jwe.getHeader().getAlgorithm());
			keyGen.initialize(1024, random);
			KeyPair pair = keyGen.generateKeyPair();
			contentMasterKey = pair.getPublic();
			
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//TODO:Get public key from keystore, currently null
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encryptedKey = cipher.doFinal(contentMasterKey.getEncoded());
			
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
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return encryptedKey;
		
	}
	
	public byte[] encryptClaims(Jwe jwe, Key cek) {
		
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
			
			//TODO: generated the iv, but not sure how to use it to encrypt?
			IvParameterSpec spec = cipher.getParameters().getParameterSpec(IvParameterSpec.class);
			byte[] iv = spec.getIV();
			
			cipher.init(Cipher.ENCRYPT_MODE, cek);
			cipherText = cipher.doFinal(jwe.getClaims().toString().getBytes());
			
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
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidParameterSpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cipherText;
		
	}
	
	public abstract Jwe encryptAndSign(Jwe jwe) throws NoSuchAlgorithmException;


}
