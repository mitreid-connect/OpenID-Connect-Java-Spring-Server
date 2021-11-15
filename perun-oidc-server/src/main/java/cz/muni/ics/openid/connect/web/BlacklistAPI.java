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
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cz.muni.ics.openid.connect.model.BlacklistedSite;
import cz.muni.ics.openid.connect.service.BlacklistedSiteService;
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
@RequestMapping("/" + BlacklistAPI.URL)
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j
public class BlacklistAPI {

	public static final String URL = RootController.API_URL + "/blacklist";

	@Autowired
	private BlacklistedSiteService blacklistService;

	private final Gson gson = new Gson();
	private final JsonParser parser = new JsonParser();

	/**
	 * Get a list of all blacklisted sites
	 * @param m
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllBlacklistedSites(ModelMap m) {

		Collection<BlacklistedSite> all = blacklistService.getAll();

		m.put(JsonEntityView.ENTITY, all);

		return JsonEntityView.VIEWNAME;
	}

	/**
	 * Create a new blacklisted site
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String addNewBlacklistedSite(@RequestBody String jsonString, ModelMap m, Principal p) {

		JsonObject json;

		BlacklistedSite blacklist = null;

		try {

			json = parser.parse(jsonString).getAsJsonObject();
			blacklist = gson.fromJson(json, BlacklistedSite.class);
			BlacklistedSite newBlacklist = blacklistService.saveNew(blacklist);
			m.put(JsonEntityView.ENTITY, newBlacklist);

		}
		catch (JsonSyntaxException e) {
			log.error("addNewBlacklistedSite failed due to JsonSyntaxException: ", e);
			m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not save new blacklisted site. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			log.error("addNewBlacklistedSite failed due to IllegalStateException", e);
			m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not save new blacklisted site. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		}

		return JsonEntityView.VIEWNAME;

	}

	/**
	 * Update an existing blacklisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateBlacklistedSite(@PathVariable("id") Long id, @RequestBody String jsonString, ModelMap m, Principal p) {

		JsonObject json;

		BlacklistedSite blacklist = null;

		try {

			json = parser.parse(jsonString).getAsJsonObject();
			blacklist = gson.fromJson(json, BlacklistedSite.class);

		}
		catch (JsonSyntaxException e) {
			log.error("updateBlacklistedSite failed due to JsonSyntaxException", e);
			m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not update blacklisted site. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			log.error("updateBlacklistedSite failed due to IllegalStateException", e);
			m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not update blacklisted site. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		}


		BlacklistedSite oldBlacklist = blacklistService.getById(id);

		if (oldBlacklist == null) {
			log.error("updateBlacklistedSite failed; blacklist with id " + id + " could not be found");
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not update blacklisted site. The requested blacklist with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {

			BlacklistedSite newBlacklist = blacklistService.update(oldBlacklist, blacklist);

			m.put(JsonEntityView.ENTITY, newBlacklist);

			return JsonEntityView.VIEWNAME;
		}
	}

	/**
	 * Delete a blacklisted site
	 *
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE)
	public String deleteBlacklistedSite(@PathVariable("id") Long id, ModelMap m) {
		BlacklistedSite blacklist = blacklistService.getById(id);

		if (blacklist == null) {
			log.error("deleteBlacklistedSite failed; blacklist with id " + id + " could not be found");
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not delete bladklist. The requested bladklist with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {
			m.put(HttpCodeView.CODE, HttpStatus.OK);
			blacklistService.remove(blacklist);
		}

		return HttpCodeView.VIEWNAME;
	}

	/**
	 * Get a single blacklisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getBlacklistedSite(@PathVariable("id") Long id, ModelMap m) {
		BlacklistedSite blacklist = blacklistService.getById(id);
		if (blacklist == null) {
			log.error("getBlacklistedSite failed; blacklist with id " + id + " could not be found");
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not delete bladklist. The requested bladklist with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {

			m.put(JsonEntityView.ENTITY, blacklist);

			return JsonEntityView.VIEWNAME;
		}

	}

}
