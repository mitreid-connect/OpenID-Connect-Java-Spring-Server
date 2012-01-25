package org.mitre.openid.connect.service;

import org.mitre.openid.connect.model.IdToken;

/**
 * Interface for IdToken service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface IdTokenService {

	/**
	 * Save an IdToken
	 * 
	 * @param idToken
	 *            the IdToken to be saved
	 */
	public void save(IdToken idToken);

	/**
	 * Get IdToken for id
	 * 
	 * @param id
	 *            id for IdToken
	 * @return IdToken for id, or null
	 */
	public IdToken getById(Long id);

	/**
	 * Remove the IdToken
	 * 
	 * @param idToken
	 *            the IdToken to remove
	 */
	public void remove(IdToken idToken);

	/**
	 * Remove the IdToken
	 * 
	 * @param id
	 *            id for IdToken to remove
	 */
	public void removeById(Long id);	
}
