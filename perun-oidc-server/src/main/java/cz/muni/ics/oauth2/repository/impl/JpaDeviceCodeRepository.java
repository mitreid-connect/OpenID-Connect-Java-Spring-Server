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
package cz.muni.ics.oauth2.repository.impl;

import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.util.jpa.JpaUtil;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode getById(Long id) {
		return em.find(DeviceCode.class, id);
	}

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode getByUserCode(String value) {
		TypedQuery<DeviceCode> query = em.createNamedQuery(DeviceCode.QUERY_BY_USER_CODE, DeviceCode.class);
		query.setParameter(DeviceCode.PARAM_USER_CODE, value);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode getByDeviceCode(String value) {
		TypedQuery<DeviceCode> query = em.createNamedQuery(DeviceCode.QUERY_BY_DEVICE_CODE, DeviceCode.class);
		query.setParameter(DeviceCode.PARAM_DEVICE_CODE, value);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void remove(DeviceCode scope) {
		DeviceCode found = getById(scope.getId());

		if (found != null) {
			em.remove(found);
		}

	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.SystemScopeRepository#save(cz.muni.ics.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public DeviceCode save(DeviceCode scope) {
		return JpaUtil.saveOrUpdate(em, scope);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.repository.impl.DeviceCodeRepository#getExpiredCodes()
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public Collection<DeviceCode> getExpiredCodes() {
		TypedQuery<DeviceCode> query = em.createNamedQuery(DeviceCode.QUERY_EXPIRED_BY_DATE, DeviceCode.class);
		query.setParameter(DeviceCode.PARAM_DATE, new Date());
		return query.getResultList();
	}

}
