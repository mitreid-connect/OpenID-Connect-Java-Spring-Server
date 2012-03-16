package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.springframework.transaction.annotation.Transactional;

public class JpaWhitelistedSiteRepositiory implements WhitelistedSiteRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public Collection<WhitelistedSite> getAll() {
		TypedQuery<WhitelistedSite> query = manager.createNamedQuery(
				"WhitelistedSite.getAll", WhitelistedSite.class);
		return query.getResultList();
	}

	@Override
	@Transactional
	public WhitelistedSite getById(Long id) {
		return manager.find(WhitelistedSite.class, id);
	}

	@Override
	@Transactional
	public void remove(WhitelistedSite whitelistedSite) {
		WhitelistedSite found = manager.find(WhitelistedSite.class,
				whitelistedSite.getId());

		if (found != null) {
			manager.remove(whitelistedSite);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		WhitelistedSite found = getById(id);

		manager.remove(found);
	}

	@Override
	@Transactional
	public WhitelistedSite save(WhitelistedSite whiteListedSite) {
		return saveOrUpdate(whiteListedSite.getId(), manager, whiteListedSite);
	}
}
