package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.IdToken;

/**
 * IdToken repository interface
 * 
 * @author Michael Joseph Walsh
 *
 */
public interface IdTokenRepository {

	/**
	 * Returns the IdToken for the given id
	 * 
	 * @param id
	 *            id the id of the IdToken
	 * @return a valid IdToken if it exists, null otherwise
	 */	
	public IdToken getById(Long id);

	/**
	 * Removes the given IdToken from the repository
	 * 
	 * @param idToken
	 *            the IdToken object to remove
	 */
	public void remove(IdToken idToken);

	/**
	 * Removes an IdToken from the repository
	 * 
	 * @param id
	 *            the id of the IdToken to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a IdToken
	 * 
	 * @param idToken
	 * @return
	 */
	public IdToken save(IdToken idToken);
}
