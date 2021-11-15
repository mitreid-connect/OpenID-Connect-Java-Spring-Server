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
/**
 *
 */
package cz.muni.ics.oauth2.repository.impl;

import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.repository.SystemScopeRepository;
import cz.muni.ics.util.jpa.JpaUtil;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository("jpaSystemScopeRepository")
public class JpaSystemScopeRepository implements SystemScopeRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager em;

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.SystemScopeRepository#getAll()
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public Set<SystemScope> getAll() {
		TypedQuery<SystemScope> query = em.createNamedQuery(SystemScope.QUERY_ALL, SystemScope.class);

		return new LinkedHashSet<>(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.SystemScopeRepository#getById(java.lang.Long)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public SystemScope getById(Long id) {
		return em.find(SystemScope.class, id);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.SystemScopeRepository#getByValue(java.lang.String)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public SystemScope getByValue(String value) {
		TypedQuery<SystemScope> query = em.createNamedQuery(SystemScope.QUERY_BY_VALUE, SystemScope.class);
		query.setParameter(SystemScope.PARAM_VALUE, value);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.SystemScopeRepository#remove(cz.muni.ics.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(SystemScope scope) {
		SystemScope found = getById(scope.getId());

		if (found != null) {
			em.remove(found);
		}

	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.SystemScopeRepository#save(cz.muni.ics.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public SystemScope save(SystemScope scope) {
		return JpaUtil.saveOrUpdate(em, scope);
	}

}
