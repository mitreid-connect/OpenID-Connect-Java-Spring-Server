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

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.Nonce;
import org.mitre.openid.connect.repository.NonceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

@Repository
public class JpaNonceRepository implements NonceRepository {

	@PersistenceContext
	private EntityManager manager;


	@Override
	@Transactional
	public Nonce getById(Long id) {
		return manager.find(Nonce.class, id);
	}

	@Override
	@Transactional
	public void remove(Nonce nonce) {
		Nonce found = manager.find(Nonce.class, nonce.getId());

		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("Nonce reporitory remove: Nonce with id " + nonce.getId() + " could not be found.");
		}

	}

	@Override
	@Transactional
	public Nonce save(Nonce nonce) {
		return saveOrUpdate(nonce.getId(), manager, nonce);
	}

	@Override
	@Transactional
	public Collection<Nonce> getAll() {
		TypedQuery<Nonce> query = manager.createNamedQuery("Nonce.getAll", Nonce.class);
		return query.getResultList();
	}

	@Override
	@Transactional
	public Collection<Nonce> getExpired() {
		TypedQuery<Nonce> query = manager.createNamedQuery("Nonce.getExpired", Nonce.class);
		return query.getResultList();
	}

	@Override
	@Transactional
	public Collection<Nonce> getByClientId(String clientId) {
		TypedQuery<Nonce> query = manager.createNamedQuery("Nonce.getByClientId", Nonce.class);
		query.setParameter("clientId", clientId);

		return query.getResultList();
	}

}
