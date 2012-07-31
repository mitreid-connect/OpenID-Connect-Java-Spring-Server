package org.mitre.jwt.encryption.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweDecrypter;
import org.mitre.jwt.encryption.JweAlgorithms;


public class RsaDecrypter extends AbstractJweDecrypter {
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	@Override
	public Jwe decrypt(String encryptedJwe) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		Jwe jwe = Jwe.parse(encryptedJwe);
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RSA1_5") || alg.equals("RSA-OAEP") || alg.equals("ECDH-ES") || alg.equals("A128KW") || alg.equals("A256KW")) {
			
			//decrypt to get cmk to be used for cek and cik
			jwe.setEncryptedKey(decryptEncryptionKey(jwe));
			
			//generation of cek and cik
			byte[] contentEncryptionKey = null;
			//check what the key length is
			String kdf = jwe.getHeader().getKeyDerivationFunction();
			String keyLength = JweAlgorithms.getByName(kdf);
			int keyBitLength = Integer.parseInt(keyLength);
			//generate cek and cik
			contentEncryptionKey = generateContentKey(jwe.getEncryptedKey(), keyBitLength, "Encryption".getBytes());
			
			//decrypt ciphertext to get claims
			jwe.setCiphertext(decryptCipherText(jwe, contentEncryptionKey));
			
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getEncryptionMethod() + " is not a valid decrypting algorithm");
		}
		return jwe;
	}

	@Override
	public byte[] decryptCipherText(Jwe jwe, byte[] contentEncryptionKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		byte[] iv = new byte[16];
		iv = Base64.decodeBase64(jwe.getHeader().getInitializationVector());
		
		String encMethod = jwe.getHeader().getEncryptionMethod();
		//TODO: should also check for A128GCM and A256GCM, but Cipher.getInstance() does not support the GCM mode. For now, don't use them
		if(encMethod.equals("A128CBC") || encMethod.equals("A256CBC")) {

			String mode = JweAlgorithms.getByName(encMethod);
			
			Cipher cipher = Cipher.getInstance("AES/" + mode + "/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(contentEncryptionKey, "AES"), new IvParameterSpec(iv));
			byte[] clearText = cipher.doFinal(jwe.getCiphertext());
			
			return clearText;
			
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getEncryptionMethod() + " is not an implemented encryption method");
		}

		
	}

	@Override
	public byte[] decryptEncryptionKey(Jwe jwe) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		if(jwe.getHeader().getAlgorithm().equals("RSA1_5")){
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
			byte[] contentMasterKey = cipher.doFinal(jwe.getEncryptedKey());
		
			return contentMasterKey;
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getAlgorithm() + " is not an implemented algorithm");
		}

	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

}
