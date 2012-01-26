package org.mitre.openid.connect.service;

import org.mitre.openid.connect.model.IdTokenClaims;

/**
 * Interface for IdTokenClaims service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface IdTokenClaimsService {

	/**
	 * Save an IdTokenClaims
	 * 
	 * @param address
	 *            the Address to be saved
	 */
	public void save(IdTokenClaims idTokenClaims);

	/**
	 * Get IdTokenClaims for id
	 * 
	 * @param id
	 *            id for IdTokenClaims
	 * @return IdTokenClaims for id, or null
	 */
	public IdTokenClaims getById(Long id);

	/**
	 * Remove the IdTokenClaims
	 * 
	 * @param idTokenClaims
	 *            the IdTokenClaims to remove
	 */
	public void remove(IdTokenClaims idTokenClaims);

	/**
	 * Remove the IdTokenClaims
	 * 
	 * @param id
	 *            id for IdTokenClaims to remove
	 */
	public void removeById(Long id);	
}
