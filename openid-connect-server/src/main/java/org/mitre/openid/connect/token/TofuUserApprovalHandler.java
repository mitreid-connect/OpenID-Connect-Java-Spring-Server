/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.mitre.openid.connect.web.AuthenticationTimeStamper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Splitter;
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

	@Autowired
	private SystemScopeService systemScopes;

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
			if (Boolean.parseBoolean(authorizationRequest.getApprovalParameters().get("user_oauth_approval"))) {			// TODO: make parameter name configurable?

				// check the value of the CSRF parameter

				if (authorizationRequest.getExtensions().get("csrf") != null) {
					if (authorizationRequest.getExtensions().get("csrf").equals(authorizationRequest.getApprovalParameters().get("csrf"))) {

						// make sure the user is actually authenticated
						return userAuthentication.isAuthenticated();
					}
				}
			}

			// if the above doesn't pass, it's not yet approved
			return false;
		}

	}

	/**
	 * Check if the user has already stored a positive approval decision for this site; or if the
	 * site is whitelisted, approve it automatically.
	 * 
	 * Otherwise the user will be directed to the approval page and can make their own decision.
	 * 
	 * @param authorizationRequest	the incoming authorization request
	 * @param userAuthentication	the Principal representing the currently-logged-in user
	 * 
	 * @return 						the updated AuthorizationRequest
	 */
	@Override
	public AuthorizationRequest checkForPreApproval(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {

		//First, check database to see if the user identified by the userAuthentication has stored an approval decision

		String userId = userAuthentication.getName();
		String clientId = authorizationRequest.getClientId();

		//lookup ApprovedSites by userId and clientId
		boolean alreadyApproved = false;

		// find out if we're supposed to force a prompt on the user or not
		String prompt = (String) authorizationRequest.getExtensions().get("prompt");
		List<String> prompts = Splitter.on(" ").splitToList(Strings.nullToEmpty(prompt));
		if (!prompts.contains("consent")) {
			// if the prompt parameter is set to "consent" then we can't use approved sites or whitelisted sites
			// otherwise, we need to check them below

			Collection<ApprovedSite> aps = approvedSiteService.getByClientIdAndUserId(clientId, userId);
			for (ApprovedSite ap : aps) {

				if (!ap.isExpired()) {

					// if we find one that fits...
					if (systemScopes.scopesMatch(ap.getAllowedScopes(), authorizationRequest.getScope())) {

						//We have a match; update the access date on the AP entry and return true.
						ap.setAccessDate(new Date());
						approvedSiteService.save(ap);

						authorizationRequest.getExtensions().put("approved_site", ap.getId());
						authorizationRequest.setApproved(true);
						alreadyApproved = true;

						setAuthTime(authorizationRequest);
					}
				}
			}

			if (!alreadyApproved) {
				WhitelistedSite ws = whitelistedSiteService.getByClientId(clientId);
				if (ws != null && systemScopes.scopesMatch(ws.getAllowedScopes(), authorizationRequest.getScope())) {

					//Create an approved site
					ApprovedSite newSite = approvedSiteService.createApprovedSite(clientId, userId, null, ws.getAllowedScopes(), ws);
					authorizationRequest.getExtensions().put("approved_site", newSite.getId());
					authorizationRequest.setApproved(true);

					setAuthTime(authorizationRequest);
				}
			}
		}

		return authorizationRequest;

	}


	@Override
	public AuthorizationRequest updateAfterApproval(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {

		String userId = userAuthentication.getName();
		String clientId = authorizationRequest.getClientId();
		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);

		// This must be re-parsed here because SECOAUTH forces us to call things in a strange order
		if (Boolean.parseBoolean(authorizationRequest.getApprovalParameters().get("user_oauth_approval"))
				&& authorizationRequest.getExtensions().get("csrf") != null
				&& authorizationRequest.getExtensions().get("csrf").equals(authorizationRequest.getApprovalParameters().get("csrf"))) {

			authorizationRequest.setApproved(true);

			// process scopes from user input
			Set<String> allowedScopes = Sets.newHashSet();
			Map<String,String> approvalParams = authorizationRequest.getApprovalParameters();

			Set<String> keys = approvalParams.keySet();

			for (String key : keys) {
				if (key.startsWith("scope_")) {
					//This is a scope parameter from the approval page. The value sent back should
					//be the scope string. Check to make sure it is contained in the client's
					//registered allowed scopes.

					String scope = approvalParams.get(key);
					Set<String> approveSet = Sets.newHashSet(scope);

					//Make sure this scope is allowed for the given client
					if (systemScopes.scopesMatch(client.getScope(), approveSet)) {

						// If it's structured, assign the user-specified parameter
						SystemScope systemScope = systemScopes.getByValue(scope);
						if (systemScope != null && systemScope.isStructured()){
							String paramValue = approvalParams.get("scopeparam_" + scope);
							allowedScopes.add(scope + ":"+paramValue);
							// .. and if it's unstructured, we're all set
						} else {
							allowedScopes.add(scope);
						}
					}

				}
			}

			// inject the user-allowed scopes into the auth request
			authorizationRequest.setScope(allowedScopes);

			//Only store an ApprovedSite if the user has checked "remember this decision":
			String remember = authorizationRequest.getApprovalParameters().get("remember");
			if (!Strings.isNullOrEmpty(remember) && !remember.equals("none")) {

				Date timeout = null;
				if (remember.equals("one-hour")) {
					// set the timeout to one hour from now
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.HOUR, 1);
					timeout = cal.getTime();
				}

				ApprovedSite newSite = approvedSiteService.createApprovedSite(clientId, userId, timeout, allowedScopes, null);
				authorizationRequest.getExtensions().put("approved_site", newSite.getId());
			}

			setAuthTime(authorizationRequest);


		}

		return authorizationRequest;
	}

	/**
	 * Get the auth time out of the current session and add it to the
	 * auth request in the extensions map.
	 * 
	 * @param authorizationRequest
	 */
	private void setAuthTime(AuthorizationRequest authorizationRequest) {
		// Get the session auth time, if we have it, and store it in the request
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		if (attr != null) {
			HttpSession session = attr.getRequest().getSession();
			if (session != null) {
				Date authTime = (Date) session.getAttribute(AuthenticationTimeStamper.AUTH_TIMESTAMP);
				if (authTime != null) {
					authorizationRequest.getExtensions().put(AuthenticationTimeStamper.AUTH_TIMESTAMP, authTime);
				}
			}
		}
	}

	@Override
	public Map<String, Object> getUserApprovalRequest(
			AuthorizationRequest authorizationRequest,
			Authentication userAuthentication) {
		Map<String, Object> model = new HashMap<String, Object>();
		// In case of a redirect we might want the request parameters to be included
		model.putAll(authorizationRequest.getRequestParameters());
		return model;

	}

}
