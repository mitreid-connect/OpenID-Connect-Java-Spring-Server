package org.mitre.openid.connect.model;

import java.util.Date;

import javax.persistence.Entity;

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
	
}
