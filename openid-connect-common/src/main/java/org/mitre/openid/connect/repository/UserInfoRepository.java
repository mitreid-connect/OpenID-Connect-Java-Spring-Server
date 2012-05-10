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
package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.openid.connect.model.DefaultUserInfo;
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
	public UserInfo save(DefaultUserInfo userInfo);
	
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
	public Collection<? extends UserInfo> getAll();	
	
}
