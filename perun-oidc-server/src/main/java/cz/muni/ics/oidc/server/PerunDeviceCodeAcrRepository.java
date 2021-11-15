package cz.muni.ics.oidc.server;

import cz.muni.ics.openid.connect.models.Acr;
import cz.muni.ics.openid.connect.models.DeviceCodeAcr;
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
public class PerunDeviceCodeAcrRepository {

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	private EntityManager manager;

	public DeviceCodeAcr getActiveByDeviceCode(String deviceCode) {
		TypedQuery<DeviceCodeAcr> query = manager.createNamedQuery(DeviceCodeAcr.GET_ACTIVE_BY_DEVICE_CODE,
				DeviceCodeAcr.class);
		query.setParameter(DeviceCodeAcr.PARAM_DEVICE_CODE, deviceCode);
		query.setParameter(Acr.PARAM_EXPIRES_AT, now());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public DeviceCodeAcr getByUserCode(String userCode) {
		TypedQuery<DeviceCodeAcr> query = manager.createNamedQuery(DeviceCodeAcr.GET_BY_USER_CODE, DeviceCodeAcr.class);
		query.setParameter(DeviceCodeAcr.PARAM_USER_CODE, userCode);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public DeviceCodeAcr getById(Long id) {
		TypedQuery<DeviceCodeAcr> query = manager.createNamedQuery(DeviceCodeAcr.GET_BY_ID, DeviceCodeAcr.class);
		query.setParameter(DeviceCodeAcr.PARAM_ID, id);
		query.setParameter(DeviceCodeAcr.PARAM_EXPIRES_AT, now());

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	public DeviceCodeAcr store(DeviceCodeAcr acr) {
		try {
			return getActiveByDeviceCode(acr.getDeviceCode());
		} catch (NoResultException e) {
			DeviceCodeAcr tmp = manager.merge(acr);
			manager.flush();
			return tmp;
		}
	}

	@Transactional
	public void remove(Long id) {
		DeviceCodeAcr acr = getById(id);
		if (acr != null) {
			manager.remove(acr);
		}
	}

	@Transactional
	public void deleteExpired() {
		Query query = manager.createNamedQuery(DeviceCodeAcr.DELETE_EXPIRED);
		query.setParameter(DeviceCodeAcr.PARAM_EXPIRES_AT, now());
		query.executeUpdate();
	}

	private long now() {
		return Instant.now().toEpochMilli();
	}

}
