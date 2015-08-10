/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/
package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA ApprovedSite repository implementation
 * 
 * @author Michael Joseph Walsh, aanganes
 *
 */
@Repository
@Transactional(value="defaultTransactionManagerIdentifier")
public class JpaApprovedSiteRepository extends DefaultEntityManager implements ApprovedSiteRepository {

	@Override
	public Collection<ApprovedSite> getAll() {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_ALL, ApprovedSite.class);
		return query.getResultList();
	}

	@Override
	public ApprovedSite getById(Long id) {
		return manager.find(ApprovedSite.class, id);
	}

	@Override
	public void remove(ApprovedSite approvedSite) {
		ApprovedSite found = manager.find(ApprovedSite.class, approvedSite.getId());

		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public ApprovedSite save(ApprovedSite approvedSite) {
		return saveOrUpdate(approvedSite.getId(), manager, approvedSite);
	}

	@Override
	public Collection<ApprovedSite> getByClientIdAndUserId(String clientId, String userId) {

		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_BY_CLIENT_ID_AND_USER_ID, ApprovedSite.class);
		query.setParameter(ApprovedSite.PARAM_USER_ID, userId);
		query.setParameter(ApprovedSite.PARAM_CLIENT_ID, clientId);

		return query.getResultList();
	}

	@Override
	public Collection<ApprovedSite> getByUserId(String userId) {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_BY_USER_ID, ApprovedSite.class);
		query.setParameter(ApprovedSite.PARAM_USER_ID, userId);

		return query.getResultList();

	}

	@Override
	public Collection<ApprovedSite> getByClientId(String clientId) {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(ApprovedSite.QUERY_BY_CLIENT_ID, ApprovedSite.class);
		query.setParameter(ApprovedSite.PARAM_CLIENT_ID, clientId);

		return query.getResultList();
	}
}
