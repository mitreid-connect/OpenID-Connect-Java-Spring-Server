package org.mitre.jwe.model;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class Jwe {

	private JweHeader header;
	
	private String encryptedKey;
	
	private String ciphertext;
	
	private String integrityValue;
	
	public Jwe() {
		this.header = new JweHeader();
		this.encryptedKey = null;
		this.ciphertext = null;
		this.integrityValue = null;
	}
	
	public Jwe(JweHeader header, String encryptedKey, String ciphertext, String integrityValue) {
		this.header = header;
		this.encryptedKey = encryptedKey;
		this.ciphertext = ciphertext;
		this.integrityValue = integrityValue;
	}
	
	public JweHeader getHeader() {
		return header;
	}

	public void setHeader(JweHeader header) {
		this.header = header;
	}

	public String getEncryptedKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(String encryptedKey) {
		this.encryptedKey = encryptedKey;
	}

	public String getCiphertext() {
		return ciphertext;
	}

	public void setCiphertext(String ciphertext) {
		this.ciphertext = ciphertext;
	}

	public String getIntegrityValue() {
		return integrityValue;
	}

	public void setIntegrityValue(String integrityValue) {
		this.integrityValue = integrityValue;
	}
	
	public static Jwe parse(String s) {
		
		// null string is a null token
		if (s == null) {
			return null;
		}
		
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(s));
		
		if (parts.size() != 3) {
			throw new IllegalArgumentException("Invalid JWE format.");
		}
		
		String h64 = parts.get(0);
		String e64 = parts.get(1);
		String c64 = parts.get(2);
		String i64 = parts.get(3);

		Jwe jwe = new Jwe(new JweHeader(h64), e64, c64, i64);
		
		return jwe;
		
	}

}
