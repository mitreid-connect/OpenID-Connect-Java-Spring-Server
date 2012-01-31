package org.mitre.jwt.service;

import java.security.PublicKey;
import java.util.List;

import org.mitre.jwt.model.Jwt;

public interface JwtSigningAndValidationService {

	/**
	 * Returns all public keys this service is configured with.
	 * @return
	 */
	public List<PublicKey> getAllPublicKeys();
	
	/**
	 * Checks the signature of the given JWT against all configured signers, returns true if at least one of the signers validates it.
	 * @param jwtString the string representation of the JWT as sent on the wire
	 * @return true if the signature is valid, false if not
	 */
	public boolean validateSignature(String jwtString);
	
	/**
	 * Checks to see if this JWT has been issued by us
	 * @param jwt the JWT to check the issuer of
	 * @return true if the JWT was issued by this AS, false if not
	 */
	public boolean validateIssuedJwt(Jwt jwt);
	
	
	/**
	 * Check to see if this JWT has expired or not
	 * @param jwt the JWT to check
	 * @return true if this JWT has an expiration and it has passed, false if the JWT has no expiration or it has an expiration and the expiration has not passed
	 */
	 public boolean isJwtExpired(Jwt jwt);
}
