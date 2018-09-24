package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.host.service.HostInfoService;
import org.mitre.openid.connect.model.DefaultUser;
import org.mitre.openid.connect.repository.UserRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author nkabiliravi
 *
 */
@Repository("jpaUserRepository")
public class JpaUserRepository implements UserRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;
	
	@Autowired
	HostInfoService hostInfoService;
	
	public JpaUserRepository() {

	}

	public JpaUserRepository(EntityManager manager) {
		this.manager = manager;
	}
	
	@Override
	public DefaultUser getById(String uuid) {
		DefaultUser entity = manager.find(DefaultUser.class, uuid);
		if (entity == null) {
			throw new IllegalArgumentException("DefaultUser not found: " + uuid);
		}
		hostInfoService.validateHost(entity.getHostUuid());
		return entity;
	}

	@Override
	public DefaultUser getUserByUsername(String username) {
		TypedQuery<DefaultUser> query = manager.createNamedQuery(DefaultUser.QUERY_BY_USER_NAME, DefaultUser.class);
		query.setParameter(DefaultUser.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		query.setParameter(DefaultUser.PARAM_USER_NAME, username);
		return JpaUtil.getSingleResult(query.getResultList());
	}

	@Override
	public DefaultUser saveUser(DefaultUser user) {
		user.setHostUuid(hostInfoService.getCurrentHostUuid());
		return JpaUtil.saveOrUpdate(user.getUuid(), manager, user);
	}

	@Override
	public void deleteUser(DefaultUser user) {
		DefaultUser found = getById(user.getUuid());
		manager.remove(found);
	}

	@Override
	public DefaultUser updateUser(String uuid, DefaultUser user) {
		// sanity check
		user.setUuid(uuid);

		hostInfoService.validateHost(user.getHostUuid());
		
		return saveOrUpdate(uuid, manager, user);
	}

	@Override
	public Collection<DefaultUser> getAllUsers() {
		TypedQuery<DefaultUser> query = manager.createNamedQuery(DefaultUser.QUERY_ALL, DefaultUser.class);
		query.setParameter(DefaultUser.PARAM_HOST_UUID, hostInfoService.getCurrentHostUuid());
		return query.getResultList();
	}

}
