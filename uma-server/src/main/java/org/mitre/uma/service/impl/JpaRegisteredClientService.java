/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

package org.mitre.uma.service.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.RegisteredClientService;
import org.mitre.uma.model.SavedRegisteredClient;
import org.mitre.uma.service.SavedRegisteredClientService;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Service
public class JpaRegisteredClientService implements RegisteredClientService, SavedRegisteredClientService{

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager em;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.RegisteredClientService#getByIssuer(java.lang.String)
	 */
	@Override
	public RegisteredClient getByIssuer(String issuer) {
		SavedRegisteredClient saved = getSavedRegisteredClientFromStorage(issuer);

		if (saved == null) {
			return null;
		} else {
			return saved.getRegisteredClient();
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.RegisteredClientService#save(java.lang.String, org.mitre.oauth2.model.RegisteredClient)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void save(String issuer, RegisteredClient client) {


		SavedRegisteredClient saved = getSavedRegisteredClientFromStorage(issuer);

		if (saved == null) {
			saved = new SavedRegisteredClient();
			saved.setIssuer(issuer);
		}

		saved.setRegisteredClient(client);

		em.persist(saved);

	}

	private SavedRegisteredClient getSavedRegisteredClientFromStorage(String issuer) {
		TypedQuery<SavedRegisteredClient> query = em.createQuery("SELECT c from SavedRegisteredClient c where c.issuer = :issuer", SavedRegisteredClient.class);
		query.setParameter("issuer", issuer);

		SavedRegisteredClient saved = JpaUtil.getSingleResult(query.getResultList());
		return saved;
	}

	/**
	 * @return
	 */
	@Override
	public Collection<SavedRegisteredClient> getAll() {
		TypedQuery<SavedRegisteredClient> query = em.createQuery("SELECT c from SavedRegisteredClient c", SavedRegisteredClient.class);
		return query.getResultList();
	}

}
