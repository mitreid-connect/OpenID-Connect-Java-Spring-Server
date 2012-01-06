package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.IdTokenClaims;

public interface IdTokenRepository {

	public IdTokenClaims getById(Long id);
	
	public IdTokenClaims save(IdTokenClaims idToken);
	
}
