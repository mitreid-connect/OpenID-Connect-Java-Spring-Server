package org.mitre.jwt.encryption;

import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.mitre.jwt.encryption.impl.HmacJwtEncrypter;
import org.mitre.jwt.encryption.impl.RsaJwtClaimsEncrypter;
import org.mitre.jwt.encryption.impl.RsaJwtEncrypter;
import org.mitre.jwt.model.EncryptedJwt;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;

public class JwtEncrypter {
	
	private Jwt jwt;
	
	private JwtHeader header;
	
	private JwtClaims claims;
	
	private String signature;
	
	private Key encryptedKey; 
	
	private byte[] cipherText;
	
	public JwtEncrypter(Jwt jwt) {
		setJwt(jwt);
		header = jwt.getHeader();
		claims = jwt.getClaims();
		signature = jwt.getSignature();
	}

	public Jwt getJwt() {
		return jwt;
	}

	public void setJwt(Jwt jwt) {
		this.jwt = jwt;
	}
	
	public Key getEncryptecKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(Key encryptedKey) {
		this.encryptedKey = encryptedKey;
	}
	
	public JwtHeader getHeader() {
		return header;
	}

	public void setHeader(JwtHeader header) {
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
	
	public Key getEncryptedKey(Jwt jwt){
		String alg = jwt.getHeader().getAlgorithm();
		RSAPublicKey pubKey = null;
		RSAPrivateKey privateKey = null;
		String passphrase = null;
		
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			RsaJwtEncrypter rsaEncrypter = new RsaJwtEncrypter(pubKey, privateKey);
			encryptedKey = rsaEncrypter.createEncryptedKey();
		} else if (alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){
			HmacJwtEncrypter hmacEncrypter = new HmacJwtEncrypter(alg, passphrase.getBytes());
			encryptedKey = hmacEncrypter.createEncryptedKey();
		} else {
			throw new IllegalArgumentException("Not a valid algorithm");
		}
		
		return encryptedKey;
		
	}
	
	public byte[] getCipherText(Jwt jwt) {
		String alg = jwt.getHeader().getAlgorithm();
		RSAPublicKey pubKey = null;
		
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			RsaJwtClaimsEncrypter claimsEncrypter = new RsaJwtClaimsEncrypter(jwt.getClaims(), pubKey);
			cipherText = claimsEncrypter.createCipherText();
		} else if (alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){

		} else {
			throw new IllegalArgumentException("Not a valid algorithm");
		}
		
		return cipherText;
		
	}

	public EncryptedJwt encryptJwt(Jwt jwt) {
		
		//EncryptedJwt jwe = new EncryptedJwt(header, encryptedKey, claims, signature);
		
		return null;
	}
	

}
