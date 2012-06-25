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
	public boolean validateSignature(String jwtString) {
	
		for (JwtSigner signer : getSigners().values()) {
			try {
	            if (signer.verify(jwtString)) {
	            	return true;
	            }
            } catch (NoSuchAlgorithmException e) {
            	// ignore, signer didn't verify signature, try the next one
            	e.printStackTrace();
            }
		}
		return false;
	}
	
	@Override
	public boolean validateIssuedAt(Jwt jwt) {
		Date issuedAt = jwt.getClaims().getIssuedAt();
		
		if (issuedAt != null) {
			// make sure the token was issued in the past
			return new Date().after(issuedAt);
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