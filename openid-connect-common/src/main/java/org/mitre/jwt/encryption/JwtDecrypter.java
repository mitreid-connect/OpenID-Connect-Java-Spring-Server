package org.mitre.jwt.encryption;

import org.mitre.jwe.model.Jwe;

public interface JwtDecrypter {
	
	public Jwe decrypt(Jwe jwe);
	
	public String decryptCipherText(Jwe jwe);
	
	public byte[] decryptEncryptionKey(Jwe jwe);
	

}
