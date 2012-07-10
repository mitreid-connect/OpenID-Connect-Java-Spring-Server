package org.mitre.jwt.encryption;

import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AbstractJweDecrypter implements JwtDecrypter {
	
	protected PrivateKey privateKey;
	
	private PublicKey publicKey;
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
}
