package org.mitre.discovery.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.discovery.model.DefaultHostInfo;
import org.mitre.discovery.model.HostInfo;
import org.mitre.discovery.repository.HostInfoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(value="defaultTransactionManager")
public class JpaHostInfoRepository implements HostInfoRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;

	@Override
	public HostInfo getByUuid(String uuid) {
		TypedQuery<DefaultHostInfo> query = manager.createNamedQuery(DefaultHostInfo.QUERY_BY_UUID, DefaultHostInfo.class);
		query.setParameter(DefaultHostInfo.PARAM_UUID, uuid);
		return query.getSingleResult();
	}

	@Override
	public HostInfo getByHost(String host) {
		TypedQuery<DefaultHostInfo> query = manager.createNamedQuery(DefaultHostInfo.QUERY_BY_HOST, DefaultHostInfo.class);
		query.setParameter(DefaultHostInfo.PARAM_HOST, host);
		return query.getSingleResult();
	}

}
