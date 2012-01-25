package org.mitre.openid.connect.service;

import org.mitre.openid.connect.model.Event;

/**
 * Interface for Event service
 * 
 * @author Michael Joseph Walsh
 * 
 */
public interface EventService {

	/**
	 * Save Event
	 * 
	 * @param event
	 *            Event to be saved
	 */
	public void save(Event event);

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
}
