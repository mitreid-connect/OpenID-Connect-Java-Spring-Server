package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA UserInfo repository implementation
 * 
 * @author Michael Joseph Walsh
 *
 */
@Repository
public class JpaUserInfoRepository implements UserInfoRepository {

	@PersistenceContext
    private EntityManager manager;
	
	@Override
    @Transactional	
	public UserInfo getByUserId(String userId) {
		return manager.find(UserInfo.class, userId);
	}	
	
	@Override
	@Transactional	
	public UserInfo save(UserInfo userInfo) {
		return saveOrUpdate(userInfo.getUserId(), manager, userInfo);
	}

	@Override
	@Transactional	
	public void remove(UserInfo userInfo) {
		
		UserInfo found = manager.find(UserInfo.class, userInfo.getUserId());
		
		if (found != null) {
			manager.remove(userInfo);
		} else {
			throw new IllegalArgumentException();
		}		
	}

	@Override
	@Transactional		
	public void removeByUserId(String userId) {
		UserInfo found = manager.find(UserInfo.class, userId);
		
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException();
		}			
	}
	
	@Override
	@Transactional	
	public Collection<UserInfo> getAll() {
		
		TypedQuery<UserInfo> query = manager.createNamedQuery(
				"UserInfo.getAll", UserInfo.class);
		
		return query.getResultList();
	}

}
