package org.mitre.jwt.encryption;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import org.mitre.jwe.model.Jwe;


public interface JwtEncrypter {
	
	public byte[] encryptKey(Jwe jwe, Key cmk);
	
	public byte[] encryptClaims(Jwe jwe, byte[] cik);

	public Jwe encryptAndSign(Jwe jwe) throws NoSuchAlgorithmException;
	
	public byte[] generateContentKey(byte[] cmk, int keyDataLen, byte[] type);
	
	public byte[] intToFourBytes(int i);

}
