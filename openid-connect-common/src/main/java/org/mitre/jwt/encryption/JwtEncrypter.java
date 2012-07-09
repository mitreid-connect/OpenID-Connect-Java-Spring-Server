package org.mitre.jwt.encryption;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import org.mitre.jwe.model.Jwe;


public interface JwtEncrypter {
	
	public byte[] encryptKey(Jwe jwe);
	
	public byte[] encryptClaims(Jwe jwe, Key cik);

	public Jwe encryptAndSign(Jwe jwe) throws NoSuchAlgorithmException;

}
