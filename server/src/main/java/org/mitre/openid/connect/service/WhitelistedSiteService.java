package org.mitre.openid.connect.service;

import org.mitre.openid.connect.model.WhitelistedSite;

/**
 * Interface for WhitelistedSite service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface WhitelistedSiteService {

	/**
	 * Returns the WhitelistedSite for the given id
	 * 
	 * @param id
	 *            id the id of the WhitelistedSite
	 * @return a valid WhitelistedSite if it exists, null otherwise
	 */
	public WhitelistedSite getById(Long id);

	/**
	 * Removes the given WhitelistedSite from the repository
	 * 
	 * @param address
	 *            the WhitelistedSite object to remove
	 */
	public void remove(WhitelistedSite whitelistedSite);

	/**
	 * Removes an WhitelistedSite from the repository
	 * 
	 * @param id
	 *            the id of the WhitelistedSite to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a WhitelistedSite
	 * 
	 * @param whitelistedSite
	 *            the WhitelistedSite to be saved
	 * @return
	 */
	public WhitelistedSite save(WhitelistedSite whitelistedSite);
}
