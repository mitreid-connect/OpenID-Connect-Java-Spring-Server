package org.mitre.openid.connect.repository;

import java.util.Collection;
import org.mitre.openid.connect.model.Nonce;

/**
 * 
 * Nonce repository interface.
 * 
 * @author Amanda Anganes
 *
 */
public interface NonceRepository {

	/**
	 * Return the nonce with the given ID
	 * 
	 * @param id the ID of the nonce to find
	 * @return the nonce, if found
	 */
	public Nonce getById(Long id);
	
	/**
	 * Remove the given Nonce from the database
	 * 
	 * @param nonce the Nonce to remove
	 */
	public void remove(Nonce nonce);
	
	/**
	 * Save a new Nonce in the database
	 * 
	 * @param nonce the Nonce to save
	 * @return the saved Nonce
	 */
	public Nonce save(Nonce nonce);
	
	/**
	 * Return all nonces stored in the database
	 * 
	 * @return the set of nonces
	 */
	public Collection<Nonce> getAll();
	
	/**
	 * Return all expired nonces stored in the database
	 * 
	 * @return the set of expired nonces
	 */
	public Collection<Nonce> getExpired();
	
	/**
	 * Return the set of nonces registered to the given client ID
	 * 
	 * @param clientId the client ID
	 * @return the set of nonces registered to the client
	 */
	public Collection<Nonce> getByClientId(String clientId);
	
}
