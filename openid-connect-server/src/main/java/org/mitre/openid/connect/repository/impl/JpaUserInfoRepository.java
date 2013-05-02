/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.mitre.util.jpa.JpaUtil.getSingleResult;
import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

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
	public UserInfo getBySubject(String sub) {
		TypedQuery<DefaultUserInfo> query = manager.createNamedQuery("DefaultUserInfo.getBySubject", DefaultUserInfo.class);
		query.setParameter("sub", sub);

		return getSingleResult(query.getResultList());
	}

	@Override
	@Transactional
	public UserInfo save(UserInfo userInfo) {
		DefaultUserInfo dui = (DefaultUserInfo)userInfo;
		return saveOrUpdate(dui.getId(), manager, dui);
	}

	@Override
	@Transactional
	public void remove(UserInfo userInfo) {
		DefaultUserInfo dui = (DefaultUserInfo)userInfo;
		UserInfo found = manager.find(DefaultUserInfo.class, dui.getId());

		if (found != null) {
			manager.remove(userInfo);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public Collection<DefaultUserInfo> getAll() {

		TypedQuery<DefaultUserInfo> query = manager.createNamedQuery("DefaultUserInfo.getAll", DefaultUserInfo.class);

		return query.getResultList();
	}

	/**
	 * Get a single UserInfo object by its username
	 */
	@Override
	public UserInfo getByUsername(String username) {
		TypedQuery<DefaultUserInfo> query = manager.createNamedQuery("DefaultUserInfo.getByUsername", DefaultUserInfo.class);
		query.setParameter("username", username);

		return getSingleResult(query.getResultList());

	}

}
