/**
 * 
 */
package org.mitre.openid.connect.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jricher
 *
 */
@Service
public class DefaultStatsService implements StatsService {

	@Autowired
	private ApprovedSiteService approvedSiteService;
	
	@Override
    public Map<String, Integer> calculateSummaryStats() {
        // get all approved sites
    	Collection<ApprovedSite> allSites = approvedSiteService.getAll();
    
    	// process to find number of unique users and sites
    	Set<String> userIds = new HashSet<String>();
    	Set<String> clientIds = new HashSet<String>();
    	for (ApprovedSite approvedSite : allSites) {
            userIds.add(approvedSite.getUserId());
            clientIds.add(approvedSite.getClientId());
        }
    	
    	Map<String, Integer> e = new HashMap<String, Integer>();
    	
    	e.put("approvalCount", allSites.size());
    	e.put("userCount", userIds.size());
    	e.put("clientCount", clientIds.size());
        return e;
    }

}
