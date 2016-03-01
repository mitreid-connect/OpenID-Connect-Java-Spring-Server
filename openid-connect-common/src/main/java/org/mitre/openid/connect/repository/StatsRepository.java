package org.mitre.openid.connect.repository;

import java.util.Vector;

/**
 * Repository that aggregates queries used to compute stats
 * in a more efficient way.
 *
 * @author zanitete
 *
 */
public interface StatsRepository {

	/**
	 * Return a collection of Object[], one for each ApprovedSite.
	 * The first element contains the clientId, the second contains the userId.
	 *
	 * @return
	 */
	Vector<Object[]> getAllApprovedSitesClientIdAndUserId();

	/**
	 * Return a collection of Object arrays. The first element of each array represents a clientId (String),
	 * the second is the number of ApprovedSites for that client (Long).
	 *
	 * @return
	 */
	Vector<Object[]> getAllApprovedSitesClientIdCount();

	/**
	 * Return a collection of Object[] containing the mapping between
	 * the client PK (Long) and the clientId (String).
	 *
	 * @return
	 */
	Vector<Object[]> getAllClientIds();
}
