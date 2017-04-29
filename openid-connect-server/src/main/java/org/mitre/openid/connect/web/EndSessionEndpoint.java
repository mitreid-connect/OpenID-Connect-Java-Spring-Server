/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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

package org.mitre.openid.connect.web;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mitre.jwt.assertion.AssertionValidator;
import org.mitre.jwt.assertion.impl.SelfAssertionValidator;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

/**
 * Implementation of the End Session Endpoint from OIDC session management
 * 
 * @author jricher
 *
 */
@Controller
public class EndSessionEndpoint {

	public static final String URL = "endsession";
	
	private static final String CLIENT_KEY = "client";
	private static final String STATE_KEY = "state";
	private static final String REDIRECT_URI_KEY = "redirectUri";

	private static Logger logger = LoggerFactory.getLogger(EndSessionEndpoint.class);
	
	@Autowired
	private SelfAssertionValidator validator;
	
	@Autowired
	private UserInfoService userInfoService;
	
	@Autowired
	private ClientDetailsEntityService clientService;
	
	@RequestMapping(value = "/" + URL, method = RequestMethod.GET)
	public String endSession(@RequestParam (value = "id_token_hint", required = false) String idTokenHint,  
		    @RequestParam (value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri,
		    @RequestParam (value = STATE_KEY, required = false) String state,
		    HttpServletRequest request,
		    HttpServletResponse response,
		    HttpSession session,
		    Authentication auth, Model m) {

		// conditionally filled variables
		JWTClaimsSet idTokenClaims = null; // pulled from the parsed and validated ID token
		ClientDetailsEntity client = null; // pulled from ID token's audience field
		
		if (!Strings.isNullOrEmpty(postLogoutRedirectUri)) {
			session.setAttribute(REDIRECT_URI_KEY, postLogoutRedirectUri);
		}
		if (!Strings.isNullOrEmpty(state)) {
			session.setAttribute(STATE_KEY, state);
		}
		
		// parse the ID token hint to see if it's valid
		if (!Strings.isNullOrEmpty(idTokenHint)) {
			try {
				JWT idToken = JWTParser.parse(idTokenHint);
				
				if (validator.isValid(idToken)) {
					// we issued this ID token, figure out who it's for
					idTokenClaims = idToken.getJWTClaimsSet();
					
					String clientId = Iterables.getOnlyElement(idTokenClaims.getAudience());
					
					client = clientService.loadClientByClientId(clientId);
					
					// save a reference in the session for us to pick up later
					//session.setAttribute("endSession_idTokenHint_claims", idTokenClaims);
					session.setAttribute(CLIENT_KEY, client);
				}
			} catch (ParseException e) {
				// it's not a valid ID token, ignore it
				logger.debug("Invalid id token hint", e);
			} catch (InvalidClientException e) {
				// couldn't find the client, ignore it
				logger.debug("Invalid client", e);
			}
		}
		
		// are we logged in or not?
		if (auth == null || !request.isUserInRole("ROLE_USER")) {
			// we're not logged in anyway, process the final redirect bits if needed
			return processLogout(null, request, response, session, auth, m);
		} else {
			// we are logged in, need to prompt the user before we log out
		
			// see who the current user is
			UserInfo ui = userInfoService.getByUsername(auth.getName()); 
			
			if (idTokenClaims != null) {
				String subject = idTokenClaims.getSubject();
				// see if the current user is the same as the one in the ID token
				// TODO: should we do anything different in these cases?
				if (!Strings.isNullOrEmpty(subject) && subject.equals(ui.getSub())) {
					// it's the same user
				} else { 
					// it's not the same user
				}
			}

			m.addAttribute("client", client);
			m.addAttribute("idToken", idTokenClaims);
			
			// display the log out confirmation page
			return "logoutConfirmation";
		}
	}
	
	@RequestMapping(value = "/" + URL, method = RequestMethod.POST)
	public String processLogout(@RequestParam(value = "approve", required = false) String approved,
			HttpServletRequest request,
			HttpServletResponse response,
		    HttpSession session,
		    Authentication auth, Model m) {

		String redirectUri = (String) session.getAttribute(REDIRECT_URI_KEY);
		String state = (String) session.getAttribute(STATE_KEY);
		ClientDetailsEntity client = (ClientDetailsEntity) session.getAttribute(CLIENT_KEY);
		
		if (!Strings.isNullOrEmpty(approved)) {
			// use approved, perform the logout
			if (auth != null){    
				new SecurityContextLogoutHandler().logout(request, response, auth);
			}
			SecurityContextHolder.getContext().setAuthentication(null);
			// TODO: hook into other logout post-processing
		}
		
		// if the user didn't approve, don't log out but hit the landing page anyway for redirect as needed

		
		
		// if we have a client AND the client has post-logout redirect URIs
		// registered AND the URI given is in that list, then...
		if (!Strings.isNullOrEmpty(redirectUri) && 
			client != null && client.getPostLogoutRedirectUris() != null) {
			
			if (client.getPostLogoutRedirectUris().contains(redirectUri)) {
				// TODO: future, add the redirect URI to the model for the display page for an interstitial
				// m.addAttribute("redirectUri", postLogoutRedirectUri);
				
				UriComponents uri = UriComponentsBuilder.fromHttpUrl(redirectUri).queryParam("state", state).build();
				
				return "redirect:" + uri;
			}
		}
		
		// otherwise, return to a nice post-logout landing page
		return "postLogout";
	}

}
