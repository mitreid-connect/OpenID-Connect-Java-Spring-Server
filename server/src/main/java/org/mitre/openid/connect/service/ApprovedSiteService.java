package org.mitre.openid.connect.service;

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.UserInfo;

/**
 * Interface for ApprovedSite service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface ApprovedSiteService {
	
	
	/**
	 * Return a collection of all ApprovedSites
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
	public Collection<ApprovedSite> getByClientDetails(ClientDetailsEntity clientDetails);
	
	/**
	 * Return a collection of ApprovedSite managed by this repository matching the
	 * provided UserInfo
	 * 
	 * @param userId
	 * @return
	 */
	public Collection<ApprovedSite> getByUserInfo(UserInfo userInfo);	
	
	/**
	 * Save an ApprovedSite
	 * 
	 * @param approvedSite
	 *            the ApprovedSite to be saved
	 */
	public void save(ApprovedSite approvedSite);

	/**
	 * Get ApprovedSite for id
	 * 
	 * @param id
	 *            id for ApprovedSite
	 * @return ApprovedSite for id, or null
	 */
	public ApprovedSite getById(Long id);

	/**
	 * Remove the ApprovedSite
	 * 
	 * @param approvedSite
	 *            the ApprovedSite to remove
	 */
	public void remove(ApprovedSite approvedSite);

	/**
	 * Remove the ApprovedSite
	 * 
	 * @param id
	 *            id for ApprovedSite to remove
	 */
	public void removeById(Long id);

}
