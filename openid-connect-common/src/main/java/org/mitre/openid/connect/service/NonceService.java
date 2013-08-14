/*******************************************************************************
 * Copyright 2013 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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
package org.mitre.openid.connect.service;

import java.util.Collection;

import org.mitre.openid.connect.model.Nonce;


/**
 * 
 * Nonce service interface
 * 
 * @author Amanda Anganes
 *
 */
public interface NonceService {

	/**
	 * Create a new nonce.
	 * 
	 * @param clientId the ID of the client
	 * @param value the value of the Nonce
	 * @return the saved Nonce
	 */
	public Nonce create(String clientId, String value);

	/**
	 * Check whether a given nonce value has been previously used and stored
	 * by the client.
	 * 
	 * @param clientId the ID of the client
	 * @param value the value of the nonce
	 * @return true if the nonce has already been used, false otherwise
	 */
	public boolean alreadyUsed(String clientId, String value);

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

	/**
	 * Clear expired nonces from the database
	 */
	void clearExpiredNonces();

}
