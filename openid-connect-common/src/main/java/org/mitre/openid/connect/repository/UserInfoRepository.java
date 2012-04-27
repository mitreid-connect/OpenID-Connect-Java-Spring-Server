package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.openid.connect.model.UserInfo;

/**
 * UserInfo repository interface
 * 
 * @author Michael Joseph Walsh
 *
 */
public interface UserInfoRepository {
	
	/**
	 * Returns the UserInfo for the given user id
	 * 
	 * @param userId
	 *            userId the user id of the UserInfo
	 * @return a valid UserInfo if it exists, null otherwise
	 */	
	public UserInfo getByUserId(String userId);		
	
	/**
	 * Persists a UserInfo 
	 *
	 * @param user
	 * @return
	 */
	public UserInfo save(UserInfo userInfo);
	
	/**
	 * Removes the given UserInfo from the repository
	 * 
	 * @param userInfo
	 *            the UserInfo object to remove
	 */
	public void remove(UserInfo userInfo);

	/**
	 * Removes the UserInfo from the repository for the given user id
	 * 
	 * @param userId
	 *            the user id for the UserInfo object to remove
	 */
	public void removeByUserId(String userId);	
	
	/**
	 * Return a collection of all UserInfos managed by this repository
	 * 
	 * @return the UserInfo collection, or null
	 */
	public Collection<UserInfo> getAll();	
	
}
