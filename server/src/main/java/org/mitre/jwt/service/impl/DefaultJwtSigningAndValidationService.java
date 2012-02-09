package org.mitre.jwt.service.impl;

import java.security.PublicKey;
import java.util.List;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.service.JwtSigningAndValidationService;
import org.springframework.stereotype.Service;

/**
 * THIS IS A STUB
 * 
 * TODO: Implement
 * 
 * @author AANGANES
 *
 */
@Service
public class DefaultJwtSigningAndValidationService implements
		JwtSigningAndValidationService {

	@Override
	public List<PublicKey> getAllPublicKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateSignature(String jwtString) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validateIssuedJwt(Jwt jwt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isJwtExpired(Jwt jwt) {
		// TODO Auto-generated method stub
		return false;
	}

}
