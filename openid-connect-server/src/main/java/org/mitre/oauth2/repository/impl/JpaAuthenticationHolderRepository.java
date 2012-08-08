package org.mitre.oauth2.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.AuthenticationHolder;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class JpaAuthenticationHolderRepository implements AuthenticationHolderRepository {

	@PersistenceContext
	private EntityManager manager;
	
	@Override
	public AuthenticationHolder getById(Long id) {
		return manager.find(AuthenticationHolder.class, id);
	}

	@Override
	public AuthenticationHolder getByAuthentication(OAuth2Authentication a) {
		TypedQuery<AuthenticationHolder> query = manager.createNamedQuery("AuthenticationHolder.getByAuthentication", AuthenticationHolder.class);
		query.setParameter("authentication", a);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		AuthenticationHolder found = getById(id);
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("AuthenticationHolder not found: " + id);
		}
	}

	@Override
	@Transactional
	public void remove(AuthenticationHolder a) {
		AuthenticationHolder found = getById(a.getId());
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("AuthenticationHolder not found: " + a);
		}
	}

	@Override
	@Transactional
	public AuthenticationHolder save(AuthenticationHolder a) {
		return JpaUtil.saveOrUpdate(a.getId(), manager, a);
	}

}
