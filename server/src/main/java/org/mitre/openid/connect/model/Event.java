package org.mitre.openid.connect.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;

/**
 * Class to contain a logged event in the system.
 * 
 * @author jricher
 *
 */

@Entity
public class Event {

	public static enum EventType { LOGIN, AUTHORIZATION, ACCESS }
	
	private Long id;
	private EventType type;
	private Date timestamp;

	/**
     * @return the id
     */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long getId() {
    	return id;
    }
	/**
     * @param id the id to set
     */
    public void setId(Long id) {
    	this.id = id;
    }
	/**
     * @return the type
     */
    public EventType getType() {
    	return type;
    }
	/**
     * @param type the type to set
     */
    public void setType(EventType type) {
    	this.type = type;
    }
	/**
     * @return the timestamp
     */
    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getTimestamp() {
    	return timestamp;
    }
	/**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
    	this.timestamp = timestamp;
    }
	
}
