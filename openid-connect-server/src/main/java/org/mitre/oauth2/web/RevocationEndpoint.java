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
package org.mitre.oauth2.web;

import org.mitre.oauth2.exception.PermissionDeniedException;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RevocationEndpoint {
	@Autowired
	OAuth2TokenEntityService tokenServices;
	
	public RevocationEndpoint() {
		
	}
	
	public RevocationEndpoint(OAuth2TokenEntityService tokenServices) {
		this.tokenServices = tokenServices;
	}
	
	// TODO
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
	@RequestMapping("/oauth/revoke")
	public ModelAndView revoke(@RequestParam("token") String tokenValue, 
			ModelAndView modelAndView) {

		OAuth2RefreshTokenEntity refreshToken = tokenServices.getRefreshToken(tokenValue);
		OAuth2AccessTokenEntity accessToken = tokenServices.getAccessToken(tokenValue);

		if (refreshToken == null && accessToken == null) {
			// TODO: this should throw a 400 with a JSON error code
			throw new InvalidTokenException("Invalid OAuth token: " + tokenValue);
		}
		
		// TODO: there should be a way to do this in SPEL, right?
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof OAuth2Authentication) {
			// we've got a client acting on its own behalf, not an admin
			//ClientAuthentication clientAuth = (ClientAuthenticationToken) ((OAuth2Authentication) auth).getClientAuthentication();
			AuthorizationRequest clientAuth = ((OAuth2Authentication) auth).getAuthorizationRequest();

			if (refreshToken != null) {
				if (!refreshToken.getClient().getClientId().equals(clientAuth.getClientId())) {
					// trying to revoke a token we don't own, fail
					// TODO: this should throw a 403 
					throw new PermissionDeniedException("Client tried to revoke a token it doesn't own");
				}
			} else {
				if (!accessToken.getClient().getClientId().equals(clientAuth.getClientId())) {
					// trying to revoke a token we don't own, fail
					// TODO: this should throw a 403 
					throw new PermissionDeniedException("Client tried to revoke a token it doesn't own");
				}
			}
		}
		
		// if we got this far, we're allowed to do this
		if (refreshToken != null) {
			tokenServices.revokeRefreshToken(refreshToken);
		} else {
			tokenServices.revokeAccessToken(accessToken);
		}
		
		// TODO: throw a 200 back (no content?)
		return modelAndView;
	}

}
