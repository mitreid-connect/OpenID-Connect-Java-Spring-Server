/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/
package org.mitre.openid.connect.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * OpenID Connect EndSession endpoint, as specified in https://openid.net/specs/openid-connect-session-1_0.html
 * 
 * @author SDOXSEE
 *
 */
@Controller
@PreAuthorize("hasRole('ROLE_USER')")
public class EndSessionEndpoint {

	public static final String URL = "endsession";
	
	@Autowired
	EndSessionValidator endSessionValidator;

	@Autowired
	OAuth2TokenEntityService tokenService;
	
	@Autowired
	SignOutHelper signOutHelper;
	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(EndSessionEndpoint.class);

	/**
	 * Get information about the user as specified in the accessToken included in this request
	 */
	@RequestMapping(value="/" + EndSessionEndpoint.URL, method= {RequestMethod.GET})
	public String getEndSession(
			@RequestParam(value="id_token_hint", required=false) String idTokenHint,
			@RequestParam(value="post_logout_redirect_uri", required=false, defaultValue="") String postLogoutRedirectUri,
			AbstractAuthenticationToken auth, Model model, final RedirectAttributes redirectAttributes, HttpServletRequest request) {

		if (endSessionValidator.isValid(idTokenHint, postLogoutRedirectUri, auth)) {
			try {
				OAuth2AccessTokenEntity accessToken = tokenService.readAccessToken(idTokenHint);
				tokenService.revokeAccessToken(accessToken);
			} catch (Exception e) {
				logger.warn("Couldn't revoke valid id_token: " + idTokenHint);
				// if we can't revoke the token, not the end of the world. Carry on
			}
			signOutHelper.signOutProgrammatically(request);
			return "redirect:" + postLogoutRedirectUri;
		} else {
			redirectAttributes.addFlashAttribute("id_token_hint", idTokenHint);
			redirectAttributes.addFlashAttribute("post_logout_redirect_uri", postLogoutRedirectUri);
			
			return "redirect:/logout";
		}
	}

	@RequestMapping(value="/logout", method= {RequestMethod.GET})
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		return new ModelAndView("logout");
	}
	
}
