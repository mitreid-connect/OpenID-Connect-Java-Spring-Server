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

package org.mitre.uma.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.uma.model.Permission;
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

	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional
	public Permission save(Permission p) {
		return JpaUtil.saveOrUpdate(p.getId(), em, p);
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PermissionRepository#getByTicket(java.lang.String)
	 */
	@Override
	public Permission getByTicket(String ticket) {
		TypedQuery<Permission> query = em.createNamedQuery(Permission.QUERY_TICKET, Permission.class);
		query.setParameter(Permission.PARAM_TICKET, ticket);
		return JpaUtil.getSingleResult(query.getResultList());
	}

}
