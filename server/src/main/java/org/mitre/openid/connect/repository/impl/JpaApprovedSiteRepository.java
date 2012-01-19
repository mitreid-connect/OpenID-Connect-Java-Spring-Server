package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA ApprovedSite repository implementation
 * 
 * @author Michael Joseph Walsh
 *
 */
public class JpaApprovedSiteRepository implements ApprovedSiteRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public Collection<ApprovedSite> getAll() {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(
				"ApprovedSite.getAll", ApprovedSite.class);
		return query.getResultList();
	}

	@Override
	@Transactional
	public Collection<ApprovedSite> getByClientDetails(
			ClientDetailsEntity clientDetails) {

		TypedQuery<ApprovedSite> query = manager.createNamedQuery(
				"ApprovedSite.getByClientDetails", ApprovedSite.class);
		query.setParameter("approvedSiteClientDetails", clientDetails);

		List<ApprovedSite> found = query.getResultList();

		return found;
	}
	
	@Override
	@Transactional
	public ApprovedSite getById(Long id) {
		return manager.find(ApprovedSite.class, id);
	}

	@Override
	@Transactional
	public Collection<ApprovedSite> getByUserInfo(UserInfo userInfo) {
		TypedQuery<ApprovedSite> query = manager.createNamedQuery(
				"ApprovedSite.getByUserInfo", ApprovedSite.class);
		query.setParameter("approvedSiteUserInfo", userInfo);
		
		List<ApprovedSite> found = query.getResultList();
		
		return found;
	}

	@Override
	@Transactional
	public void remove(ApprovedSite approvedSite) {
		ApprovedSite found = manager.find(ApprovedSite.class,
				approvedSite.getId());
		
		if (found != null) {
			manager.remove(approvedSite);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		ApprovedSite found = getById(id);

		manager.remove(found);
	}

	@Override
	@Transactional
	public ApprovedSite save(ApprovedSite approvedSite) {
		return saveOrUpdate(approvedSite.getId(), manager, approvedSite);
	}
}
