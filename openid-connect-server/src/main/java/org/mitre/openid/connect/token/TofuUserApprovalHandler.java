/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;

import com.google.common.base.Splitter;
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
public class TofuUserApprovalHandler implements UserApprovalHandler {
	
	@Autowired
	ApprovedSiteService approvedSiteService;
	
	@Autowired
	WhitelistedSiteService whitelistedSiteService;
	
	@Autowired
	ClientDetailsService clientDetailsService;
	
	
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
		
		//First, check database to see if the user identified by the userAuthentication has stored an approval decision
		
		//getName may not be filled in? TODO: investigate
		String userId = userAuthentication.getName();
		String clientId = authorizationRequest.getClientId();
		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
		
		String scopes = authorizationRequest.getAuthorizationParameters().get("scope");
		Set<String> authRequestScopes = Sets.newHashSet(Splitter.on(" ").split(scopes));
		
		//lookup ApprovedSites by userId and clientId
		Collection<ApprovedSite> aps = approvedSiteService.getByClientIdAndUserId(clientId, userId);
		for (ApprovedSite ap : aps) {
			// if we find one that fits...
			if (scopesMatch(authRequestScopes, ap.getAllowedScopes())) {
				
				//We have a match; update the access date on the AP entry and return true.
				ap.setAccessDate(new Date());
				approvedSiteService.save(ap);
				
				return true;
			}
        }
		
		WhitelistedSite ws = whitelistedSiteService.getByClientId(clientId);
		if (ws != null && scopesMatch(authRequestScopes, ws.getAllowedScopes())) {
			
			//Create an approved site
			approvedSiteService.createApprovedSite(clientId, userId, null, ws.getAllowedScopes(), ws);
			
			return true;
		}
		
		boolean approved = Boolean.parseBoolean(authorizationRequest.getApprovalParameters().get("user_oauth_approval"));
		
		if (approved && !authorizationRequest.getApprovalParameters().isEmpty()) {
			
			//Only store an ApprovedSite if the user has checked "remember this decision":
			if (authorizationRequest.getApprovalParameters().get("remember") != null) {
				
				//TODO: Remember may eventually have an option to remember for a specific amount
				//of time; this would set the ApprovedSite.timeout.
				
				Set<String> allowedScopes = Sets.newHashSet();
				Map<String,String> approvalParams = authorizationRequest.getApprovalParameters();
				
				Set<String> keys = approvalParams.keySet();
				
				for (String key : keys) {
					if (key.contains("scope")) {
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
				
				//FIXME: inject the final allowedScopes set into the AuthorizationRequest. The requester may have
				//asked for many scopes and the user may have denied some of them.
				
				approvedSiteService.createApprovedSite(clientId, userId, null, allowedScopes, null);
			}
			
			return true;
		}

		return false;
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

	//
	// FIXME
	// 
    @Override
    public AuthorizationRequest updateBeforeApproval(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
	    // TODO Auto-generated method stub
	    return null;
    }
	
}
