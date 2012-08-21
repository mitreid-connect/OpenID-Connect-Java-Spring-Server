package org.mitre.openid.connect.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ROLE_USER')")
@RequestMapping("stats")
public class StatsAPI {
	
	@Autowired
	private ApprovedSiteService approvedSiteService;
	
	public StatsAPI() {
		
	}

	@RequestMapping("summary")
	public String statsSummary(ModelMap m) {
		
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
		
		m.put("entity", e);
		
		return "statsSummaryJson";
		
	}
	
}
