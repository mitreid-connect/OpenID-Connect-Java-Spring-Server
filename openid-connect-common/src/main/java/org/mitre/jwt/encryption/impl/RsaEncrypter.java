package org.mitre.jwt.encryption.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.mitre.jwe.model.Jwe;
import org.mitre.jwe.model.JweHeader;
import org.mitre.jwt.encryption.AbstractJweEncrypter;
import org.mitre.jwt.model.JwtClaims;

public class RsaEncrypter extends AbstractJweEncrypter {
	
	private Jwe jwe;
	
	private JweHeader header;
	
	private JwtClaims claims;
	
	private String signature;
	
	private byte[] encryptedKey; 
	
	private byte[] cipherText;
	
	public RsaEncrypter(Jwe jwe) {
		setJwe(jwe);
		setHeader(jwe.getHeader());
		setClaims(jwe.getClaims());
		setSignature(jwe.getSignature());
	}

	public Jwe getJwe() {
		return jwe;
	}

	public void setJwe(Jwe jwe) {
		this.jwe = jwe;
	}
	
	public byte[] getEncryptecKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(byte[] encryptedKey) {
		this.encryptedKey = encryptedKey;
	}
	
	public JweHeader getHeader() {
		return header;
	}

	public void setHeader(JweHeader header) {
		this.header = header;
	}

	public JwtClaims getClaims() {
		return claims;
	}

	public void setClaims(JwtClaims claims) {
		this.claims = claims;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public byte[] getCipherText() {
		return cipherText;
	}

	public void setCipherText(byte[] cipherText) {
		this.cipherText = cipherText;
	}

	@Override
	public byte[] encryptKey(Jwe jwe){
		String alg = jwe.getHeader().getAlgorithm();
		RSAPublicKey publicKey = null; // TODO: placeholder
		RSAPrivateKey privateKey = null;
		
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			Cipher cipher;
			try {
				cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				encryptedKey = cipher.doFinal(privateKey.getEncoded());
				
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

		} else {
			throw new IllegalArgumentException("Not a valid algorithm for encryption");
		}
		
		return encryptedKey;
		
	}
	
	@Override
	public byte[] encryptClaims(Jwe jwe) {
		String alg = jwe.getHeader().getAlgorithm();
		RSAPublicKey publicKey = null; // TODO: placeholder
		
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			Cipher cipher;
			try {
				cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				cipherText = cipher.doFinal(claims.toString().getBytes());
				
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
		} else {
			throw new IllegalArgumentException("Not a valid algorithm for encryption");
		}
		
		return cipherText;
		
	}

}
