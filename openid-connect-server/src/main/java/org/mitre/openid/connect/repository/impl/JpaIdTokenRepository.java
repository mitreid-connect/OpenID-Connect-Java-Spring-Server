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

import org.mitre.openid.connect.model.IdToken;
import org.mitre.openid.connect.repository.IdTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA IdToken repository implementation
 * 
 * @author Michael Joseph Walsh
 *
 */
@Repository
public class JpaIdTokenRepository implements IdTokenRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public IdToken getById(Long id) {
		return manager.find(IdToken.class, id);
	}

	@Override
	@Transactional
	public void remove(IdToken idToken) {
		IdToken found = manager.find(IdToken.class, idToken.getId());

		if (found != null) {
			manager.remove(idToken);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		IdToken found = getById(id);

		manager.remove(found);
	}

	@Override
	@Transactional
	public IdToken save(IdToken idToken) {
		return saveOrUpdate(idToken.getId(), manager, idToken);
	}

}
