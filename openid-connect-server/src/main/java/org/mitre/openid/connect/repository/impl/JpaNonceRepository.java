package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.Nonce;
import org.mitre.openid.connect.repository.NonceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
