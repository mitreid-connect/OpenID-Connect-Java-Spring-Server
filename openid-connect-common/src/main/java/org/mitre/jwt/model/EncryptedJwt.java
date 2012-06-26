package org.mitre.jwt.model;

import java.util.List;

import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class EncryptedJwt {
	
	JwtHeader header;
	
	String encryptedKey;
	
	JwtClaims claims;
	
	String signature;
	
	public EncryptedJwt() {
		this.header = new JwtHeader();
		this.encryptedKey = null;
		this.claims = new JwtClaims();
		this.signature = null;
	}
	
	public EncryptedJwt (JwtHeader header, String encryptedKey, JwtClaims claims, String signature){
		setHeader(header);
		setEncryptedKey(encryptedKey);
		setClaims(claims);
		setSignature(signature);
	}
	
	public String toString(EncryptedJwt jwe) {
		return getHeader() + "." + getEncryptedKey() + "." + getClaims() + "." + getSignature();
	}

	public JwtHeader getHeader() {
		return header;
	}

	public void setHeader(JwtHeader header) {
		this.header = header;
	}

	public String getEncryptedKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(String encryptedKey) {
		this.encryptedKey = encryptedKey;
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
	
	public static EncryptedJwt parse(String s) {
		
		// null string is a null token
		if (s == null) {
			return null;
		}
		
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(s));
		
		if (parts.size() != 4) {
			throw new IllegalArgumentException("Invalid Encrypted JWT format.");
		}
		
		String h64 = parts.get(0);
		String e64 = parts.get(1);
		String c64 = parts.get(2);
		String i64 = parts.get(3);

		EncryptedJwt jwt = new EncryptedJwt(new JwtHeader(h64), e64, new JwtClaims(c64), i64);
		
		return jwt;
		
	}

}
