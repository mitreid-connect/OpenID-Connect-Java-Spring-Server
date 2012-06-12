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
	
		if (expiration != null)
			return new Date().after(expiration);
		else
			return false;
	
	}

	@Override
	public boolean validateIssuedJwt(Jwt jwt, String expectedIssuer) {
	
		String iss = jwt.getClaims().getIssuer();
		
		if (iss.equals(expectedIssuer))
			return true;
		
		return false;
	}

	@Override
	public boolean validateSignature(String jwtString) throws NoSuchAlgorithmException {
	
		for (JwtSigner signer : getSigners().values()) {
			if (signer.verify(jwtString))
				return true;
		}
	
		return false;
	}

}