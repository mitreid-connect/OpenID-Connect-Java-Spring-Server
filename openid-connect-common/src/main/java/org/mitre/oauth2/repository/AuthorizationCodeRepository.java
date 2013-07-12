package org.mitre.oauth2.repository;

import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * Interface for saving and consuming OAuth2 authorization codes as AuthorizationCodeEntitys.
 * 
 * @author aanganes
 *
 */
public interface AuthorizationCodeRepository {

	/**
	 * Save an AuthorizationCodeEntity to the repository
	 * 
	 * @param authorizationCode the AuthorizationCodeEntity to save
	 * @return					the saved AuthorizationCodeEntity
	 */
	public AuthorizationCodeEntity save(AuthorizationCodeEntity authorizationCode);

	/**
	 * Consume an authorization code.
	 * 
	 * @param code						the authorization code value
	 * @return							the authentication associated with the code
	 * @throws InvalidGrantException	if no AuthorizationCodeEntity is found with the given value
	 */
	public OAuth2Authentication consume(String code) throws InvalidGrantException;

}
