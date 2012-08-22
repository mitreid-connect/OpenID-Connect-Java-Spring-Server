package org.mitre.oauth2.repository;

import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;

/**
 * @author amanda
 *
 */
public interface AuthorizationCodeRepository {

	public AuthorizationCodeEntity save(AuthorizationCodeEntity authorizationCode);
	
	public AuthorizationRequestHolder consume(String code) throws InvalidGrantException;
}
