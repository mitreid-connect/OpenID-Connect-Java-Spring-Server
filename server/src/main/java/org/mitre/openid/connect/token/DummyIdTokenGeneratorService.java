package org.mitre.openid.connect.token;

import java.util.Date;

import org.mitre.openid.connect.model.IdToken;
import org.mitre.openid.connect.model.IdTokenClaims;
import org.mitre.util.Utility;
import org.springframework.stereotype.Service;

/**
 * Dummy implementation of the IdTokenGeneratorService.
 * 
 * A concrete implementation would need access to a data service that 
 * would provide information / claims about the users in the system. This
 * information would be pulled up by the given userId and inserted into 
 * a new IdToken.
 * 
 * @author AANGANES
 *
 */
@Service
public class DummyIdTokenGeneratorService implements IdTokenGeneratorService {

	@Override
	public IdToken generateIdToken(String userId, String issuer) {
		IdToken token = new IdToken();
		
		IdTokenClaims claims = new IdTokenClaims();
		claims.setAuthTime(new Date());
		claims.setIssuer(issuer);
		claims.setUserId(userId);
		
		token.setClaims(claims);
		
		return token;
	}

}
