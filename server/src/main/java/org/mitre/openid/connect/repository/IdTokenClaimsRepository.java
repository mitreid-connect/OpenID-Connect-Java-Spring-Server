package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.IdTokenClaims;

public interface IdTokenClaimsRepository {

	/**
	 * Returns the IdTokenClaims for the given id
	 * 
	 * @param id
	 *            id the id of the Address
	 * @return a valid IdTokenClaims if it exists, null otherwise
	 */
	public IdTokenClaims getById(Long id);

	/**
	 * Removes the given IdTokenClaims from the repository
	 * 
	 * @param address
	 *            the IdTokenClaims object to remove
	 */
	public void remove(IdTokenClaims idTokenClaims);

	/**
	 * Removes an IdTokenClaims from the repository
	 * 
	 * @param id
	 *            the id of the IdTokenClaims to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a IdTokenClaims
	 * 
	 * @param idTokenClaims
	 *            the IdTokenClaims to be saved
	 * @return
	 */
	public IdTokenClaims save(IdTokenClaims idTokenClaims);
}
