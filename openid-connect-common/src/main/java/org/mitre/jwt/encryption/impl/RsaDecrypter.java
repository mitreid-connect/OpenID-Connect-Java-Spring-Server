package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweDecrypter;
import org.mitre.jwt.encryption.AlgorithmLength;
import org.mitre.jwt.signer.impl.HmacSigner;


public class RsaDecrypter extends AbstractJweDecrypter {
	
	public RsaDecrypter() {
		//TODO: Put something here
	}
	
	@Override
	public Jwe decrypt(String encryptedJwe) {
		
		Jwe jwe = Jwe.parse(encryptedJwe);
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RSA1_5") || alg.equals("RSA-OAEP") || alg.equals("ECDH-ES") || alg.equals("A128KW") || alg.equals("A256KW")) {
			
			//decrypt to get cmk to be used for cek and cik
			jwe.setEncryptedKey(decryptEncryptionKey(jwe));
			
			//generation of cek and cik
			String algorithmLength = AlgorithmLength.getByName(jwe.getHeader().getEncryptionMethod()).getStandardName();
			int keyLength = Integer.parseInt(algorithmLength);
			byte[] contentEncryptionKey = generateContentKey(jwe.getEncryptedKey(), keyLength, new String("Encryption").getBytes());
			byte[] contentIntegrityKey = generateContentKey(jwe.getEncryptedKey(), keyLength, new String("Integrity").getBytes());
			
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
			if(signature != jwe.getSignature()){
				throw new IllegalArgumentException("Didn't decrypt correctly. Decoded Sig and generated Sig do not match");
			}
			
		} else {
			throw new IllegalArgumentException("Not a valid decrypting algorithm");
		}
		return jwe;
	}

	@Override
	public byte[] decryptCipherText(Jwe jwe, byte[] cek) {
		Cipher cipher;
		byte[] clearText = null;
		try {
			
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cek, "RSA"));
			clearText = cipher.doFinal(jwe.getCiphertext());
			
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
		
		return clearText;
		
	}

	@Override
	public byte[] decryptEncryptionKey(Jwe jwe) {
		Cipher cipher;
		byte[] contentMasterKey = null;
		PrivateKey privateKey = null;
		
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
