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
/**
 * 
 */
package org.mitre.oauth2.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.repository.AuthorizationCodeRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA AuthorizationCodeRepository implementation.
 * 
 * @author aanganes
 *
 */
@Repository
@Transactional
public class JpaAuthorizationCodeRepository implements AuthorizationCodeRepository {

	@PersistenceContext
	EntityManager manager;

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.AuthorizationCodeRepository#save(org.mitre.oauth2.model.AuthorizationCodeEntity)
	 */
	@Override
	@Transactional
	public AuthorizationCodeEntity save(AuthorizationCodeEntity authorizationCode) {

		return JpaUtil.saveOrUpdate(authorizationCode.getId(), manager, authorizationCode);

	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.AuthorizationCodeRepository#consume(java.lang.String)
	 */
	@Override
	@Transactional
	public OAuth2Authentication consume(String code) throws InvalidGrantException {

		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery("AuthorizationCodeEntity.getByValue", AuthorizationCodeEntity.class);
		query.setParameter("code", code);

		AuthorizationCodeEntity result = JpaUtil.getSingleResult(query.getResultList());

		if (result == null) {
			throw new InvalidGrantException("JpaAuthorizationCodeRepository: no authorization code found for value " + code);
		}

		OAuth2Authentication authRequest = result.getAuthentication();

		manager.remove(result);

		return authRequest;

	}

}
