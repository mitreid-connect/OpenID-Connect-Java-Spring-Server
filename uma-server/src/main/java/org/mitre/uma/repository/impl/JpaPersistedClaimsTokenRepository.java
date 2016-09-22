/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.uma.model.PersistedClaimsToken;
import org.mitre.uma.repository.PersistedClaimsTokenRepository;
import org.mitre.util.jpa.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository("persistedClaimsTokenRepository")
public class JpaPersistedClaimsTokenRepository implements PersistedClaimsTokenRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager em;
	private static Logger logger = LoggerFactory.getLogger(JpaPersistedClaimsTokenRepository.class);

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PersistedClaimsTokenRepository#getPersistedClaimsTokenByValue(java.lang.String)
	 */
	@Override
	public PersistedClaimsToken getByValue(String pctValue) {
		TypedQuery<PersistedClaimsToken> query = em.createNamedQuery(PersistedClaimsToken.QUERY_BY_VALUE, PersistedClaimsToken.class);
		query.setParameter(PersistedClaimsToken.PARAM_VALUE, pctValue);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.repository.PersistedClaimsTokenRepository#savePersistedClaimsToken(org.mitre.uma.model.PersistedClaimsToken)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public PersistedClaimsToken save(PersistedClaimsToken pct) {
		return JpaUtil.saveOrUpdate(pct.getId(), em, pct);
	}

}
