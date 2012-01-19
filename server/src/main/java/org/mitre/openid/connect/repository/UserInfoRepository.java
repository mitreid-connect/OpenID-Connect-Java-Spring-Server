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
	 * Return a collection of all UserInfos managed by this repository
	 * 
	 * @return the UserInfo collection, or null
	 */
	public Collection<UserInfo> getAll();	
	
}
