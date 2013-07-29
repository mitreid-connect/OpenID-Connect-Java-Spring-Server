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
/**
 * 
 */
package org.mitre.oauth2.repository.impl;

import static org.mitre.util.jpa.JpaUtil.getSingleResult;
import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository("jpaSystemScopeRepository")
public class JpaSystemScopeRepository implements SystemScopeRepository {

	@PersistenceContext
	private EntityManager em;

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#getAll()
	 */
	@Override
	@Transactional
	public Set<SystemScope> getAll() {
		TypedQuery<SystemScope> query = em.createNamedQuery("SystemScope.findAll", SystemScope.class);

		return new LinkedHashSet<SystemScope>(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#getById(java.lang.Long)
	 */
	@Override
	@Transactional
	public SystemScope getById(Long id) {
		return em.find(SystemScope.class, id);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#getByValue(java.lang.String)
	 */
	@Override
	@Transactional
	public SystemScope getByValue(String value) {
		TypedQuery<SystemScope> query = em.createNamedQuery("SystemScope.getByValue", SystemScope.class);
		query.setParameter("value", value);
		return getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#remove(org.mitre.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional
	public void remove(SystemScope scope) {
		SystemScope found = getById(scope.getId());

		if (found != null) {
			em.remove(found);
		}

	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#save(org.mitre.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional
	public SystemScope save(SystemScope scope) {
		return saveOrUpdate(scope.getId(), em, scope);
	}

}
