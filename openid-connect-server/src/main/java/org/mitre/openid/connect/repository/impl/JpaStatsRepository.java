package org.mitre.openid.connect.repository.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.mitre.openid.connect.repository.StatsRepository;
import org.springframework.stereotype.Repository;

/**
 * @author zanitete
 *
 */
@Repository
public class JpaStatsRepository implements StatsRepository {

	@PersistenceContext
	EntityManager manager;

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Object[]> getAllApprovedSitesClientIdAndUserId() {
		Query query = manager.createNamedQuery("ApprovedSite.stats.getAllClientIdUserId");
		return query.getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Object[]> getAllApprovedSitesClientIdCount() {
		Query query = manager.createNamedQuery("ApprovedSite.stats.getAllClientIds");
		return query.getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Object[]> getAllClientIds() {
		Query query = manager.createNamedQuery("ClientDetailsEntity.stats.findAllIds");
		return query.getResultList();
	}

}
