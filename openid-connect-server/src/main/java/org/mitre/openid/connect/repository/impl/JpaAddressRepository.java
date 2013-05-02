/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
 ******************************************************************************/
package org.mitre.openid.connect.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.repository.AddressRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

/**
 * JPA Address repository implementation
 * 
 * @author Michael Joseph Walsh
 * 
 */
@Repository
public class JpaAddressRepository implements AddressRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public Address getById(Long id) {
		return manager.find(Address.class, id);
	}

	@Override
	@Transactional
	public void remove(Address address) {
		Address found = manager.find(Address.class, address.getId());

		if (found != null) {
			manager.remove(address);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		Address found = getById(id);

		manager.remove(found);
	}

	@Override
	@Transactional
	public Address save(Address address) {
		return saveOrUpdate(address.getId(), manager, address);
	}
}
