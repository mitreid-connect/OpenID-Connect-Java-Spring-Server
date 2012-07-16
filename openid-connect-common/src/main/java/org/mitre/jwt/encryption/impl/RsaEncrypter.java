package org.mitre.jwt.encryption.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweEncrypter;
import org.mitre.jwt.signer.impl.HmacSigner;

public class RsaEncrypter extends AbstractJweEncrypter {

	
	public RsaEncrypter() {
		//TODO: Put something here
	}

	public Jwe encryptAndSign(Jwe jwe, Key publicKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		
		String alg = jwe.getHeader().getAlgorithm();
		String integrityAlg = jwe.getHeader().getIntegrity();
		
		if(alg.equals("RSA1_5") || alg.equals("RSA-OAEP") || alg.equals("ECDH-ES") || alg.equals("A128KW") || alg.equals("A256KW")) {
			
			//generate random content master key

			//check what the key length is
			String encMethod = jwe.getHeader().getEncryptionMethod();
			char[] array = encMethod.toCharArray();
			String keyBitLengthString = String.copyValueOf(array, 1, 3);
			int keyBitLength = Integer.parseInt(keyBitLengthString);
			
			byte[] contentMasterKey = new byte[keyBitLength];
			new Random().nextBytes(contentMasterKey);

			byte[] contentEncryptionKey = null;
			byte[] contentIntegrityKey = null;
			
			//generate cek and cik
			contentEncryptionKey = generateContentKey(contentMasterKey, keyBitLength, "Encryption".getBytes());
			contentIntegrityKey = generateContentKey(contentMasterKey, keyBitLength, "Integrity".getBytes());
			
			//encrypt claims and cmk to get ciphertext and encrypted key
			jwe.setCiphertext(encryptClaims(jwe, contentEncryptionKey));
			jwe.setEncryptedKey(encryptKey(jwe, contentMasterKey, publicKey));
			
			//Signer must be hmac
			if(integrityAlg.equals("HS256") || integrityAlg.equals("HS384") || integrityAlg.equals("HS512")){
			
				HmacSigner hmacSigner = new HmacSigner(contentIntegrityKey); 
				jwe = (Jwe) hmacSigner.sign(jwe);
				
			} else {
				throw new IllegalArgumentException(integrityAlg + " is not a valid integrity value algorithm for signing.");
			}
			
		} else {
			throw new IllegalArgumentException(alg + " is not a valid encrypting algorithm.");
		}
		
		return jwe;
	}

	public byte[] encryptKey(Jwe jwe, byte[] contentMasterKey, Key publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		if(jwe.getHeader().getAlgorithm().equals("RSA1_5")){
		
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedKey = cipher.doFinal(contentMasterKey);
		return encryptedKey;
		
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getAlgorithm() + " is not a supported algorithm");
		}
		
	}

	public byte[] encryptClaims(Jwe jwe, byte[] contentEncryptionKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		
		byte[] iv = new byte[16];
		//look for iv value in header, if not there make one
		if(jwe.getHeader().getInitializationVector() != null){
			iv = Base64.decodeBase64(jwe.getHeader().getInitializationVector());
		} else {
			new Random().nextBytes(iv);
			jwe.getHeader().setIv(Base64.encodeBase64String(iv));
		}
		
		String encMethod = jwe.getHeader().getEncryptionMethod();
		
		if(encMethod.equals("A128CBC") || encMethod.equals("A256CBC") || encMethod.equals("A128GCM") || encMethod.equals("A256GCM")) {

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(contentEncryptionKey, "AES"), new IvParameterSpec(iv));
			byte[] cipherText = cipher.doFinal(jwe.getCiphertext());
			return cipherText;

		} else {
			throw new IllegalArgumentException(jwe.getHeader().getEncryptionMethod() + " is not a supported encryption method");
		}

	}
}
