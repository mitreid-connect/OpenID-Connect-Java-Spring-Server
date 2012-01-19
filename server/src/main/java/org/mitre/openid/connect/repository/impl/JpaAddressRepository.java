package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.repository.AddressRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA Address repository implementation
 * 
 * @author Michael Joseph Walsh
 * 
 */
public class JpaAddressRepository implements AddressRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public Address getById(Long id) {
		return manager.find(Address.class, id);
	}

	@Override
	@Transactional
	public void remove(Address address) {
		Address found = manager.find(Address.class, address.getId());

		if (found != null) {
			manager.remove(address);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		Address found = getById(id);

		manager.remove(found);
	}

	@Override
	@Transactional
	public Address save(Address address) {
		return saveOrUpdate(address.getId(), manager, address);
	}
}
