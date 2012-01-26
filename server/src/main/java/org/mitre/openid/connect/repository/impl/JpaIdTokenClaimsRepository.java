package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mitre.openid.connect.model.IdTokenClaims;
import org.mitre.openid.connect.repository.IdTokenClaimsRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA IdTokenClaims repository implementation
 * 
 * @author Michael Joseph Walsh
 *
 */
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
