package org.mitre.openid.connect.token;

import org.mitre.openid.connect.model.IdToken;

public interface IdTokenGeneratorService {

	public IdToken generateIdToken(String userId, String issuer);
	
}
