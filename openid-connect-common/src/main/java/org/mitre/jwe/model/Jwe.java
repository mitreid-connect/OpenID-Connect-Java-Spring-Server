package org.mitre.jwe.model;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwt.model.Jwt;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

public class Jwe extends Jwt {

	private JweHeader header;
	
	private byte[] encryptedKey;
	
	private byte[] ciphertext;
	
	private String signature;
	
	public Jwe() {
		this.header = new JweHeader();
		this.encryptedKey = null;
		this.ciphertext = null;
		this.signature = null;
	}
	
	public Jwe(JweHeader header, byte[] encryptedKey, byte[] ciphertext, String integrityValue) {
		this.header = header;
		this.encryptedKey = encryptedKey;
		this.ciphertext = ciphertext;
		this.signature = integrityValue;
	}
	
	public JweHeader getHeader() {
		return header;
	}

	public void setHeader(JweHeader header) {
		this.header = header;
	}

	public byte[] getEncryptedKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(byte[] encryptedKey) {
		this.encryptedKey = encryptedKey;
	}

	public byte[] getCiphertext() {
		return ciphertext;
	}

	public void setCiphertext(byte[] ciphertext) {
		this.ciphertext = ciphertext;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	@Override
	public String toString() {
		return getSignatureBase() + "." + Strings.nullToEmpty(this.signature);
	}
	
	@Override
	public String getSignatureBase() {
		JsonObject h = header.getAsJsonObject();
		byte[] c = ciphertext;
		byte[] e = encryptedKey;

		String h64 = new String(Base64.encodeBase64URLSafe(h.toString().getBytes()));
		String c64 = new String(Base64.encodeBase64URLSafe(c));
		String e64 = new String(Base64.encodeBase64URLSafe(e));
		
		return h64 + "." + e64 + "." + c64;	
	}
	
	
	public static Jwe parse(String s) {
		
		// null string is a null token
		if (s == null) {
			return null;
		}
		
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(s));
		
		if (parts.size() != 4) {
			throw new IllegalArgumentException("Invalid JWE format.");
		}
		
		String h64 = parts.get(0);
		String e64 = parts.get(1);
		String c64 = parts.get(2);
		String i64 = parts.get(3);

		Jwe jwe = new Jwe(new JweHeader(h64), e64.getBytes(), c64.getBytes(), i64);
		
		return jwe;
		
	}

}
