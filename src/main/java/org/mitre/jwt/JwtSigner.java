package org.mitre.jwt;

public interface JwtSigner {

	public void sign(Jwt jwt);
	
	public boolean verify(String jwtString);
	
}
