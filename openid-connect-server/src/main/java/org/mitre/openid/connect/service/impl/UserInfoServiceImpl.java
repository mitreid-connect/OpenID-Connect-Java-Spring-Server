package org.mitre.openid.connect.service.impl;

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
	public void save(UserInfo userInfo) {
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
