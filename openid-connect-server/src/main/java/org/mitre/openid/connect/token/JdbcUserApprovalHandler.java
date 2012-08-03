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
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.UserInfoService;
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
public class JdbcUserApprovalHandler implements UserApprovalHandler {

	@Autowired
	UserInfoService userInfoService;
	
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
	 * @return 						true if the site is pre-approved, false otherwise
	 */
	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
		
		//First, check database to see if the user identified by the userAuthentication has stored an approval decision
		
		String userId = userAuthentication.getName();
		ClientDetails client = clientDetailsService.loadClientByClientId(authorizationRequest.getClientId());
		DefaultUserInfo user = (DefaultUserInfo)userInfoService.getByUserId(userId);
		
		//lookup ApprovedSites by userId		
		Collection<ApprovedSite> approvedSites = approvedSiteService.getByUserInfo(user);
		
		for (ApprovedSite ap : approvedSites) {
			if (sitesMatch(ap, authorizationRequest, user)) {
				
				//We have a match; update the access date on the AP entry and return true.
				ap.setAccessDate(new Date());
				approvedSiteService.save(ap);
				
				return true;
			}
		}
		
		WhitelistedSite ws = whitelistedSiteService.getByClientDetails(client);
		if (ws != null) {
			//Create an approved site
			ApprovedSite newAP = new ApprovedSite();
			newAP.setAccessDate(new Date());
			newAP.setWhitelistedSite(ws);
			newAP.setAllowedScopes(ws.getAllowedScopes());
			newAP.setCreationDate(new Date());
			newAP.setUserInfo(user);
			//TODO set timeout date?
			approvedSiteService.save(newAP);
			
			return true;
		}
		
		boolean approved = Boolean.parseBoolean(authorizationRequest.getApprovalParameters().get("user_oauth_approval"));
		
		if (approved && !authorizationRequest.getApprovalParameters().isEmpty()) {
			
			//TODO: check approval parameters to see if we should store this request or not
			
			//Make a new AP
			ApprovedSite newAP = new ApprovedSite();
			newAP.setAccessDate(new Date());
			String scopes = authorizationRequest.getAuthorizationParameters().get("scope");
			Set<String> allowedScopes = Sets.newHashSet(Splitter.on(" ").split(scopes));
			newAP.setAllowedScopes(allowedScopes);
			newAP.setClientDetails((ClientDetailsEntity)client);
			newAP.setUserInfo((DefaultUserInfo)user);
			newAP.setCreationDate(new Date());
			approvedSiteService.save(newAP);
			
			return true;
		}

		return false;
	}
	
	/**
	 * Check if a given ApprovedSite entry matches the information about the current request.
	 * 
	 * @param ap		the ApprovedSite to compare
	 * @param authReq	the AuthorizationRequest for this requst
	 * @param user		the User making the request
	 * @return			true if everything matches, false otherwise
	 */
	private boolean sitesMatch(ApprovedSite ap, AuthorizationRequest authReq, DefaultUserInfo user) {
		
		ClientDetails client = clientDetailsService.loadClientByClientId(authReq.getClientId());
		
		String scopes = authReq.getAuthorizationParameters().get("scope");
		Set<String> allowedScopes = Sets.newHashSet(Splitter.on(" ").split(scopes));
		
		if (!(ap.getClientDetails().getClientId()).equalsIgnoreCase(client.getClientId())) {
			return false;
		}
		if (!(ap.getUserInfo().getUserId()).equalsIgnoreCase(user.getUserId())) {
			return false;
		}
		for (String scope : allowedScopes) {
			if (!ap.getAllowedScopes().contains(scope)) {
				return false;
			}
		}
		
		return true;
	}

}
