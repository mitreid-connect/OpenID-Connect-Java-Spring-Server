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
