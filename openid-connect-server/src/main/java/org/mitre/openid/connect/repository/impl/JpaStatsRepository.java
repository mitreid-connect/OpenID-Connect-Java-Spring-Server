package org.mitre.openid.connect.repository.impl;

import java.util.ArrayList;
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
	public Collection<ApprovedSiteId> getAllApprovedSitesClientIdAndUserId() {
		Query query = manager.createNamedQuery("ApprovedSite.stats.getAllClientIdUserId");
		Collection<Object[]> result = query.getResultList();
		Collection<ApprovedSiteId> retList = new ArrayList<ApprovedSiteId>();
		for(Object[] row: result) {
			retList.add(new ApprovedSiteId((Long) row[0], (String) row[1], (String) row[2]));
		}
		return retList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<ApprovedSitePerClientCount> getAllApprovedSitesClientIdCount() {
		Query query = manager.createNamedQuery("ApprovedSite.stats.getAllClientIds");
		Collection<Object[]> result = query.getResultList();
		Collection<ApprovedSitePerClientCount> retList = new ArrayList<ApprovedSitePerClientCount>();
		for(Object[] row: result) {
			retList.add(new ApprovedSitePerClientCount((String) row[0], (Long) row[1]));
		}
		return retList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<ClientDetailsEntityId> getAllClientIds() {
		Query query = manager.createNamedQuery("ClientDetailsEntity.stats.findAllIds");
		Collection<Object[]> result = query.getResultList();
		Collection<ClientDetailsEntityId> retList = new ArrayList<ClientDetailsEntityId>();
		for(Object[] row: result) {
			retList.add(new ClientDetailsEntityId((Long) row[0], (String) row[1]));
		}
		return retList;
	}

}
