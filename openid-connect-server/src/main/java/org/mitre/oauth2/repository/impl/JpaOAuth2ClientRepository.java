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
package org.mitre.oauth2.repository.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository
@Transactional
public class JpaOAuth2ClientRepository implements OAuth2ClientRepository {

	@PersistenceContext
	private EntityManager manager;

	public JpaOAuth2ClientRepository() {

	}

	public JpaOAuth2ClientRepository(EntityManager manager) {
		this.manager = manager;
	}

	@Override
	public ClientDetailsEntity getById(Long id) {
		return manager.find(ClientDetailsEntity.class, id);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2ClientRepository#getClientById(java.lang.String)
	 */
	@Override
	public ClientDetailsEntity getClientByClientId(String clientId) {
		TypedQuery<ClientDetailsEntity> query = manager.createNamedQuery("ClientDetailsEntity.getByClientId", ClientDetailsEntity.class);
		query.setParameter("clientId", clientId);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2ClientRepository#saveClient(org.mitre.oauth2.model.ClientDetailsEntity)
	 */
	@Override
	public ClientDetailsEntity saveClient(ClientDetailsEntity client) {
		return JpaUtil.saveOrUpdate(client.getClientId(), manager, client);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2ClientRepository#deleteClient(org.mitre.oauth2.model.ClientDetailsEntity)
	 */
	@Override
	public void deleteClient(ClientDetailsEntity client) {
		ClientDetailsEntity found = getById(client.getId());
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("Client not found: " + client);
		}
	}

	@Override
	public ClientDetailsEntity updateClient(Long id, ClientDetailsEntity client) {
		// sanity check
		client.setId(id);

		return JpaUtil.saveOrUpdate(id, manager, client);
	}

	@Override
	public Collection<ClientDetailsEntity> getAllClients() {
		TypedQuery<ClientDetailsEntity> query = manager.createNamedQuery("ClientDetailsEntity.findAll", ClientDetailsEntity.class);
		return query.getResultList();
	}

}
