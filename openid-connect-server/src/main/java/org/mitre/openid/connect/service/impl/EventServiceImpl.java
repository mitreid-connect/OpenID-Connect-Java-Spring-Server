/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.openid.connect.service.impl;

import java.util.Collection;
import java.util.Date;

import org.mitre.openid.connect.model.Event;
import org.mitre.openid.connect.repository.EventRepository;
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
	private EventRepository eventRepository;

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
	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public Collection<Event> getEventsDuringPeriod(Date start, Date end,
			int startChunk, int chunkSize) {

		return eventRepository.getEventsDuringPeriod(start, end, startChunk, chunkSize); 
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

	@Override
	public void save(Event event) {
		eventRepository.save(event);
	}

}
