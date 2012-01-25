package org.mitre.openid.connect.service.impl;

import org.mitre.openid.connect.model.Event;
import org.mitre.openid.connect.repository.impl.JpaEventRepository;
import org.mitre.openid.connect.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the EventService
 * 
 * @author Michael Joseph Walsh
 *
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

	@Autowired
	private JpaEventRepository eventRepository;

	/**
	 * Default constructor
	 */	
	public EventServiceImpl() {

	}

    /**
     * Constructor for use in test harnesses. 
     * 
     * @param repository
     */	
	public EventServiceImpl(JpaEventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public void save(Event event) {
		eventRepository.save(event);
	}

	@Override
	public Event getById(Long id) {
		return eventRepository.getById(id);
	}

	@Override
	public void remove(Event event) {
		eventRepository.remove(event);
	}

	@Override
	public void removeById(Long id) {
		eventRepository.removeById(id);
	}

}
