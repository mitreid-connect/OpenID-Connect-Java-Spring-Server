package org.mitre.jwt.encryption.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweDecrypter;
import org.mitre.jwt.signer.impl.HmacSigner;


public class RsaDecrypter extends AbstractJweDecrypter {
	
	public RsaDecrypter() {
		//TODO: Put something here
	}
	
	@Override
	public Jwe decrypt(String encryptedJwe, Key privateKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		Jwe jwe = Jwe.parse(encryptedJwe);
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RSA1_5") || alg.equals("RSA-OAEP") || alg.equals("ECDH-ES") || alg.equals("A128KW") || alg.equals("A256KW")) {
			
			//decrypt to get cmk to be used for cek and cik
			jwe.setEncryptedKey(decryptEncryptionKey(jwe, privateKey));
			
			//generation of cek and cik
			byte[] contentEncryptionKey = null;
			byte[] contentIntegrityKey = null;
			//check whether the key length is 128 or 256
			if(jwe.getHeader().getEncryptionMethod().equals("A128CBC") || jwe.getHeader().getEncryptionMethod().equals("A128GCM")){
				contentEncryptionKey = generateContentKey(jwe.getEncryptedKey(), 128, new String("Encryption").getBytes());
				contentIntegrityKey = generateContentKey(jwe.getEncryptedKey(), 128, new String("Integrity").getBytes());
			} else if(jwe.getHeader().getEncryptionMethod().equals("A256CBC") || jwe.getHeader().getEncryptionMethod().equals("A256GCM")){
				contentEncryptionKey = generateContentKey(jwe.getEncryptedKey(), 256, new String("Encryption").getBytes());
				contentIntegrityKey = generateContentKey(jwe.getEncryptedKey(), 256, new String("Integrity").getBytes());
			} else {
				throw new IllegalArgumentException(jwe.getHeader().getEncryptionMethod() + " is not a valid encryption method");
			}
			
			//decrypt ciphertext to get claims
			jwe.setCiphertext(decryptCipherText(jwe, contentEncryptionKey));
			
			//generate signature for decrypted signature base in order to verify that decryption worked
			String signature = null;
			try {
				HmacSigner hmacSigner = new HmacSigner(contentIntegrityKey);
				signature = hmacSigner.generateSignature(jwe.getSignatureBase());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//verifys that the signature base was decrypted correctly
			/*if(signature != jwe.getSignature()){
				throw new IllegalArgumentException("Didn't decrypt correctly. Decoded Sig and generated Sig do not match. " +
						"Generated Signature is: " + signature + " while decoded sig is: " + jwe.getSignature());
			}*/
			
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getEncryptionMethod() + " is not a valid decrypting algorithm");
		}
		return jwe;
	}

	@Override
	public byte[] decryptCipherText(Jwe jwe, byte[] cek) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		byte[] iv = new byte[16];
		iv = Base64.decodeBase64(jwe.getHeader().getInitializationVector());
		
		String encMethod = jwe.getHeader().getEncryptionMethod();
		
		if(encMethod.equals("A128CBC") || encMethod.equals("A256CBC") || encMethod.equals("A128GCM") || encMethod.equals("A128GCM")) {
			
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cek, "AES"), new IvParameterSpec(iv));
		byte[] clearText = cipher.doFinal(jwe.getCiphertext());
			
		return clearText;
			
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getAlgorithm() + " is not an implemented algorithm");
		}

		
	}

	@Override
	public byte[] decryptEncryptionKey(Jwe jwe, Key privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		if(jwe.getHeader().getAlgorithm().equals("RSA1_5")){
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			//TODO: Get private key from key store. Placeholder
			byte[] contentMasterKey = cipher.doFinal(jwe.getEncryptedKey());
		
			return contentMasterKey;
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getAlgorithm() + " is not an implemented algorithm");
		}

	}

}
