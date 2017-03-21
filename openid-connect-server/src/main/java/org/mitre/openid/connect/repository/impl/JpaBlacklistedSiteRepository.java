/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
/**
 *
 */
package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.BlacklistedSite;
import org.mitre.openid.connect.repository.BlacklistedSiteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository
public class JpaBlacklistedSiteRepository implements BlacklistedSiteRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.BlacklistedSiteRepository#getAll()
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<BlacklistedSite> getAll() {
		TypedQuery<BlacklistedSite> query = manager.createNamedQuery(BlacklistedSite.QUERY_ALL, BlacklistedSite.class);
		return query.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.BlacklistedSiteRepository#getById(java.lang.Long)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public BlacklistedSite getById(Long id) {
		return manager.find(BlacklistedSite.class, id);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.BlacklistedSiteRepository#remove(org.mitre.openid.connect.model.BlacklistedSite)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(BlacklistedSite blacklistedSite) {
		BlacklistedSite found = manager.find(BlacklistedSite.class, blacklistedSite.getId());

		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException();
		}

	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.BlacklistedSiteRepository#save(org.mitre.openid.connect.model.BlacklistedSite)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public BlacklistedSite save(BlacklistedSite blacklistedSite) {
		return saveOrUpdate(blacklistedSite.getId(), manager, blacklistedSite);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.BlacklistedSiteRepository#update(org.mitre.openid.connect.model.BlacklistedSite, org.mitre.openid.connect.model.BlacklistedSite)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public BlacklistedSite update(BlacklistedSite oldBlacklistedSite, BlacklistedSite blacklistedSite) {

		blacklistedSite.setId(oldBlacklistedSite.getId());
		return saveOrUpdate(oldBlacklistedSite.getId(), manager, blacklistedSite);

	}

}
