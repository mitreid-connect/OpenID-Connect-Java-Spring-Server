/**
 * 
 */
package org.mitre.openid.connect.web;

import java.security.Principal;
import java.util.Collection;

import org.mitre.openid.connect.model.BlacklistedSite;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/api/blacklist")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class BlacklistAPI {


	@Autowired
	private BlacklistedSiteService blacklistService;
	
	private Gson gson = new Gson();
	private JsonParser parser = new JsonParser();
	
	/**
	 * Get a list of all blacklisted sites
	 * @param m
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, headers = "Accept=application/json")
	public String getAllBlacklistedSites(ModelMap m) {
		
		Collection<BlacklistedSite> all = blacklistService.getAll();
		
		m.put("entity", all);
		
		return "jsonEntityView";
	}
	
	/**
	 * Create a new blacklisted site
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
	public String addNewBlacklistedSite(@RequestBody String jsonString, ModelMap m, Principal p) {
		
		JsonObject json = parser.parse(jsonString).getAsJsonObject();
		
		BlacklistedSite blacklist = gson.fromJson(json, BlacklistedSite.class);

		BlacklistedSite newBlacklist = blacklistService.saveNew(blacklist);
		
		m.put("entity", newBlacklist);
		
		return "jsonEntityView";
		
	}
	
	/**
	 * Update an existing blacklisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public String updateBlacklistedSite(@PathVariable("id") Long id, @RequestBody String jsonString, ModelMap m, Principal p) {
		
		JsonObject json = parser.parse(jsonString).getAsJsonObject();
		
		BlacklistedSite blacklist = gson.fromJson(json, BlacklistedSite.class);
		
		BlacklistedSite oldBlacklist = blacklistService.getById(id);
		
		if (oldBlacklist == null) {
			m.put("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		} else {
			
			BlacklistedSite newBlacklist = blacklistService.update(oldBlacklist, blacklist);
			
			m.put("entity", newBlacklist);
			
			return "jsonEntityView";
		}
	}
	
	/**
	 * Delete a blacklisted site
	 * 
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public String deleteBlacklistedSite(@PathVariable("id") Long id, ModelMap m) {
		BlacklistedSite blacklist = blacklistService.getById(id);
		
		if (blacklist == null) {
			m.put("code", HttpStatus.NOT_FOUND);
		} else {
			blacklistService.remove(blacklist);
		}		
		
		return "httpCodeView";
	}
	
	/**
	 * Get a single blacklisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public String getBlacklistedSite(@PathVariable("id") Long id, ModelMap m) {
		BlacklistedSite blacklist = blacklistService.getById(id);
		if (blacklist == null) {
			m.put("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		} else {
		
			m.put("entity", blacklist);
			
			return "jsonEntityView";
		}
		
	}
}
