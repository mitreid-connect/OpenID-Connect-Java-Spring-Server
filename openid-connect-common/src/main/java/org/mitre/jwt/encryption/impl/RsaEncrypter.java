package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweEncrypter;
import org.mitre.jwt.encryption.AlgorithmLength;
import org.mitre.jwt.signer.impl.HmacSigner;

public class RsaEncrypter extends AbstractJweEncrypter {

	
	public RsaEncrypter() {
		//TODO: Put something here
	}

	public Jwe encryptAndSign(Jwe jwe) throws NoSuchAlgorithmException {
		
		String alg = jwe.getHeader().getAlgorithm();
		String iv = jwe.getHeader().getIntegrity();
		
		if(alg.equals("RSA1_5") || alg.equals("RSA-OAEP") || alg.equals("ECDH-ES") || alg.equals("A128KW") || alg.equals("A256KW")) {
			
			//generate random content master key
			Key contentMasterKey = null;
			
			try {
				
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
				SecureRandom random = SecureRandom.getInstance("RSA");
				keyGen.initialize(1024, random);
				KeyPair pair = keyGen.generateKeyPair();
				contentMasterKey = pair.getPublic();
				
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//generate CEK and CIK
			
			String algorithmLength = AlgorithmLength.getByName(jwe.getHeader().getEncryptionMethod()).toString();
			int keyLength = Integer.parseInt(algorithmLength);
			byte[] contentEncryptionKey = generateContentKey(contentMasterKey.getEncoded(), keyLength, new String("Encryption").getBytes());
			byte[] contentIntegrityKey = generateContentKey(contentMasterKey.getEncoded(), keyLength, new String("Integrity").getBytes());
			
			//encrypt claims and cmk to get ciphertext and encrypted key
			jwe.setCiphertext(encryptClaims(jwe, contentEncryptionKey));
			jwe.setEncryptedKey(encryptKey(jwe, contentMasterKey));
			
			//Signer must be hmac
			if(iv.equals("HS256") || iv.equals("HS384") || iv.equals("HS512")){
			
				HmacSigner hmacSigner = new HmacSigner(contentIntegrityKey); 
				jwe = (Jwe) hmacSigner.sign(jwe);
				
			} else {
				throw new IllegalArgumentException("Not a valid integrity value algorithm");
			}
			
		} else {
			throw new IllegalArgumentException("Not a valid signing algorithm");
		}
		
		return jwe;
	}

	public byte[] encryptKey(Jwe jwe, Key cmk) {
		
		//TODO:Get public key from keystore, for now randomly generate key pair
		
		PublicKey publicKey = null;
		
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair pair = keyGen.generateKeyPair();
			publicKey = pair.getPublic();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Cipher cipher;
		byte[] encryptedKey = null;
		
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encryptedKey = cipher.doFinal(cmk.getEncoded());
			
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

	public byte[] encryptClaims(Jwe jwe, byte[] contentEncryptionKey) {
		
		Cipher cipher;
		byte[] cipherText = null;
		
		try {
			cipher = Cipher.getInstance("RSA");
			
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(contentEncryptionKey, "RSA"));
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
		} 
		
		return cipherText;
		
	}
	


}
