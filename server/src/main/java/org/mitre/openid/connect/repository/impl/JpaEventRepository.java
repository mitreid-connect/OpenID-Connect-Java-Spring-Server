package org.mitre.openid.connect.repository.impl;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mitre.openid.connect.model.Event;
import org.mitre.openid.connect.repository.EventRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA Event repository implementation
 * 
 * @author Michael Joseph Walsh
 * 
 */
public class JpaEventRepository implements EventRepository {
	
	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public Event getById(Long id) {
		return manager.find(Event.class, id);
	}

	@Override
	@Transactional
	public void remove(Event event) {
		Event found = manager.find(Event.class, event.getId());

		if (found != null) {
			manager.remove(event);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	@Transactional
	public void removeById(Long id) {
		Event found = getById(id);

		manager.remove(found);
	}

	@Override
	@Transactional
	public Event save(Event event) {
		return saveOrUpdate(event.getId(), manager, event);
	}
}
