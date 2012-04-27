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
package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA UserInfo repository implementation
 * 
 * @author Michael Joseph Walsh
 *
 */
@Repository
public class JpaUserInfoRepository implements UserInfoRepository {

	@PersistenceContext
    private EntityManager manager;
	
	@Override
    @Transactional	
	public UserInfo getByUserId(String userId) {
		return manager.find(UserInfo.class, userId);
	}	
	
	@Override
	@Transactional	
	public UserInfo save(UserInfo userInfo) {
		return saveOrUpdate(userInfo.getUserId(), manager, userInfo);
	}

	@Override
	@Transactional	
	public void remove(UserInfo userInfo) {
		
		UserInfo found = manager.find(UserInfo.class, userInfo.getUserId());
		
		if (found != null) {
			manager.remove(userInfo);
		} else {
			throw new IllegalArgumentException();
		}		
	}

	@Override
	@Transactional		
	public void removeByUserId(String userId) {
		UserInfo found = manager.find(UserInfo.class, userId);
		
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException();
		}			
	}
	
	@Override
	@Transactional	
	public Collection<UserInfo> getAll() {
		
		TypedQuery<UserInfo> query = manager.createNamedQuery(
				"UserInfo.getAll", UserInfo.class);
		
		return query.getResultList();
	}

}
