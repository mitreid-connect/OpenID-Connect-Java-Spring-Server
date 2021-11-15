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

import cz.muni.ics.data.PageCriteria;
import cz.muni.ics.oauth2.model.AuthorizationCodeEntity;
import cz.muni.ics.oauth2.repository.AuthorizationCodeRepository;
import cz.muni.ics.util.jpa.JpaUtil;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA AuthorizationCodeRepository implementation.
 *
 * @author aanganes
 *
 */
@Repository
@Transactional(value="defaultTransactionManager")
public class JpaAuthorizationCodeRepository implements AuthorizationCodeRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	EntityManager manager;

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.AuthorizationCodeRepository#save(cz.muni.ics.oauth2.model.AuthorizationCodeEntity)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public AuthorizationCodeEntity save(AuthorizationCodeEntity authorizationCode) {

		return JpaUtil.saveOrUpdate(manager, authorizationCode);

	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.AuthorizationCodeRepository#getByCode(java.lang.String)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public AuthorizationCodeEntity getByCode(String code) {
		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery(AuthorizationCodeEntity.QUERY_BY_VALUE, AuthorizationCodeEntity.class);
		query.setParameter("code", code);

		AuthorizationCodeEntity result = JpaUtil.getSingleResult(query.getResultList());
		return result;
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.AuthorizationCodeRepository#remove(cz.muni.ics.oauth2.model.AuthorizationCodeEntity)
	 */
	@Override
	public void remove(AuthorizationCodeEntity authorizationCodeEntity) {
		AuthorizationCodeEntity found = manager.find(AuthorizationCodeEntity.class, authorizationCodeEntity.getId());
		if (found != null) {
			manager.remove(found);
		}
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.AuthorizationCodeRepository#getExpiredCodes()
	 */
	@Override
	public Collection<AuthorizationCodeEntity> getExpiredCodes() {
		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery(AuthorizationCodeEntity.QUERY_EXPIRATION_BY_DATE, AuthorizationCodeEntity.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_DATE, new Date()); // this gets anything that's already expired
		return query.getResultList();
	}


	@Override
	public Collection<AuthorizationCodeEntity> getExpiredCodes(PageCriteria pageCriteria) {
		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery(AuthorizationCodeEntity.QUERY_EXPIRATION_BY_DATE, AuthorizationCodeEntity.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_DATE, new Date()); // this gets anything that's already expired
		return JpaUtil.getResultPage(query, pageCriteria);
	}



}
