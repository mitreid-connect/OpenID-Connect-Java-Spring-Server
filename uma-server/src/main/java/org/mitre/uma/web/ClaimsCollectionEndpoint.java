/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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

import java.util.Set;

import javax.servlet.http.HttpSession;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.uma.model.Claim;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.ClaimsProcessingService;
import org.mitre.uma.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * 
 * Collect claims interactively from the end user.
 * 
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + ClaimsCollectionEndpoint.URL)
public class ClaimsCollectionEndpoint {
	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(ClaimsCollectionEndpoint.class);

	public static final String URL = "rqp_claims";

	// variables for session storage
	private static final String TICKET = URL + "-ticket";
	private static final String CLIENT = URL + "-client";
	private static final String REDIRECT_URI = URL + "-redirect_uri";
	private static final String STATE = URL + "-state";

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ClaimsProcessingService claimsProcessingService;


	@RequestMapping(method = RequestMethod.GET)
	public String startClaimsCollection(@RequestParam("client_id") String clientId, @RequestParam(value = "redirect_uri", required = false) String redirectUri,
			@RequestParam("ticket") String ticketValue, @RequestParam(value = "state", required = false) String state,
			Model m, OIDCAuthenticationToken auth, HttpSession session) {


		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		PermissionTicket ticket = permissionService.getByTicket(ticketValue);

		if (client == null || ticket == null) {
			// couldn't find the client or the ticket, we bail here
			logger.info("Client or ticket not found: " + clientId + " :: " + ticketValue);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}
		
		// stash the client and ticket on the session and send the user to the claims endpoint
		session.setAttribute(TICKET, ticket);
		session.setAttribute(CLIENT, client);
		session.setAttribute(REDIRECT_URI, redirectUri);
		session.setAttribute(STATE, state);
		
		return "redirect:" + URL + "/collect";
	}
	
	@RequestMapping(value = "/collect", method = RequestMethod.GET)
	public String collectClaims(Model m, OIDCAuthenticationToken auth, HttpSession session) {
		
		ClientDetailsEntity client = (ClientDetailsEntity) session.getAttribute(CLIENT);
		PermissionTicket ticket = (PermissionTicket) session.getAttribute(TICKET);
		
		// we've got a client and ticket, let's attach the claims that we have from the token and userinfo

		// subject
		Set<Claim> claimsSupplied = Sets.newHashSet(ticket.getClaimsSupplied());

		if (auth != null) {
		
			String issuer = auth.getIssuer();
			UserInfo userInfo = auth.getUserInfo();
	
			claimsSupplied.add(mkClaim(issuer, "sub", new JsonPrimitive(auth.getSub())));
			if (userInfo.getEmail() != null) {
				claimsSupplied.add(mkClaim(issuer, "email", new JsonPrimitive(userInfo.getEmail())));
			}
			if (userInfo.getEmailVerified() != null) {
				claimsSupplied.add(mkClaim(issuer, "email_verified", new JsonPrimitive(userInfo.getEmailVerified())));
			}
			if (userInfo.getPhoneNumber() != null) {
				claimsSupplied.add(mkClaim(issuer, "phone_number", new JsonPrimitive(auth.getUserInfo().getPhoneNumber())));
			}
			if (userInfo.getPhoneNumberVerified() != null) {
				claimsSupplied.add(mkClaim(issuer, "phone_number_verified", new JsonPrimitive(auth.getUserInfo().getPhoneNumberVerified())));
			}
			if (userInfo.getPreferredUsername() != null) {
				claimsSupplied.add(mkClaim(issuer, "preferred_username", new JsonPrimitive(auth.getUserInfo().getPreferredUsername())));
			}
			if (userInfo.getProfile() != null) {
				claimsSupplied.add(mkClaim(issuer, "profile", new JsonPrimitive(auth.getUserInfo().getProfile())));
			}
	
			ticket.setClaimsSupplied(claimsSupplied);

			ticket = permissionService.updateTicket(ticket);
			
			session.setAttribute(TICKET, ticket);
		}
		
		ResourceSet resourceSet = ticket.getPermission().getResourceSet();
		
		m.addAttribute("claims", ticket.getClaimsSupplied());
		m.addAttribute("resourceSet", resourceSet);
		m.addAttribute("client", client);
		m.addAttribute("claimsResult", claimsProcessingService.claimsAreSatisfied(resourceSet, ticket));
		
		return "claims_collection";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String returnToClient(Model m, OIDCAuthenticationToken auth, HttpSession session) {
		
		ClientDetailsEntity client = (ClientDetailsEntity) session.getAttribute(CLIENT);
		PermissionTicket ticket = (PermissionTicket) session.getAttribute(TICKET);
		String redirectUri = (String) session.getAttribute(REDIRECT_URI);
		String state = (String) session.getAttribute(STATE);
		
		if (Strings.isNullOrEmpty(redirectUri)) {
			if (client.getClaimsRedirectUris().size() == 1) {
				redirectUri = client.getClaimsRedirectUris().iterator().next(); // get the first (and only) redirect URI to use here
				logger.info("No redirect URI passed in, using registered value: " + redirectUri);
			} else {
				throw new RedirectMismatchException("Unable to find redirect URI and none passed in.");
			}
		} else {
			if (!client.getClaimsRedirectUris().contains(redirectUri)) {
				throw new RedirectMismatchException("Claims redirect did not match the registered values.");
			}
		}

		UriComponentsBuilder template = UriComponentsBuilder.fromUriString(redirectUri);
		template.queryParam("authorization_state", "claims_submitted");
		template.queryParam("ticket", ticket.getTicket());
		if (!Strings.isNullOrEmpty(state)) {
			template.queryParam("state", state);
		}

		String uriString = template.toUriString();
		logger.info("Redirecting to " + uriString);

		return "redirect:" + uriString;
	}


	private Claim mkClaim(String issuer, String name, JsonElement value) {
		Claim c = new Claim();
		c.setIssuer(Sets.newHashSet(issuer));
		c.setName(name);
		c.setValue(value);
		return c;
	}

}
