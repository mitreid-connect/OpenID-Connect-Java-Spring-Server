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
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;
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
	public AuthorizationRequestHolder consume(String code) throws InvalidGrantException {
		
		TypedQuery<AuthorizationCodeEntity> query = manager.createNamedQuery("AuthorizationCodeEntity.getByValue", AuthorizationCodeEntity.class);
		query.setParameter("code", code);
		
		AuthorizationCodeEntity result = JpaUtil.getSingleResult(query.getResultList());
		
		if (result == null) {
			throw new InvalidGrantException("JpaAuthorizationCodeRepository: no authorization code found for value " + code);
		}
		
		AuthorizationRequestHolder authRequest = result.getAuthorizationRequestHolder();
		
		manager.remove(result);
		
		return authRequest;

	}

}
