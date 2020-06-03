package org.mitre.xyz;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.util.jpa.JpaUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository
@Transactional(value = "defaultTransactionManager")
public class JpaTxService implements TxService {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;

	private TxEntity getById(Long id) {
		return manager.find(TxEntity.class, id);
	}

	@Override
	public TxEntity loadByHandle(String handle) {
		TypedQuery<TxEntity> query = manager.createNamedQuery(TxEntity.QUERY_BY_HANDLE, TxEntity.class);
		query.setParameter(TxEntity.PARAM_HANDLE, handle);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	public TxEntity loadByInteractUrl(String interaction) {
		TypedQuery<TxEntity> query = manager.createNamedQuery(TxEntity.QUERY_BY_INTERACTION, TxEntity.class);
		query.setParameter(TxEntity.PARAM_INTERACTION, interaction);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	public TxEntity save(TxEntity tx) {
		return JpaUtil.saveOrUpdate(tx.getId(), manager, tx);
	}

	@Override
	public void delete(TxEntity tx) {
		TxEntity found = getById(tx.getId());

		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("Transaction not found: " + tx);
		}

	}

}
