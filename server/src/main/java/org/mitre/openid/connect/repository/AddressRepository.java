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
