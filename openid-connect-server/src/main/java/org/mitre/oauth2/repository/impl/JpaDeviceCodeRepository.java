/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

import static org.mitre.util.jpa.JpaUtil.getSingleResult;
import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.host.service.HostInfoService;
import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.model.DeviceCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository("jpaDeviceCodeRepository")
public class JpaDeviceCodeRepository implements DeviceCodeRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager em;
	
	@Autowired
	HostInfoService hostInfoService;

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode getById(String uuid) {
		DeviceCode entity = em.find(DeviceCode.class, uuid);
		if (entity == null) {
			throw new IllegalArgumentException("DeviceCode not found: " + uuid);
		}
		hostInfoService.validateHost(entity.getHostUuid());
		return entity;
	}

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode getByUserCode(String value) {
		TypedQuery<DeviceCode> query = em.createNamedQuery(DeviceCode.QUERY_BY_USER_CODE, DeviceCode.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(DeviceCode.PARAM_USER_CODE, value);
		return getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode getByDeviceCode(String value) {
		TypedQuery<DeviceCode> query = em.createNamedQuery(DeviceCode.QUERY_BY_DEVICE_CODE, DeviceCode.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(DeviceCode.PARAM_DEVICE_CODE, value);
		return getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(DeviceCode scope) {
		DeviceCode found = getById(scope.getUuid());
		em.remove(found);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#save(org.mitre.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode save(DeviceCode scope) {
		hostInfoService.validateHost(scope.getHostUuid());
		return saveOrUpdate(scope.getUuid(), em, scope);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.impl.DeviceCodeRepository#getExpiredCodes()
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<DeviceCode> getExpiredCodes() {
		TypedQuery<DeviceCode> query = em.createNamedQuery(DeviceCode.QUERY_EXPIRED_BY_DATE, DeviceCode.class);
		query.setParameter(AuthorizationCodeEntity.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(DeviceCode.PARAM_DATE, new Date());
		return query.getResultList();
	}

}
