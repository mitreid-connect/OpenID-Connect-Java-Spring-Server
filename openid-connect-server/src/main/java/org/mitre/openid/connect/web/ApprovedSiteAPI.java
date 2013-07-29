/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
/**
 * 
 */
package org.mitre.openid.connect.web;

import java.security.Principal;
import java.util.Collection;

import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/api/approved")
@PreAuthorize("hasRole('ROLE_USER')")
public class ApprovedSiteAPI {

	@Autowired
	private ApprovedSiteService approvedSiteService;

	@Autowired
	OAuth2TokenEntityService tokenServices;

	private static Logger logger = LoggerFactory.getLogger(ApprovedSiteAPI.class);

	/**
	 * Get a list of all of this user's approved sites
	 * @param m
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public String getAllApprovedSites(ModelMap m, Principal p) {

		Collection<ApprovedSite> all = approvedSiteService.getByUserId(p.getName());

		m.put("entity", all);

		return "jsonEntityView";
	}

	/**
	 * Delete an approved site
	 * 
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE)
	public String deleteApprovedSite(@PathVariable("id") Long id, ModelMap m, Principal p) {
		ApprovedSite approvedSite = approvedSiteService.getById(id);

		if (approvedSite == null) {
			logger.error("deleteApprovedSite failed; no approved site found for id: " + id);
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "Could not delete approved site. The requested approved site with id: " + id + " could not be found.");
			return "jsonErrorView";
		} else if (!approvedSite.getUserId().equals(p.getName())) {
			logger.error("deleteApprovedSite failed; principal "
					+ p.getName() + " does not own approved site" + id);
			m.put("code", HttpStatus.FORBIDDEN);
			m.put("errorMessage", "You do not have permission to delete this approved site. The approved site decision will not be deleted.");
			return "jsonErrorView";
		} else {
			m.put("code", HttpStatus.OK);
			approvedSiteService.remove(approvedSite);
		}

		return "httpCodeView";
	}

	/**
	 * Get a single approved site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.GET, produces = "application/json")
	public String getApprovedSite(@PathVariable("id") Long id, ModelMap m, Principal p) {
		ApprovedSite approvedSite = approvedSiteService.getById(id);
		if (approvedSite == null) {
			logger.error("getApprovedSite failed; no approved site found for id: " + id);
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested approved site with id: " + id + " could not be found.");
			return "jsonErrorView";
		} else if (!approvedSite.getUserId().equals(p.getName())) {
			logger.error("getApprovedSite failed; principal "
					+ p.getName() + " does not own approved site" + id);
			m.put("code", HttpStatus.FORBIDDEN);
			m.put("errorMessage", "You do not have permission to view this approved site.");
			return "jsonErrorView";
		} else {
			m.put("entity", approvedSite);
			return "jsonApprovedSiteView";
		}

	}
}
