/**
 * 
 */
package org.mitre.oauth2.repository.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.mitre.util.jpa.JpaUtil.getSingleResult;
import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

/**
 * @author jricher
 *
 */
@Repository("jpaSystemScopeRepository")
public class JpaSystemScopeRepository implements SystemScopeRepository {

	@PersistenceUnit
	private EntityManager em;
	
	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#getAll()
	 */
	@Override
	@Transactional
	public Set<SystemScope> getAll() {
		TypedQuery<SystemScope> query = em.createNamedQuery("SystemScope.findAll", SystemScope.class);
		
		return new HashSet<SystemScope>(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#getById(java.lang.Long)
	 */
	@Override
	@Transactional
	public SystemScope getById(Long id) {
		return em.find(SystemScope.class, id);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#getByValue(java.lang.String)
	 */
	@Override
	@Transactional
	public SystemScope getByValue(String value) {
		TypedQuery<SystemScope> query = em.createNamedQuery("SystemScope.getByValue", SystemScope.class);
		query.setParameter("value", value);
		return getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#remove(org.mitre.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional
	public void remove(SystemScope scope) {
		SystemScope found = getById(scope.getId());
		
		if (found != null) {
			em.remove(found);
		}

	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.SystemScopeRepository#save(org.mitre.oauth2.model.SystemScope)
	 */
	@Override
	@Transactional
	public SystemScope save(SystemScope scope) {
		return saveOrUpdate(scope.getId(), em, scope);
	}

}
