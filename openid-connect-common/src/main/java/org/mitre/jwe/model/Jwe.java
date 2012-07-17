package org.mitre.jwe.model;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwt.model.Jwt;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

/**
 * 
 * 
	 * Return the canonical encoded string of this JWE, the header in Base64, a period ".", the encrypted key in Base64, a period ".",
	 * the ciphertext in Base64, a period ".", and the signature, or integrity value, in Base64.

 * @author DERRYBERRY
 *
 */
public class Jwe extends Jwt {

	private JweHeader header;
	
	private byte[] encryptedKey;
	
	private byte[] ciphertext;
	
	public Jwe() {
		super();
		this.header = new JweHeader();
		this.encryptedKey = null;
		this.ciphertext = null;
	}
	
	public Jwe(JweHeader header, byte[] encryptedKey, byte[] ciphertext, String integrityValue) {
		super(null, null, integrityValue);
		this.header = header;
		this.encryptedKey = encryptedKey;
		this.ciphertext = ciphertext;
	}
	
	public Jwe(String headerBase64, String encryptedKeyBase64, String cipherTextBase64, String integrityValueBase64) {
		byte[] decodedEncryptedKey = Base64.decodeBase64(encryptedKeyBase64.getBytes());
		byte[] decodedCipherText = Base64.decodeBase64(cipherTextBase64.getBytes());
		this.header = new JweHeader(headerBase64);
		this.encryptedKey = decodedEncryptedKey;
		this.ciphertext = decodedCipherText;
		setSignature(integrityValueBase64);
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

	@Override
	public String getSignatureBase() {
		JsonObject h = header.getAsJsonObject();
		byte[] c = ciphertext;
		byte[] e = encryptedKey;

		String h64 = new String(Base64.encodeBase64URLSafe(h.toString().getBytes()));
		String e64 = new String(Base64.encodeBase64URLSafe(e));
		String c64 = new String(Base64.encodeBase64URLSafe(c));
		
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

		Jwe jwe = new Jwe(h64, e64, c64, i64);
		
		return jwe;
		
	}

}
