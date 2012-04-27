/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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

import org.mitre.openid.connect.model.Address;

/**
 * Interface for Address service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface AddressService {

	/**
	 * Save an Address
	 * 
	 * @param address
	 *            the Address to be saved
	 */
	public void save(Address address);

	/**
	 * Get Address for id
	 * 
	 * @param id
	 *            id for Address
	 * @return Address for id, or null
	 */
	public Address getById(Long id);

	/**
	 * Remove the Address
	 * 
	 * @param address
	 *            the Address to remove
	 */
	public void remove(Address address);

	/**
	 * Remove the Address
	 * 
	 * @param id
	 *            id for Address to remove
	 */
	public void removeById(Long id);
}
