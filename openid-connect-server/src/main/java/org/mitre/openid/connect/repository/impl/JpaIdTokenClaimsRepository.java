/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mitre.openid.connect.model.IdTokenClaims;
import org.mitre.openid.connect.repository.IdTokenClaimsRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA IdTokenClaims repository implementation
 * 
 * @author Michael Joseph Walsh
 *
 */
@Repository
public class JpaIdTokenClaimsRepository implements IdTokenClaimsRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public IdTokenClaims getById(Long id) {
		return manager.find(IdTokenClaims.class, id);
	}

	@Override
	@Transactional
	public void remove(IdTokenClaims idTokenClaims) {
		IdTokenClaims found = manager.find(IdTokenClaims.class, idTokenClaims.getId());

		if (found != null) {
			manager.remove(idTokenClaims);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		IdTokenClaims found = getById(id);

		manager.remove(found);
	}

	@Override
	@Transactional
	public IdTokenClaims save(IdTokenClaims idTokenClaims) {
		return saveOrUpdate(idTokenClaims.getId(), manager, idTokenClaims);
	}
}
