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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mitre.openid.connect.model.ResourceSet;
import org.mitre.openid.connect.repository.ResourceSetRepository;
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

	@PersistenceContext
	private EntityManager em;
	private static Logger logger = LoggerFactory.getLogger(JpaResourceSetRepository.class);
	
	@Override
	@Transactional
	public ResourceSet save(ResourceSet rs) {
		return JpaUtil.saveOrUpdate(rs.getId(), em, rs);
	}

	@Override
	public ResourceSet getById(Long id) {
		return em.find(ResourceSet.class, id);
	}

	@Override
	@Transactional
	public void remove(ResourceSet rs) {
		ResourceSet found = getById(rs.getId());
		if (found != null) {
			em.remove(found);
		} else {
			logger.info("Tried to remove unknown resource set: " + rs.getId());
		}
	}

}
