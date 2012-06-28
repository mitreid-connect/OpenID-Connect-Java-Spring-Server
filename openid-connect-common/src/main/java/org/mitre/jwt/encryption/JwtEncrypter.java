package org.mitre.jwt.encryption;

import org.mitre.jwe.model.Jwe;


public interface JwtEncrypter {
	
	public byte[] encryptKey(Jwe jwe);
	
	public byte[] encryptClaims(Jwe jwe);

	public Jwe encryptAndSign(Jwe jwe);

}
