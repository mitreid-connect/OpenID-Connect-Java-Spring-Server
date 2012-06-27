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

import java.util.Date;
import java.util.Collection;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.UserInfo;
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
		
		//Check database to see if the user identified by the userAuthentication has stored an approval decision
		String userId = userAuthentication.getName();
		
		ClientDetails client = clientDetailsService.loadClientByClientId(authorizationRequest.getClientId());
		
		//lookup ApprovedSites by userId
		UserInfo user = userInfoService.getByUserId(userId);
		
		Collection<ApprovedSite> approvedSites = approvedSiteService.getByUserInfo(user);
		
		for (ApprovedSite ap : approvedSites) {
			if (ap.getClientDetails().getClientId() == client.getClientId()) {
				//TODO need to test more than just id
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

		return false;
	}

}
