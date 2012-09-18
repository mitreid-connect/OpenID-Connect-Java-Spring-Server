package org.mitre.openid.connect.web;

import java.util.Map;

import org.mitre.openid.connect.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ROLE_USER')")
@RequestMapping("stats")
public class StatsAPI {
	
	@Autowired
	private StatsService statsService;
	
	@RequestMapping("summary")
	public String statsSummary(ModelMap m) {
		
		Map<String, Integer> e = statsService.calculateSummaryStats();
		
		m.put("entity", e);
		
		return "statsSummaryJson";
		
	}
	
}
