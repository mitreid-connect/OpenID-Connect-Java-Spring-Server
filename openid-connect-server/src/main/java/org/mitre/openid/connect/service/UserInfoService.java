package org.mitre.openid.connect.service;

import org.mitre.openid.connect.model.UserInfo;

/**
 * Interface for UserInfo service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface UserInfoService {

	/**
	 * Save an UserInfo
	 * 
	 * @param userInfo
	 *            the UserInfo to be saved
	 */
	public void save(UserInfo userInfo);

	/**
	 * Get UserInfo for user id
	 * 
	 * @param userId
	 *            user id for UserInfo
	 * @return UserInfo for user id, or null
	 */
	public UserInfo getByUserId(String userId);

	/**
	 * Remove the UserInfo
	 * 
	 * @param userInfo
	 *            the UserInfo to remove
	 */
	public void remove(UserInfo userInfo);

	/**
	 * Remove the UserInfo
	 * 
	 * @param userId
	 *            user id for UserInfo to remove
	 */
	public void removeByUserId(String userId);
}
