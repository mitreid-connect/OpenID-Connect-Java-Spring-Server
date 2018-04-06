package org.opal.data;

import static org.mitre.util.jpa.JpaUtil.getSingleResult;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.DefaultUserInfo;
import org.opal.data.model.FIAccess;
import org.springframework.transaction.annotation.Transactional;

public class CrudRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;
	
	@Transactional
	public void saveUserInfo(DefaultUserInfo user) {
		manager.persist(user);
	}
	
	@Transactional
	public void saveFIAccess(FIAccess access) {
		manager.persist(access);
	}
	
	
	public FIAccess getFIAccessByUsernameAndClientId(String username, String clientId) {
		TypedQuery<FIAccess> query = manager.createNamedQuery(FIAccess.QUERY_BY_USERNAME_AND_CLIENTID, FIAccess.class);
		query.setParameter(FIAccess.PARAM_USERNAME, username);
		query.setParameter(FIAccess.PARAM_CLIENT_ID, clientId);

		return getSingleResult(query.getResultList());

	}
}
