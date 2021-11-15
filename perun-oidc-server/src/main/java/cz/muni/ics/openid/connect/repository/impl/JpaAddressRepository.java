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
package cz.muni.ics.openid.connect.repository.impl;

import cz.muni.ics.openid.connect.model.Address;
import cz.muni.ics.openid.connect.repository.AddressRepository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA Address repository implementation
 *
 * @author Michael Joseph Walsh
 *
 */
@Repository
public class JpaAddressRepository implements AddressRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;

	@Override
	@Transactional(value="defaultTransactionManager")
	public Address getById(Long id) {
		return manager.find(Address.class, id);
	}

}
