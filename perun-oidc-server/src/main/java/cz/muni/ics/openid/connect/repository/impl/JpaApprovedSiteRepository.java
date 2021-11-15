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
package cz.muni.ics.openid.connect.repository.impl;

import cz.muni.ics.openid.connect.model.ApprovedSite;
import cz.muni.ics.openid.connect.repository.ApprovedSiteRepository;
import cz.muni.ics.util.jpa.JpaUtil;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA ApprovedSite repository implementation
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
@Repository
public class JpaApprovedSiteRepository implements ApprovedSiteRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;

	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<ApprovedSite> getAll() {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_ALL, ApprovedSite.class);
		return query.getResultList();
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public ApprovedSite getById(Long id) {
		return manager.find(ApprovedSite.class, id);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(ApprovedSite approvedSite) {
		ApprovedSite found = manager.find(ApprovedSite.class, approvedSite.getId());

		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public ApprovedSite save(ApprovedSite approvedSite) {
		return JpaUtil.saveOrUpdate(manager, approvedSite);
	}

	@Override
	public Collection<ApprovedSite> getByClientIdAndUserId(String clientId, String userId) {

		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_BY_CLIENT_ID_AND_USER_ID, ApprovedSite.class);
		query.setParameter(ApprovedSite.PARAM_USER_ID, userId);
		query.setParameter(ApprovedSite.PARAM_CLIENT_ID, clientId);

		return query.getResultList();
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<ApprovedSite> getByUserId(String userId) {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_BY_USER_ID, ApprovedSite.class);
		query.setParameter(ApprovedSite.PARAM_USER_ID, userId);

		return query.getResultList();

	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<ApprovedSite> getByClientId(String clientId) {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_BY_CLIENT_ID, ApprovedSite.class);
		query.setParameter(ApprovedSite.PARAM_CLIENT_ID, clientId);

		return query.getResultList();
	}
}
