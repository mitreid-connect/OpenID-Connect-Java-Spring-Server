/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.openid.connect.web;

import java.util.Map;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
public class ManagerController {

	@Autowired
	private StatsService statsService;
	
	@Autowired
	private ConfigurationPropertiesBean configBean;

    @RequestMapping({"", "home", "index"})
    public String showHomePage(ModelMap m) {
    	
    	Map<String, Integer> summary = statsService.calculateSummaryStats();
    	
    	m.put("statsSummary", summary);
    	m.put("topbarTitle", configBean.getAdminConsoleTopbarTitle());
    	m.put("landingPageText", configBean.getAdminConsoleLandingPageText());
    	m.put("copyright", configBean.getAdminConsoleCopyrightFooter());
    	m.put("logoUrl", configBean.getLogoImageUrl());
    	
        return "home";
    }
    
    @RequestMapping({"about", "about/"})
    public String showAboutPage(ModelMap m) {
    	
    	m.put("topbarTitle", configBean.getAdminConsoleTopbarTitle());
    	m.put("landingPageText", configBean.getAdminConsoleLandingPageText());
    	m.put("copyright", configBean.getAdminConsoleCopyrightFooter());
    	m.put("logoUrl", configBean.getLogoImageUrl());
    	
    	return "about";
    }
    
    @RequestMapping({"stats", "stats/"})
    public String showStatsPage(ModelMap m) {
    	
    	Map<String, Integer> summary = statsService.calculateSummaryStats();
    	
    	m.put("statsSummary", summary);
    	m.put("statsSummary", summary);
    	m.put("topbarTitle", configBean.getAdminConsoleTopbarTitle());
    	m.put("landingPageText", configBean.getAdminConsoleLandingPageText());
    	m.put("copyright", configBean.getAdminConsoleCopyrightFooter());
    	m.put("logoUrl", configBean.getLogoImageUrl());
    	
    	return "stats";
    }
    
    @RequestMapping({"contact", "contact/"})
    public String showContactPage(ModelMap m) {
    	
    	m.put("topbarTitle", configBean.getAdminConsoleTopbarTitle());
    	m.put("landingPageText", configBean.getAdminConsoleLandingPageText());
    	m.put("copyright", configBean.getAdminConsoleCopyrightFooter());
    	m.put("logoUrl", configBean.getLogoImageUrl());
    	
    	return "contact";
    }

    @PreAuthorize("hasRole('ROLE_USER')") // TODO: this probably shouldn't be here
    @RequestMapping("manage/**")
    public String showClientManager(ModelMap m) {
    	// TODO: move view
    	
    	m.put("topbarTitle", configBean.getAdminConsoleTopbarTitle());
    	m.put("landingPageText", configBean.getAdminConsoleLandingPageText());
    	m.put("copyright", configBean.getAdminConsoleCopyrightFooter());
    	m.put("logoUrl", configBean.getLogoImageUrl());
    	
        return "admin/manage";
    }

	public StatsService getStatsService() {
		return statsService;
	}

	public void setStatsService(StatsService statsService) {
		this.statsService = statsService;
	}

	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}

}
