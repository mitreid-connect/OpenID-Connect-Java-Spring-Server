package org.mitre.jwt.encryption;

import java.security.Key;

import org.mitre.jwe.model.Jwe;

public interface JwtDecrypter {
	
	public Jwe decrypt(String encryptedJwe);
	
	public String decryptCipherText(Jwe jwe, Key cek);
	
	public byte[] decryptEncryptionKey(Jwe jwe);
	

}
