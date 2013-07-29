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
package org.mitre.oauth2.web;

import java.security.Principal;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RevocationEndpoint {
	@Autowired
	OAuth2TokenEntityService tokenServices;

	private static Logger logger = LoggerFactory.getLogger(RevocationEndpoint.class);

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
	@RequestMapping("/revoke")
	public String revoke(@RequestParam("token") String tokenValue, @RequestParam(value = "token_type_hint", required = false) String tokenType, Principal principal, Model model) {

		// This is the token as passed in from OAuth (in case we need it some day)
		//OAuth2AccessTokenEntity tok = tokenServices.getAccessToken((OAuth2Authentication) principal);

		OAuth2Request authRequest = null;
		if (principal instanceof OAuth2Authentication) {
			// if the client is acting on its own behalf (the common case), pull out the client authorization request
			authRequest = ((OAuth2Authentication) principal).getOAuth2Request();
		}

		try {
			// check and handle access tokens first

			OAuth2AccessTokenEntity accessToken = tokenServices.readAccessToken(tokenValue);
			if (authRequest != null) {
				// client acting on its own, make sure it owns the token
				if (!accessToken.getClient().getClientId().equals(authRequest.getClientId())) {
					// trying to revoke a token we don't own, throw a 403
					model.addAttribute("code", HttpStatus.FORBIDDEN);
					return "httpCodeView";
				}
			}

			// if we got this far, we're allowed to do this
			tokenServices.revokeAccessToken(accessToken);
			model.addAttribute("code", HttpStatus.OK);
			return "httpCodeView";

		} catch (InvalidTokenException e) {

			// access token wasn't found, check the refresh token

			try {
				OAuth2RefreshTokenEntity refreshToken = tokenServices.getRefreshToken(tokenValue);
				if (authRequest != null) {
					// client acting on its own, make sure it owns the token
					if (!refreshToken.getClient().getClientId().equals(authRequest.getClientId())) {
						// trying to revoke a token we don't own, throw a 403
						model.addAttribute("code", HttpStatus.FORBIDDEN);
						return "httpCodeView";
					}
				}

				// if we got this far, we're allowed to do this
				tokenServices.revokeRefreshToken(refreshToken);
				model.addAttribute("code", HttpStatus.OK);
				return "httpCodeView";

			} catch (InvalidTokenException e1) {

				// neither token type was found, simply say "OK" and be on our way.

				model.addAttribute("code", HttpStatus.OK);
				return "httpCodeView";
			}
		}
	}

}
