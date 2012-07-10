package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweDecrypter;
import org.mitre.jwt.signer.impl.HmacSigner;


public class RsaDecrypter extends AbstractJweDecrypter {
	
	public RsaDecrypter() {
		//TODO: Put something here
	}
	
	@Override
	public Jwe decrypt(String encryptedJwe) {
		
		Jwe jwe = Jwe.parse(encryptedJwe);
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {

			PrivateKey contentEncryptionKey = null;
			PublicKey contentIntegrityKey = null;
			
			try {
				
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance(jwe.getHeader().getKeyDerivationFunction());
				KeyPair keyPair = keyGen.genKeyPair();
				contentEncryptionKey = keyPair.getPrivate();
				contentIntegrityKey = keyPair.getPublic();
				
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			jwe.setCiphertext(decryptCipherText(jwe, contentEncryptionKey).getBytes());
			jwe.setEncryptedKey(decryptEncryptionKey(jwe));
			
			//generate signature for decrypted signature base in order to verify that decryption worked
			String signature = null;
			try {
				HmacSigner hmacSigner = new HmacSigner(contentIntegrityKey.getEncoded());
				signature = hmacSigner.generateSignature(jwe.getSignatureBase());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//verifys that the signature base was decrypted correctly
			if(signature != jwe.getSignature()){
				throw new IllegalArgumentException("Didn't decrypt correctly. Decoded Sig and generated Sig do not match");
			}
			
		} else if(alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){
			
			throw new IllegalArgumentException("Cannot use Hmac for decryption");
			
		} else {
			throw new IllegalArgumentException("Not a valid decrypting algorithm");
		}
		return jwe;
	}

	@Override
	public String decryptCipherText(Jwe jwe, Key cek) {
		Cipher cipher;
		String clearTextString = null;
		try {
			
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, cek);
			byte[] clearText = cipher.doFinal(jwe.getCiphertext());
			clearTextString = new String(clearText);
			
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
		
		return clearTextString;
		
	}

	@Override
	public byte[] decryptEncryptionKey(Jwe jwe) {
		Cipher cipher;
		byte[] contentMasterKey = null;
		
		try {
			
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);//TODO: Get private key from key store. Placeholder
			contentMasterKey = cipher.doFinal(jwe.getEncryptedKey());
			
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
	
		return contentMasterKey;
	}

}
