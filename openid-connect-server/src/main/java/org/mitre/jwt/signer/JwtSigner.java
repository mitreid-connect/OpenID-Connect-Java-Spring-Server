package org.mitre.jwt.signer;

import org.mitre.jwt.model.Jwt;

public interface JwtSigner {

	public Jwt sign(Jwt jwt);
	
	public boolean verify(String jwtString);
	
}
