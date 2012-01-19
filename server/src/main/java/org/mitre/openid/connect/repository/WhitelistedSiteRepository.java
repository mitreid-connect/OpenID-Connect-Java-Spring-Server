package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.openid.connect.model.WhitelistedSite;

/**
 * WhitelistedSite repository interface
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface WhitelistedSiteRepository {

	/**
	 * Return a collection of all WhitelistedSite managed by this repository
	 * 
	 * @return the WhitelistedSite collection, or null
	 */
	public Collection<WhitelistedSite> getAll();

	/**
	 * Returns the WhitelistedSite for the given id
	 * 
	 * @param id
	 *            id the id of the WhitelistedSite
	 * @return a valid WhitelistedSite if it exists, null otherwise
	 */
	public WhitelistedSite getById(Long id);

	/**
	 * Removes the given IdToken from the repository
	 * 
	 * @param whitelistedSite
	 *            the WhitelistedSite object to remove
	 */
	public void remove(WhitelistedSite whitelistedSite);

	/**
	 * Removes an WhitelistedSite from the repository
	 * 
	 * @param id
	 *            the id of the IdToken to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a WhitelistedSite
	 * 
	 * @param whitelistedSite
	 * @return
	 */
	public WhitelistedSite save(WhitelistedSite whiteListedSite);

}
