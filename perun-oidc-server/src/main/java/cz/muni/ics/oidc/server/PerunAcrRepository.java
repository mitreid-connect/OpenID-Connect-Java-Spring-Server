package cz.muni.ics.oidc.server;

import cz.muni.ics.openid.connect.models.Acr;
import java.time.Instant;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for ACR model.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Repository
@Transactional(value = "defaultTransactionManager")
public class PerunAcrRepository {

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	private EntityManager manager;

	public Acr getActive(String sub, String clientId, String state) {
		TypedQuery<Acr> query = manager.createNamedQuery(Acr.GET_ACTIVE, Acr.class);
		query.setParameter(Acr.PARAM_SUB, sub);
		query.setParameter(Acr.PARAM_CLIENT_ID, clientId);
		query.setParameter(Acr.PARAM_STATE, state);
		query.setParameter(Acr.PARAM_EXPIRES_AT, now());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Acr getById(Long id) {
		TypedQuery<Acr> query = manager.createNamedQuery(Acr.GET_BY_ID, Acr.class);
		query.setParameter(Acr.PARAM_ID, id);
		query.setParameter(Acr.PARAM_EXPIRES_AT, now());

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	public Acr store(Acr acr) {
		Acr existing = getActive(acr.getSub(), acr.getClientId(), acr.getState());
		if (existing != null) {
			return existing;
		} else {
			Acr tmp = manager.merge(acr);
			manager.flush();
			return tmp;
		}
	}

	@Transactional
	public void remove(Long id) {
		Acr acr = getById(id);
		if (acr != null) {
			manager.remove(acr);
		}
	}

	@Transactional
	public void deleteExpired() {
		Query query = manager.createNamedQuery(Acr.DELETE_EXPIRED);
		query.setParameter(Acr.PARAM_EXPIRES_AT, now());
		query.executeUpdate();
	}

	private long now() {
		return Instant.now().toEpochMilli();
	}

}
