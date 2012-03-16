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
