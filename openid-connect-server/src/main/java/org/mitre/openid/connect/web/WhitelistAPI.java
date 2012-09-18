/**
 * 
 */
package org.mitre.openid.connect.web;

import java.security.Principal;
import java.util.Collection;

import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.WhitelistedSiteService;
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
@RequestMapping("/api/whitelist")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class WhitelistAPI {

	@Autowired
	private WhitelistedSiteService whitelistService;
	
	private Gson gson = new Gson();
	private JsonParser parser = new JsonParser();
	
	/**
	 * Get a list of all whitelisted sites
	 * @param m
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, headers = "Accept=application/json")
	public String getAllWhitelistedSites(ModelMap m) {
		
		Collection<WhitelistedSite> all = whitelistService.getAll();
		
		m.put("entity", all);
		
		return "jsonEntityView";
	}
	
	/**
	 * Create a new whitelisted site
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
	public String addNewWhitelistedSite(@RequestBody String jsonString, ModelMap m, Principal p) {
		
		JsonObject json = parser.parse(jsonString).getAsJsonObject();
		
		WhitelistedSite whitelist = gson.fromJson(json, WhitelistedSite.class);

		// save the id of the person who created this
		whitelist.setCreatorUserId(p.getName());
		
		WhitelistedSite newWhitelist = whitelistService.saveNew(whitelist);
		
		m.put("entity", newWhitelist);
		
		return "jsonEntityView";
		
	}
	
	/**
	 * Update an existing whitelisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public String updateWhitelistedSite(@PathVariable("id") Long id, @RequestBody String jsonString, ModelMap m, Principal p) {
		
		JsonObject json = parser.parse(jsonString).getAsJsonObject();
		
		WhitelistedSite whitelist = gson.fromJson(json, WhitelistedSite.class);
		
		WhitelistedSite oldWhitelist = whitelistService.getById(id);
		
		if (oldWhitelist == null) {
			m.put("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		} else {
			
			WhitelistedSite newWhitelist = whitelistService.update(oldWhitelist, whitelist);
			
			m.put("entity", newWhitelist);
			
			return "jsonEntityView";
		}
	}
	
	/**
	 * Delete a whitelisted site
	 * 
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public String deleteWhitelistedSite(@PathVariable("id") Long id, ModelMap m) {
		WhitelistedSite whitelist = whitelistService.getById(id);
		
		if (whitelist == null) {
			m.put("code", HttpStatus.NOT_FOUND);
		} else {
			whitelistService.remove(whitelist);
		}		
		
		return "httpCodeView";
	}
	
	/**
	 * Get a single whitelisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public String getWhitelistedSite(@PathVariable("id") Long id, ModelMap m) {
		WhitelistedSite whitelist = whitelistService.getById(id);
		if (whitelist == null) {
			m.put("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		} else {
		
			m.put("entity", whitelist);
			
			return "jsonEntityView";
		}
		
	}
}
