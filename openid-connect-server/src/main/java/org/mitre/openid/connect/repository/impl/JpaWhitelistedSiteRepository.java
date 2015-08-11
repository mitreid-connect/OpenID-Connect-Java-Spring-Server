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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA WhitelistedSite repository implementation
 * 
 * @author Michael Joseph Walsh, aanganes
 *
 */
@Repository
@Transactional(value="defaultTransactionManagerIdentifier")
public class JpaWhitelistedSiteRepository implements WhitelistedSiteRepository {
	
	@PersistenceContext(unitName="defaultPersistenceUnit")
	public EntityManager manager;

	@Override
	public Collection<WhitelistedSite> getAll() {
		TypedQuery<WhitelistedSite> query = manager.createNamedQuery(WhitelistedSite.QUERY_ALL, WhitelistedSite.class);
		return query.getResultList();
	}

	@Override
	public WhitelistedSite getById(Long id) {
		return manager.find(WhitelistedSite.class, id);
	}

	@Override
	public void remove(WhitelistedSite whitelistedSite) {
		WhitelistedSite found = manager.find(WhitelistedSite.class, whitelistedSite.getId());

		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public WhitelistedSite save(WhitelistedSite whiteListedSite) {
		return saveOrUpdate(whiteListedSite.getId(), manager, whiteListedSite);
	}

	@Override
	public WhitelistedSite update(WhitelistedSite oldWhitelistedSite, WhitelistedSite whitelistedSite) {
		// sanity check
		whitelistedSite.setId(oldWhitelistedSite.getId());

		return saveOrUpdate(oldWhitelistedSite.getId(), manager, whitelistedSite);
	}

	@Override
	public WhitelistedSite getByClientId(String clientId) {
		TypedQuery<WhitelistedSite> query = manager.createNamedQuery(WhitelistedSite.QUERY_BY_CLIENT_ID, WhitelistedSite.class);
		query.setParameter(WhitelistedSite.PARAM_CLIENT_ID, clientId);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	public Collection<WhitelistedSite> getByCreator(String creatorId) {
		TypedQuery<WhitelistedSite> query = manager.createNamedQuery(WhitelistedSite.QUERY_BY_CREATOR, WhitelistedSite.class);
		query.setParameter(WhitelistedSite.PARAM_USER_ID, creatorId);

		return query.getResultList();
	}
}
