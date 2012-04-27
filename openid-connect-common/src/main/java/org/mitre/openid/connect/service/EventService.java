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
package org.mitre.openid.connect.service;

import java.util.Collection;
import java.util.Date;

import org.mitre.openid.connect.model.Event;

/**
 * Interface for Event service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface EventService {
	/**
	 * Returns the Events for a given Date range
	 * 
	 * @param start
	 *            the Date to start from
	 * @param end
	 *            the Date to end at
	 * @param startChunk
	 *            the start chuck of a list you desire
	 * @param chunkSize
	 *            the size of the chunk you desire           
	 *            
	 * @return a Collection of Events
	 */
	public Collection<Event> getEventsDuringPeriod(Date start, Date end, int startChunk, int chunkSize);

	/**
	 * Get Event by id
	 * 
	 * @param id
	 *            id for Event
	 * @return Event for id, or null
	 */
	public Event getById(Long id);

	/**
	 * Remove Event
	 * 
	 * @param event
	 *            Event to remove
	 */
	public void remove(Event event);

	/**
	 * Remove Event for id
	 * 
	 * @param id
	 *            id for Event to remove
	 */
	public void removeById(Long id);
	
	/**
	 * Save Event
	 * 
	 * @param event
	 *            Event to be saved
	 */
	public void save(Event event);
}
