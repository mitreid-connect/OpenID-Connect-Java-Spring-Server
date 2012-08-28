/**
 * 
 */
package org.mitre.openid.connect.service;

import java.util.Map;

/**
 * @author jricher
 *
 */
public interface StatsService {

	/**
	 * Calculate summary statistics
	 *     	approvalCount: total approved sites
     *      userCount: unique users
   	 *      clientCount: unique clients
   	 *      
	 * @return
	 */
	public Map<String, Integer> calculateSummaryStats();

}
