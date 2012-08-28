/**
 * 
 */
package org.mitre.openid.connect.web;

import java.security.Principal;
import java.util.Collection;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/api/approved")
@PreAuthorize("hasRole('ROLE_USER')")
public class ApprovedSiteApi {

	@Autowired
	private ApprovedSiteService approvedSiteService;
	
	private Gson gson = new Gson();
	private JsonParser parser = new JsonParser();

	/**
	 * Get a list of all of this user's approved sites
	 * @param m
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, headers = "Accept=application/json")
	public String getAllApprovedSites(ModelMap m, Principal p) {
		
		Collection<ApprovedSite> all = approvedSiteService.getByUserId(p.getName());
		
		m.put("entity", all);
		
		return "jsonEntityView";
	}
	
	/**
	 * Delete an approved site
	 * 
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public String deleteApprovedSite(@PathVariable("id") Long id, ModelMap m, Principal p) {
		ApprovedSite approvedSite = approvedSiteService.getById(id);
		
		if (approvedSite == null) {
			m.put("code", HttpStatus.NOT_FOUND);
		} else if (!approvedSite.getUserId().equals(p.getName())) {
			m.put("code", HttpStatus.FORBIDDEN);
		} else {
			approvedSiteService.remove(approvedSite);
		}		
		
		return "httpCodeView";
	}
	
	/**
	 * Get a single approved site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public String getApprovedSite(@PathVariable("id") Long id, ModelMap m, Principal p) {
		ApprovedSite approvedSite = approvedSiteService.getById(id);
		if (approvedSite == null) {
			m.put("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		} else if (!approvedSite.getUserId().equals(p.getName())) {
			m.put("code", HttpStatus.FORBIDDEN);
			return "httpCodeView";
		} else {
			m.put("entity", approvedSite);
			return "jsonEntityView";
		}
		
	}
}
