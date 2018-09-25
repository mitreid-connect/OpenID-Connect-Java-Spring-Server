/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
 *******************************************************************************/
package org.mitre.oauth2.repository.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.data.DefaultPageCriteria;
import org.mitre.data.PageCriteria;
import org.mitre.host.service.HostInfoService;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(value="defaultTransactionManager")
public class JpaAuthenticationHolderRepository implements AuthenticationHolderRepository {

	private static final int MAXEXPIREDRESULTS = 1000;

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;
	
	@Autowired
	HostInfoService hostInfoService;

	@Override
	public List<AuthenticationHolderEntity> getAll() {
		TypedQuery<AuthenticationHolderEntity> query = manager.createNamedQuery(AuthenticationHolderEntity.QUERY_ALL, AuthenticationHolderEntity.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		return query.getResultList();
	}

	@Override
	public AuthenticationHolderEntity getById(String uuid) {
		AuthenticationHolderEntity entity = manager.find(AuthenticationHolderEntity.class, uuid);
		if (entity == null) {
			throw new IllegalArgumentException("AuthenticationHolderEntity not found: " + uuid);
		}
		hostInfoService.validateHost(entity.getHostUuid());
		return entity;
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(AuthenticationHolderEntity a) {
		AuthenticationHolderEntity found = getById(a.getId());
		manager.remove(found);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public AuthenticationHolderEntity save(AuthenticationHolderEntity a) {
		a.setHostUuid(hostInfoService.getCurrentHostUuid());
		return JpaUtil.saveOrUpdate(a.getId(), manager, a);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public List<AuthenticationHolderEntity> getOrphanedAuthenticationHolders() {
		DefaultPageCriteria pageCriteria = new DefaultPageCriteria(0,MAXEXPIREDRESULTS);
		return getOrphanedAuthenticationHolders(pageCriteria);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public List<AuthenticationHolderEntity> getOrphanedAuthenticationHolders(PageCriteria pageCriteria) {
		TypedQuery<AuthenticationHolderEntity> query = manager.createNamedQuery(AuthenticationHolderEntity.QUERY_GET_UNUSED, AuthenticationHolderEntity.class);
		return JpaUtil.getResultPage(query, pageCriteria);
	}

}
