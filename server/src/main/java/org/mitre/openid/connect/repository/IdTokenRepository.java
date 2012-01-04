package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.IdToken;

public interface IdTokenRepository {

	public IdToken getById(Long id);
	
	public IdToken save(IdToken idToken);
	
}
