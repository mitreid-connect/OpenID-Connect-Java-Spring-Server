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

package org.mitre.uma.repository.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.uma.model.Permission;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.repository.PermissionRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository
public class JpaPermissionRepository implements PermissionRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager em;

	@Override
	@Transactional(value="defaultTransactionManager")
	public PermissionTicket save(PermissionTicket p) {
		return JpaUtil.saveOrUpdate(p.getId(), em, p);
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PermissionRepository#getByTicket(java.lang.String)
	 */
	@Override
	public PermissionTicket getByTicket(String ticket) {
		TypedQuery<PermissionTicket> query = em.createNamedQuery(PermissionTicket.QUERY_TICKET, PermissionTicket.class);
		query.setParameter(PermissionTicket.PARAM_TICKET, ticket);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PermissionRepository#getAll()
	 */
	@Override
	public Collection<PermissionTicket> getAll() {
		TypedQuery<PermissionTicket> query = em.createNamedQuery(PermissionTicket.QUERY_ALL, PermissionTicket.class);
		return query.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PermissionRepository#saveRawPermission(org.mitre.uma.model.Permission)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public Permission saveRawPermission(Permission p) {
		return JpaUtil.saveOrUpdate(p.getId(), em, p);
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PermissionRepository#getById(java.lang.Long)
	 */
	@Override
	public Permission getById(Long permissionId) {
		return em.find(Permission.class, permissionId);
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PermissionRepository#getPermissionTicketsForResourceSet(org.mitre.uma.model.ResourceSet)
	 */
	@Override
	public Collection<PermissionTicket> getPermissionTicketsForResourceSet(ResourceSet rs) {
		TypedQuery<PermissionTicket> query = em.createNamedQuery(PermissionTicket.QUERY_BY_RESOURCE_SET, PermissionTicket.class);
		query.setParameter(PermissionTicket.PARAM_RESOURCE_SET_ID, rs.getId());
		return query.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PermissionRepository#remove(org.mitre.uma.model.PermissionTicket)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(PermissionTicket ticket) {
		PermissionTicket found = getByTicket(ticket.getTicket());
		if (found != null) {
			em.remove(found);
		}
	}

}
