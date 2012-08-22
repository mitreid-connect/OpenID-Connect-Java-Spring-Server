/**
 * 
 */
package org.mitre.oauth2.service.impl;

import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.repository.AuthorizationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;
import org.springframework.stereotype.Service;

/**
 * @author aanganes
 *
 */
@Service
public class DefaultOAuth2AuthorizationCodeService implements AuthorizationCodeServices {

	@Autowired
	private AuthorizationCodeRepository repository;
	
	private RandomValueStringGenerator generator = new RandomValueStringGenerator();
	
	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.code.AuthorizationCodeServices#createAuthorizationCode(org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder)
	 */
	@Override
	public String createAuthorizationCode(AuthorizationRequestHolder authentication) {
		String code = generator.generate();
		
		AuthorizationCodeEntity entity = new AuthorizationCodeEntity(code);
		
		repository.save(entity);
		
		return code;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.code.AuthorizationCodeServices#consumeAuthorizationCode(java.lang.String)
	 */
	@Override
	public AuthorizationRequestHolder consumeAuthorizationCode(String code) throws InvalidGrantException {
		
		AuthorizationRequestHolder auth = repository.consume(code);
		return auth;
	}

	public AuthorizationCodeRepository getRepository() {
		return repository;
	}

	public void setRepository(AuthorizationCodeRepository repository) {
		this.repository = repository;
	}

}
