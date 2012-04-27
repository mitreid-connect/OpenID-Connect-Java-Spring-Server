/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.openid.connect.token;

import java.util.Date;

import org.mitre.openid.connect.model.IdToken;
import org.mitre.openid.connect.model.IdTokenClaims;
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
