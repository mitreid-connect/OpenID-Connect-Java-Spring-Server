/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

package org.mitre.uma.repository.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.repository.ResourceSetRepository;
import org.mitre.util.jpa.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository
public class JpaResourceSetRepository implements ResourceSetRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager em;
	private static Logger logger = LoggerFactory.getLogger(JpaResourceSetRepository.class);

	@Override
	@Transactional(value="defaultTransactionManager")
	public ResourceSet save(ResourceSet rs) {
		return JpaUtil.saveOrUpdate(rs.getId(), em, rs);
	}

	@Override
	public ResourceSet getById(Long id) {
		return em.find(ResourceSet.class, id);
	}

	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(ResourceSet rs) {
		ResourceSet found = getById(rs.getId());
		if (found != null) {
			em.remove(found);
		} else {
			logger.info("Tried to remove unknown resource set: " + rs.getId());
		}
	}

	@Override
	public Collection<ResourceSet> getAllForOwner(String owner) {
		TypedQuery<ResourceSet> query = em.createNamedQuery(ResourceSet.QUERY_BY_OWNER, ResourceSet.class);
		query.setParameter(ResourceSet.PARAM_OWNER, owner);
		return query.getResultList();
	}

	@Override
	public Collection<ResourceSet> getAllForOwnerAndClient(String owner, String clientId) {
		TypedQuery<ResourceSet> query = em.createNamedQuery(ResourceSet.QUERY_BY_OWNER_AND_CLIENT, ResourceSet.class);
		query.setParameter(ResourceSet.PARAM_OWNER, owner);
		query.setParameter(ResourceSet.PARAM_CLIENTID, clientId);
		return query.getResultList();
	}

	@Override
	public Collection<ResourceSet> getAll() {
		TypedQuery<ResourceSet> query = em.createNamedQuery(ResourceSet.QUERY_ALL, ResourceSet.class);
		return query.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.ResourceSetRepository#getAllForClient(org.mitre.oauth2.model.ClientDetailsEntity)
	 */
	@Override
	public Collection<ResourceSet> getAllForClient(String clientId) {
		TypedQuery<ResourceSet> query = em.createNamedQuery(ResourceSet.QUERY_BY_CLIENT, ResourceSet.class);
		query.setParameter(ResourceSet.PARAM_CLIENTID, clientId);
		return query.getResultList();
	}

}
