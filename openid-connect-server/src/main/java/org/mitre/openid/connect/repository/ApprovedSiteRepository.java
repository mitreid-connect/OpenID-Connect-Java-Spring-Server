package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.UserInfo;

/**
 * ApprovedSite repository interface
 *
 * @author Michael Joseph Walsh
 *
 */
public interface ApprovedSiteRepository {

	/**
	 * Returns the ApprovedSite for the given id
	 * 
	 * @param id
	 *            id the id of the ApprovedSite
	 * @return a valid ApprovedSite if it exists, null otherwise
	 */
	public ApprovedSite getById(Long id);

	/**
	 * Return a collection of all ApprovedSites managed by this repository
	 * 
	 * @return the ApprovedSite collection, or null
	 */
	public Collection<ApprovedSite> getAll();

	/**
	 * Return a collection of ApprovedSite managed by this repository matching the
	 * provided ClientDetailsEntity
	 * 
	 * @param userId
	 * @return
	 */
	public Collection<ApprovedSite> getByClientDetails(
			ClientDetailsEntity clientDetails);

	/**
	 * Return a collection of ApprovedSite managed by this repository matching the
	 * provided UserInfo
	 * 
	 * @param userId
	 * @return
	 */
	public Collection<ApprovedSite> getByUserInfo(UserInfo userInfo);

	/**
	 * Removes the given ApprovedSite from the repository
	 * 
	 * @param aggregator
	 *            the ApprovedSite object to remove
	 */
	public void remove(ApprovedSite approvedSite);

	/**
	 * Removes an ApprovedSite from the repository
	 * 
	 * @param id
	 *            the id of the ApprovedSite to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists an ApprovedSite
	 * 
	 * @param aggregator
	 *            valid ApprovedSite instance
	 * @return the persisted entity
	 */
	public ApprovedSite save(ApprovedSite approvedSite);
}
