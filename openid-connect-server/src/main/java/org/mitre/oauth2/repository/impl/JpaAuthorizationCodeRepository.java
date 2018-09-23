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
package org.mitre.oauth2.repository.impl;

import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.data.PageCriteria;
import org.mitre.host.service.HostInfoService;
import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.repository.AuthorizationCodeRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	HostInfoService hostInfoService;

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.AuthorizationCodeRepository#save(org.mitre.oauth2.model.AuthorizationCodeEntity)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public AuthorizationCodeEntity save(AuthorizationCodeEntity authorizationCode) {

		hostInfoService.validateHost(authorizationCode.getHostUuid());
		
		return JpaUtil.saveOrUpdate(authorizationCode.getUuid(), manager, authorizationCode);

	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.AuthorizationCodeRepository#getByCode(java.lang.String)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public AuthorizationCodeEntity getByCode(String code) {
		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery(AuthorizationCodeEntity.QUERY_BY_VALUE, AuthorizationCodeEntity.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(AuthorizationCodeEntity.PARAM_CODE, code);

		AuthorizationCodeEntity result = JpaUtil.getSingleResult(query.getResultList());
		return result;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.AuthorizationCodeRepository#remove(org.mitre.oauth2.model.AuthorizationCodeEntity)
	 */
	@Override
	public void remove(AuthorizationCodeEntity authorizationCodeEntity) {
		AuthorizationCodeEntity found = manager.find(AuthorizationCodeEntity.class, authorizationCodeEntity.getUuid());
		if (found != null) {
			hostInfoService.validateHost(found.getHostUuid());
			manager.remove(found);
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.AuthorizationCodeRepository#getExpiredCodes()
	 */
	@Override
	public Collection<AuthorizationCodeEntity> getExpiredCodes() {
		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery(AuthorizationCodeEntity.QUERY_EXPIRATION_BY_DATE, AuthorizationCodeEntity.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(AuthorizationCodeEntity.PARAM_DATE, new Date()); // this gets anything that's already expired
		return query.getResultList();
	}


	@Override
	public Collection<AuthorizationCodeEntity> getExpiredCodes(PageCriteria pageCriteria) {
		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery(AuthorizationCodeEntity.QUERY_EXPIRATION_BY_DATE, AuthorizationCodeEntity.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(AuthorizationCodeEntity.PARAM_DATE, new Date()); // this gets anything that's already expired
		return JpaUtil.getResultPage(query, pageCriteria);
	}



}
