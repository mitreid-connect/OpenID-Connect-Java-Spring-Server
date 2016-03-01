package org.mitre.openid.connect.repository.impl;

import java.util.Vector;

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
	public Vector<Object[]> getAllApprovedSitesClientIdAndUserId() {
		Query query = manager.createNamedQuery("ApprovedSite.stats.getAllClientIdUserId");
		return (Vector<Object[]>) query.getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Vector<Object[]> getAllApprovedSitesClientIdCount() {
		Query query = manager.createNamedQuery("ApprovedSite.stats.getAllClientIds");
		return (Vector<Object[]>) query.getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Vector<Object[]> getAllClientIds() {
		Query query = manager.createNamedQuery("ClientDetailsEntity.stats.findAllIds");
		return (Vector<Object[]>) query.getResultList();
	}

}
