package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RsaJwtClaimsDecrypter {
	
	private byte[] cipherText;
	private PrivateKey privateKey;
	
	public RsaJwtClaimsDecrypter(byte[] cipherText, PrivateKey privateKey) {
		setCipherText(cipherText);
		setPrivateKey(privateKey);
	}

	public byte[] getCipherText() {
		return cipherText;
	}

	public void setCipherText(byte[] cipherText) {
		this.cipherText = cipherText;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
	public String decryptCipherText() {
		Cipher cipher;
		String clearTextString = null;
		try {
			
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] clearText = cipher.doFinal(cipherText);
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

}
