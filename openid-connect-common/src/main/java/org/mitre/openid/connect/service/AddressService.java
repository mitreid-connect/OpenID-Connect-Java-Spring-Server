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
