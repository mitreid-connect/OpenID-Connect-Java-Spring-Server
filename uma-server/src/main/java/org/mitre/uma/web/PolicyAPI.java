/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

package org.mitre.uma.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.openid.connect.web.RootController;
import org.mitre.uma.model.Claim;
import org.mitre.uma.model.Policy;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.ResourceSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;
import com.google.gson.Gson;

/**
 * API for managing policies on resource sets.
 *
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + PolicyAPI.URL)
@PreAuthorize("hasRole('ROLE_USER')")
public class PolicyAPI {

	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(PolicyAPI.class);

	public static final String URL = RootController.API_URL + "/resourceset";
	public static final String POLICYURL = "/policy";

	private Gson gson = new Gson();

	@Autowired
	private ResourceSetService resourceSetService;

	/**
	 * List all resource sets for the current user
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getResourceSetsForCurrentUser(Model m, Authentication auth) {

		Collection<ResourceSet> resourceSets = resourceSetService.getAllForOwner(auth.getName());

		m.addAttribute(JsonEntityView.ENTITY, resourceSets);

		return JsonEntityView.VIEWNAME;
	}

	/**
	 * Get the indicated resource set
	 * @param rsid
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "/{rsid}", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getResourceSet(@PathVariable (value = "rsid") Long rsid, Model m, Authentication auth) {

		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		if (!rs.getOwner().equals(auth.getName())) {
			logger.warn("Unauthorized resource set request from bad user; expected " + rs.getOwner() + " got " + auth.getName());

			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}

		m.addAttribute(JsonEntityView.ENTITY, rs);

		return JsonEntityView.VIEWNAME;
	}

	/**
	 * Delete the indicated resource set
	 * @param rsid
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "/{rsid}", method = RequestMethod.DELETE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String deleteResourceSet(@PathVariable (value = "rsid") Long rsid, Model m, Authentication auth) {

		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		if (!rs.getOwner().equals(auth.getName())) {
			logger.warn("Unauthorized resource set request from bad user; expected " + rs.getOwner() + " got " + auth.getName());

			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}

		resourceSetService.remove(rs);
		m.addAttribute(HttpCodeView.CODE, HttpStatus.NO_CONTENT);
		return HttpCodeView.VIEWNAME;

	}

	/**
	 * List all the policies for the given resource set
	 * @param rsid
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "/{rsid}" + POLICYURL, method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getPoliciesForResourceSet(@PathVariable (value = "rsid") Long rsid, Model m, Authentication auth) {

		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		if (!rs.getOwner().equals(auth.getName())) {
			logger.warn("Unauthorized resource set request from bad user; expected " + rs.getOwner() + " got " + auth.getName());

			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}

		m.addAttribute(JsonEntityView.ENTITY, rs.getPolicies());

		return JsonEntityView.VIEWNAME;
	}

	/**
	 * Create a new policy on the given resource set
	 * @param rsid
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "/{rsid}" + POLICYURL, method = RequestMethod.POST, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String createNewPolicyForResourceSet(@PathVariable (value = "rsid") Long rsid, @RequestBody String jsonString, Model m, Authentication auth) {
		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		if (!rs.getOwner().equals(auth.getName())) {
			logger.warn("Unauthorized resource set request from bad user; expected " + rs.getOwner() + " got " + auth.getName());

			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}

		Policy p = gson.fromJson(jsonString, Policy.class);

		if (p.getId() != null) {
			logger.warn("Tried to add a policy with a non-null ID: " + p.getId());
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		}

		for (Claim claim : p.getClaimsRequired()) {
			if (claim.getId() != null) {
				logger.warn("Tried to add a policy with a non-null claim ID: " + claim.getId());
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				return HttpCodeView.VIEWNAME;
			}
		}

		rs.getPolicies().add(p);
		ResourceSet saved = resourceSetService.update(rs, rs);

		// find the new policy object
		Collection<Policy> newPolicies = Sets.difference(new HashSet<>(saved.getPolicies()), new HashSet<>(rs.getPolicies()));

		if (newPolicies.size() == 1) {
			Policy newPolicy = newPolicies.iterator().next();
			m.addAttribute(JsonEntityView.ENTITY, newPolicy);
			return JsonEntityView.VIEWNAME;
		} else {
			logger.warn("Unexpected result trying to add a new policy object: " + newPolicies);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
			return HttpCodeView.VIEWNAME;
		}

	}

	/**
	 * Get a specific policy
	 * @param rsid
	 * @param pid
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "/{rsid}" + POLICYURL + "/{pid}", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getPolicy(@PathVariable (value = "rsid") Long rsid, @PathVariable (value = "pid") Long pid, Model m, Authentication auth) {

		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		if (!rs.getOwner().equals(auth.getName())) {
			logger.warn("Unauthorized resource set request from bad user; expected " + rs.getOwner() + " got " + auth.getName());

			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}

		for (Policy policy : rs.getPolicies()) {
			if (policy.getId().equals(pid)) {
				// found it!
				m.addAttribute(JsonEntityView.ENTITY, policy);
				return JsonEntityView.VIEWNAME;
			}
		}

		// if we made it this far, we haven't found it
		m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
		return HttpCodeView.VIEWNAME;
	}

	/**
	 * Update a specific policy
	 * @param rsid
	 * @param pid
	 * @param jsonString
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "/{rsid}" + POLICYURL + "/{pid}", method = RequestMethod.PUT, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String setClaimsForResourceSet(@PathVariable (value = "rsid") Long rsid, @PathVariable (value = "pid") Long pid, @RequestBody String jsonString, Model m, Authentication auth) {

		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		if (!rs.getOwner().equals(auth.getName())) {
			logger.warn("Unauthorized resource set request from bad user; expected " + rs.getOwner() + " got " + auth.getName());

			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}

		Policy p = gson.fromJson(jsonString, Policy.class);

		if (!pid.equals(p.getId())) {
			logger.warn("Policy ID mismatch, expected " + pid + " got " + p.getId());

			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		}

		for (Policy policy : rs.getPolicies()) {
			if (policy.getId().equals(pid)) {
				// found it!

				// find the existing claim IDs, make sure we're not overwriting anything from another policy
				Set<Long> claimIds = new HashSet<>();
				for (Claim claim : policy.getClaimsRequired()) {
					claimIds.add(claim.getId());
				}

				for (Claim claim : p.getClaimsRequired()) {
					if (claim.getId() != null && !claimIds.contains(claim.getId())) {
						logger.warn("Tried to add a policy with a an unmatched claim ID: got " + claim.getId() + " expected " + claimIds);
						m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
						return HttpCodeView.VIEWNAME;
					}
				}

				// update the existing object with the new values
				policy.setClaimsRequired(p.getClaimsRequired());
				policy.setName(p.getName());
				policy.setScopes(p.getScopes());

				resourceSetService.update(rs, rs);

				m.addAttribute(JsonEntityView.ENTITY, policy);
				return JsonEntityView.VIEWNAME;
			}
		}

		// if we made it this far, we haven't found it
		m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
		return HttpCodeView.VIEWNAME;
	}

	/**
	 * Delete a specific policy
	 * @param rsid
	 * @param pid
	 * @param m
	 * @param auth
	 * @return
	 */
	@RequestMapping(value = "/{rsid}" + POLICYURL + "/{pid}", method = RequestMethod.DELETE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String deleteResourceSet(@PathVariable ("rsid") Long rsid, @PathVariable (value = "pid") Long pid, Model m, Authentication auth) {

		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.addAttribute(JsonErrorView.ERROR, "not_found");
			return JsonErrorView.VIEWNAME;
		}

		if (!auth.getName().equals(rs.getOwner())) {

			logger.warn("Unauthorized resource set request from bad user; expected " + rs.getOwner() + " got " + auth.getName());

			// it wasn't issued to this user
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return JsonErrorView.VIEWNAME;
		}


		for (Policy policy : rs.getPolicies()) {
			if (policy.getId().equals(pid)) {
				// found it!
				rs.getPolicies().remove(policy);
				resourceSetService.update(rs, rs);

				m.addAttribute(HttpCodeView.CODE, HttpStatus.NO_CONTENT);
				return HttpCodeView.VIEWNAME;
			}
		}

		// if we made it this far, we haven't found it
		m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
		return HttpCodeView.VIEWNAME;

	}

}
