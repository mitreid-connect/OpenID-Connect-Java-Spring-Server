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
package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.Address;

/**
 * Address repository interface
 *
 * @author Michael Joseph Walsh
 *
 */
public interface AddressRepository {

	/**
	 * Returns the Address for the given id
	 * 
	 * @param id
	 *            id the id of the Address
	 * @return a valid Address if it exists, null otherwise
	 */
	public Address getById(Long id);

	/**
	 * Removes the given Address from the repository
	 * 
	 * @param address
	 *            the Address object to remove
	 */
	public void remove(Address address);

	/**
	 * Removes an Address from the repository
	 * 
	 * @param id
	 *            the id of the Address to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a Address
	 * 
	 * @param address
	 *            the Address to be saved
	 * @return
	 */
	public Address save(Address address);
}
