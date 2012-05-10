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
package org.mitre.openid.connect.service.impl;

import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.mitre.openid.connect.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the UserInfoService
 * 
 * @author Michael Joseph Walsh
 * 
 */
@Service
@Transactional
public class UserInfoServiceImpl implements UserInfoService {

	@Autowired
	private UserInfoRepository userInfoRepository;

	/**
	 * Default constructor
	 */
	public UserInfoServiceImpl() {

	}

	/**
	 * Constructor for use in test harnesses.
	 * 
	 * @param repository
	 */
	public UserInfoServiceImpl(UserInfoRepository userInfoRepository) {
		this.userInfoRepository = userInfoRepository;
	}

	@Override
	public void save(DefaultUserInfo userInfo) {
		userInfoRepository.save(userInfo);
	}

	@Override
	public UserInfo getByUserId(String userId) {
		return userInfoRepository.getByUserId(userId);
	}

	@Override
	public void remove(UserInfo userInfo) {
		userInfoRepository.remove(userInfo);
	}

	@Override
	public void removeByUserId(String userId) {
		userInfoRepository.removeByUserId(userId);
	}

	/**
     * @return the userInfoRepository
     */
    public UserInfoRepository getUserInfoRepository() {
    	return userInfoRepository;
    }

	/**
     * @param userInfoRepository the userInfoRepository to set
     */
    public void setUserInfoRepository(UserInfoRepository userInfoRepository) {
    	this.userInfoRepository = userInfoRepository;
    }

}
