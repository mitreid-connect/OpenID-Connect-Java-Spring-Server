/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
 *******************************************************************************/
/**
 *
 */
package cz.muni.ics.openid.connect.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import cz.muni.ics.openid.connect.model.WhitelistedSite;
import cz.muni.ics.openid.connect.service.WhitelistedSiteService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import cz.muni.ics.openid.connect.view.JsonErrorView;
import java.security.Principal;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + WhitelistAPI.URL)
@PreAuthorize("hasRole('ROLE_USER')")
@Slf4j
public class WhitelistAPI {

	public static final String URL = RootController.API_URL + "/whitelist";

	@Autowired
	private WhitelistedSiteService whitelistService;

	private final Gson gson = new Gson();
	private final JsonParser parser = new JsonParser();

	/**
	 * Get a list of all whitelisted sites
	 * @param m
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllWhitelistedSites(ModelMap m) {

		Collection<WhitelistedSite> all = whitelistService.getAll();

		m.put(JsonEntityView.ENTITY, all);

		return JsonEntityView.VIEWNAME;
	}

	/**
	 * Create a new whitelisted site
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String addNewWhitelistedSite(@RequestBody String jsonString, ModelMap m, Principal p) {

		JsonObject json;

		WhitelistedSite whitelist = null;
		try {
			json = parser.parse(jsonString).getAsJsonObject();
			whitelist = gson.fromJson(json, WhitelistedSite.class);

		} catch (JsonParseException e) {
			log.error("addNewWhitelistedSite failed due to JsonParseException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not save new whitelisted site. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			log.error("addNewWhitelistedSite failed due to IllegalStateException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not save new whitelisted site. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		}

		// save the id of the person who created this
		whitelist.setCreatorUserId(p.getName());

		WhitelistedSite newWhitelist = whitelistService.saveNew(whitelist);

		m.put(JsonEntityView.ENTITY, newWhitelist);

		return JsonEntityView.VIEWNAME;

	}

	/**
	 * Update an existing whitelisted site
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateWhitelistedSite(@PathVariable("id") Long id, @RequestBody String jsonString, ModelMap m, Principal p) {

		JsonObject json;

		WhitelistedSite whitelist = null;
		try {
			json = parser.parse(jsonString).getAsJsonObject();
			whitelist = gson.fromJson(json, WhitelistedSite.class);

		} catch (JsonParseException e) {
			log.error("updateWhitelistedSite failed due to JsonParseException", e);
			m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not update whitelisted site. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			log.error("updateWhitelistedSite failed due to IllegalStateException", e);
			m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not update whitelisted site. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		}

		WhitelistedSite oldWhitelist = whitelistService.getById(id);

		if (oldWhitelist == null) {
			log.error("updateWhitelistedSite failed; whitelist with id " + id + " could not be found.");
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not update whitelisted site. The requested whitelisted site with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {

			WhitelistedSite newWhitelist = whitelistService.update(oldWhitelist, whitelist);

			m.put(JsonEntityView.ENTITY, newWhitelist);

			return JsonEntityView.VIEWNAME;
		}
	}

	/**
	 * Delete a whitelisted site
	 *
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE)
	public String deleteWhitelistedSite(@PathVariable("id") Long id, ModelMap m) {
		WhitelistedSite whitelist = whitelistService.getById(id);

		if (whitelist == null) {
			log.error("deleteWhitelistedSite failed; whitelist with id " + id + " could not be found.");
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not delete whitelisted site. The requested whitelisted site with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {
			m.put(HttpCodeView.CODE, HttpStatus.OK);
			whitelistService.remove(whitelist);
		}

		return HttpCodeView.VIEWNAME;
	}

	/**
	 * Get a single whitelisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getWhitelistedSite(@PathVariable("id") Long id, ModelMap m) {
		WhitelistedSite whitelist = whitelistService.getById(id);
		if (whitelist == null) {
			log.error("getWhitelistedSite failed; whitelist with id " + id + " could not be found.");
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested whitelisted site with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {

			m.put(JsonEntityView.ENTITY, whitelist);

			return JsonEntityView.VIEWNAME;
		}

	}

}
