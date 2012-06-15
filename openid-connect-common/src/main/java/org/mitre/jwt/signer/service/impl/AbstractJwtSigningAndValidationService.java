package org.mitre.jwt.signer.service.impl;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;

public abstract class AbstractJwtSigningAndValidationService implements JwtSigningAndValidationService{

	/**
	 * Return the JwtSigners associated with this service
	 * 
	 * @return
	 */
	public abstract Map<String, ? extends JwtSigner> getSigners(); 

	@Override
	public boolean isJwtExpired(Jwt jwt) {
	
		Date expiration = jwt.getClaims().getExpiration();
	
		if (expiration != null) {
			return new Date().after(expiration);
		} else {
			return false;
		}
	}

	@Override
	public boolean validateIssuedJwt(Jwt jwt, String expectedIssuer) {
	
		String iss = jwt.getClaims().getIssuer();
		
		if (iss.equals(expectedIssuer)) {
			return true;
		}
		
		return false;
	}

	@Override
	public boolean validateSignature(String jwtString) throws NoSuchAlgorithmException {
	
		for (JwtSigner signer : getSigners().values()) {
			if (signer.verify(jwtString)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean validateIssuedAt(Jwt jwt) {
		Date issuedAt = jwt.getClaims().getIssuedAt();
		
		if (issuedAt != null) {
			return new Date().before(issuedAt);
		} else {
			return false;
		}
	}

	@Override
	public boolean validateAudience(Jwt jwt, String expectedAudience) {
		
		if(jwt.getClaims().getAudience().equals(expectedAudience)){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean validateNonce(Jwt jwt, String nonce) {
		if(jwt.getClaims().getNonce().equals(nonce)){
			return true;
		} else {
			return false;
		}
	}

}