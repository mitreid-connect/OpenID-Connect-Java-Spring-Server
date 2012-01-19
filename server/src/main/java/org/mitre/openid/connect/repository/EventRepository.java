package org.mitre.openid.connect.repository;

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
