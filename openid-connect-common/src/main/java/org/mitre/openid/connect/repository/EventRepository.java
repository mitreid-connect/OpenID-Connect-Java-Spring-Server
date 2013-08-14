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
package org.mitre.openid.connect.repository;

import java.util.Collection;
import java.util.Date;

import org.mitre.openid.connect.model.Event;

/**
 * Event repository interface
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface EventRepository {

	/**
	 * Returns the Event for the given id
	 * 
	 * @param id
	 *            id the id of the Event
	 * @return a valid Event if it exists, null otherwise
	 */
	public Event getById(Long id);

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
	 * Removes the given Event from the repository
	 * 
	 * @param event
	 *            the Event object to remove
	 */
	public void remove(Event event);

	/**
	 * Removes an Event from the repository
	 * 
	 * @param id
	 *            the id of the Event to remove
	 */
	public void removeById(Long id);

	/**
	 * Persists a Event
	 * 
	 * @param event
	 *            the Event to be saved
	 * @return
	 */
	public Event save(Event event);

}
