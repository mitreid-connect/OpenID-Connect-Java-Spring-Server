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
package org.mitre.openid.connect.token;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Custom User Approval Handler implementation which uses a concept of a whitelist,
 * blacklist, and greylist.
 * 
 * Blacklisted sites will be caught and handled before this
 * point.
 * 
 * Whitelisted sites will be automatically approved, and an ApprovedSite entry will
 * be created for the site the first time a given user access it.
 * 
 * All other sites fall into the greylist - the user will be presented with the user
 * approval page upon their first visit
 * @author aanganes
 *
 */
@Component("tofuUserApprovalHandler")
public class TofuUserApprovalHandler implements UserApprovalHandler {

	@Autowired
	private ApprovedSiteService approvedSiteService;

	@Autowired
	private WhitelistedSiteService whitelistedSiteService;

	@Autowired
	private ClientDetailsService clientDetailsService;


	/**
	 * Check if the user has already stored a positive approval decision for this site; or if the
	 * site is whitelisted, approve it automatically.
	 * 
	 * Otherwise, return false so that the user will see the approval page and can make their own decision.
	 * 
	 * @param authorizationRequest	the incoming authorization request
	 * @param userAuthentication	the Principal representing the currently-logged-in user
	 * 
	 * @return 						true if the site is approved, false otherwise
	 */
	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {

		// if this request is already approved, pass that info through
		// (this flag may be set by updateBeforeApproval, which can also do funny things with scopes, etc)
		if (authorizationRequest.isApproved()) {
			return true;
		} else {
			// if not, check to see if the user has approved it

			// TODO: make parameter name configurable?
			boolean approved = Boolean.parseBoolean(authorizationRequest.getApprovalParameters().get("user_oauth_approval"));

			boolean csrfApproved = false;
		    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		    HttpSession session = attr.getRequest().getSession(false);
		    if (session != null) {
		    	String csrf = (String) session.getAttribute("csrf");
		    	if (csrf != null && csrf.equals(authorizationRequest.getApprovalParameters().get("csrf"))) {
		    		csrfApproved = true;
		    	}
		    }

			return userAuthentication.isAuthenticated() && approved && csrfApproved;
		}

	}

	/**
	 * Check whether the requested scope set is a proper subset of the allowed scopes.
	 * 
	 * @param requestedScopes
	 * @param allowedScopes
	 * @return
	 */
	private boolean scopesMatch(Set<String> requestedScopes, Set<String> allowedScopes) {

		for (String scope : requestedScopes) {

			if (!allowedScopes.contains(scope)) {
				return false; //throw new InvalidScopeException("Invalid scope: " + scope, allowedScopes);
			}
		}

		return true;
	}

	/**
	 * Pre-process the authorization request during the approval stage, check against whitelist, approved sites, and stuff.
	 */
	@Override
	public AuthorizationRequest updateBeforeApproval(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
		//First, check database to see if the user identified by the userAuthentication has stored an approval decision

		String userId = userAuthentication.getName();
		String clientId = authorizationRequest.getClientId();
		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);

		// find out if we're supposed to force a prompt on the user or not
		String prompt = authorizationRequest.getAuthorizationParameters().get("prompt");
		if (!"consent".equals(prompt)) {
			// if the prompt parameter is set to "consent" then we can't use approved sites or whitelisted sites
			// otherwise, we need to check them below


			//lookup ApprovedSites by userId and clientId
			Collection<ApprovedSite> aps = approvedSiteService.getByClientIdAndUserId(clientId, userId);
			for (ApprovedSite ap : aps) {

				if (!ap.isExpired()) {

					// if we find one that fits...
					if (scopesMatch(authorizationRequest.getScope(), ap.getAllowedScopes())) {

						//We have a match; update the access date on the AP entry and return true.
						ap.setAccessDate(new Date());
						approvedSiteService.save(ap);

						// TODO: WHY DAVE WHY
						DefaultAuthorizationRequest ar = new DefaultAuthorizationRequest(authorizationRequest);
						ar.setApproved(true);

						return ar;
					}
				}
			}

			WhitelistedSite ws = whitelistedSiteService.getByClientId(clientId);
			if (ws != null && scopesMatch(authorizationRequest.getScope(), ws.getAllowedScopes())) {

				//Create an approved site
				approvedSiteService.createApprovedSite(clientId, userId, null, ws.getAllowedScopes(), ws);

				// TODO: WHY DAVE WHY
				DefaultAuthorizationRequest ar = new DefaultAuthorizationRequest(authorizationRequest);
				ar.setApproved(true);

				return ar;
			}
		}

		// This must be re-parsed here because SECOAUTH forces us to call things in a strange order
		boolean approved = Boolean.parseBoolean(authorizationRequest.getApprovalParameters().get("user_oauth_approval"));

		if (approved && !authorizationRequest.getApprovalParameters().isEmpty()) {

			// get the session so we can store a CSRF protection value in it
			boolean csrfApproved = false;
		    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		    HttpSession session = attr.getRequest().getSession(false);
		    if (session != null) {
		    	String csrf = (String) session.getAttribute("csrf");
		    	if (csrf != null && csrf.equals(authorizationRequest.getApprovalParameters().get("csrf"))) {
		    		csrfApproved = true;
		    	}
		    }

		    if (csrfApproved) {
			
				// TODO: Get SECOAUTH to stop breaking polymorphism and start using real objects, SRSLY
				DefaultAuthorizationRequest ar = new DefaultAuthorizationRequest(authorizationRequest);
	
				// process scopes from user input
				Set<String> allowedScopes = Sets.newHashSet();
				Map<String,String> approvalParams = ar.getApprovalParameters();
	
				Set<String> keys = approvalParams.keySet();
	
				for (String key : keys) {
					if (key.startsWith("scope_")) {
						//This is a scope parameter from the approval page. The value sent back should
						//be the scope string. Check to make sure it is contained in the client's
						//registered allowed scopes.
	
						String scope = approvalParams.get(key);
	
						//Make sure this scope is allowed for the given client
						if (client.getScope().contains(scope)) {
							allowedScopes.add(scope);
						}
					}
				}
	
				// inject the user-allowed scopes into the auth request
				ar.setScope(allowedScopes);
	
				//Only store an ApprovedSite if the user has checked "remember this decision":
				String remember = ar.getApprovalParameters().get("remember");
				if (!Strings.isNullOrEmpty(remember) && !remember.equals("none")) {
	
					Date timeout = null;
					if (remember.equals("one-hour")) {
						// set the timeout to one hour from now
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.HOUR, 1);
						timeout = cal.getTime();
					}
	
					approvedSiteService.createApprovedSite(clientId, userId, timeout, allowedScopes, null);
				}
	
				return ar;
		    } else {
		    	// csrf didn't match, it's not approved, pass through
		    	return authorizationRequest;
		    }
		}

		return authorizationRequest;
	}

}
