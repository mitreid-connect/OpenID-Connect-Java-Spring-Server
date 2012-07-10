package org.mitre.jwt.encryption;

import org.mitre.jwe.model.Jwe;

public interface JwtDecrypter {
	
	public Jwe decrypt(String encryptedJwe);
	
	public byte[] decryptCipherText(Jwe jwe, byte[] cek);
	
	public byte[] decryptEncryptionKey(Jwe jwe);
	

}
