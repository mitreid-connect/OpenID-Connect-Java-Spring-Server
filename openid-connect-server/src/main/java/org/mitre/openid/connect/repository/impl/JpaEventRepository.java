/*******************************************************************************
 * Copyright 2013 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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
package org.mitre.openid.connect.repository.impl;

import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.mitre.openid.connect.model.Event;
import org.mitre.openid.connect.repository.EventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

/**
 * JPA Event repository implementation
 * 
 * @author Michael Joseph Walsh
 * 
 */
@Repository
public class JpaEventRepository implements EventRepository {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional
	public Event getById(Long id) {
		return manager.find(Event.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Collection<Event> getEventsDuringPeriod(Date start, Date end, int startChunk, int chunkSize) {

		Query query = manager.createQuery("SELECT e FROM Event e WHERE e.timestamp BETWEEN :start AND :end");

		query = query.setParameter("start", start, TemporalType.DATE);
		query = query.setParameter("end", end, TemporalType.DATE);
		query = query.setFirstResult(startChunk);
		query = query.setMaxResults(chunkSize);

		return query.getResultList();
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
