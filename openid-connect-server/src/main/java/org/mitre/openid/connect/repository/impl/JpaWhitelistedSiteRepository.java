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
package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.host.service.HostInfoService;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA WhitelistedSite repository implementation
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
@Repository
public class JpaWhitelistedSiteRepository implements WhitelistedSiteRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;

	@Autowired
	HostInfoService hostInfoService;
	
	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<WhitelistedSite> getAll() {
		TypedQuery<WhitelistedSite> query = manager.createNamedQuery(WhitelistedSite.QUERY_ALL, WhitelistedSite.class);
		query.setParameter(WhitelistedSite.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		return query.getResultList();
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public WhitelistedSite getById(String id) {
		WhitelistedSite entity = manager.find(WhitelistedSite.class, id);
		if (entity == null) {
			throw new IllegalArgumentException("WhitelistedSite not found: " + id);
		}
		hostInfoService.validateHost(entity.getHostUuid());
		return entity;
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(WhitelistedSite whitelistedSite) {
		WhitelistedSite found = getById(whitelistedSite.getUuid());
		manager.remove(found);	
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public WhitelistedSite save(WhitelistedSite whiteListedSite) {
		whiteListedSite.setHostUuid(hostInfoService.getCurrentHostUuid());

		return saveOrUpdate(whiteListedSite.getUuid(), manager, whiteListedSite);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public WhitelistedSite update(WhitelistedSite oldWhitelistedSite, WhitelistedSite whitelistedSite) {
		// sanity check
		whitelistedSite.setUuid(oldWhitelistedSite.getUuid());

		hostInfoService.validateHost(oldWhitelistedSite.getHostUuid());
		
		whitelistedSite.setHostUuid(hostInfoService.getCurrentHostUuid());
		
		return saveOrUpdate(oldWhitelistedSite.getUuid(), manager, whitelistedSite);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public WhitelistedSite getByClientId(String clientId) {
		TypedQuery<WhitelistedSite> query = manager.createNamedQuery(WhitelistedSite.QUERY_BY_CLIENT_ID, WhitelistedSite.class);
		query.setParameter(DefaultUserInfo.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(WhitelistedSite.PARAM_CLIENT_ID, clientId);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<WhitelistedSite> getByCreator(String creatorId) {
		TypedQuery<WhitelistedSite> query = manager.createNamedQuery(WhitelistedSite.QUERY_BY_CREATOR, WhitelistedSite.class);
		query.setParameter(DefaultUserInfo.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(WhitelistedSite.PARAM_USER_ID, creatorId);

		return query.getResultList();
	}
}
