/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
package org.mitre.oauth2.web;

import static org.mitre.oauth2.web.AuthenticationUtilities.ensureOAuthScope;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RevocationEndpoint {
	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private OAuth2TokenEntityService tokenServices;

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(RevocationEndpoint.class);

	public static final String URL = "revoke";

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
	@RequestMapping("/" + URL)
	public String revoke(@RequestParam("token") String tokenValue, @RequestParam(value = "token_type_hint", required = false) String tokenType, Authentication auth, Model model) {

		// This is the token as passed in from OAuth (in case we need it some day)
		//OAuth2AccessTokenEntity tok = tokenServices.getAccessToken((OAuth2Authentication) principal);

		ClientDetailsEntity authClient = null;

		if (auth instanceof OAuth2Authentication) {
			// the client authenticated with OAuth, do our UMA checks
			ensureOAuthScope(auth, SystemScopeService.UMA_PROTECTION_SCOPE);
			// get out the client that was issued the access token (not the token being revoked)
			OAuth2Authentication o2a = (OAuth2Authentication) auth;

			String authClientId = o2a.getOAuth2Request().getClientId();
			authClient = clientService.loadClientByClientId(authClientId);

			// the owner is the user who authorized the token in the first place
			String ownerId = o2a.getUserAuthentication().getName();

		} else {
			// the client authenticated directly, make sure it's got the right access

			String authClientId = auth.getName(); // direct authentication puts the client_id into the authentication's name field
			authClient = clientService.loadClientByClientId(authClientId);

		}

		try {
			// check and handle access tokens first

			OAuth2AccessTokenEntity accessToken = tokenServices.readAccessToken(tokenValue);

			// client acting on its own, make sure it owns the token
			if (!accessToken.getClient().getClientId().equals(authClient.getClientId())) {
				// trying to revoke a token we don't own, throw a 403

				logger.info("Client " + authClient.getClientId() + " tried to revoke a token owned by " + accessToken.getClient().getClientId());

				model.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
				return HttpCodeView.VIEWNAME;
			}

			// if we got this far, we're allowed to do this
			tokenServices.revokeAccessToken(accessToken);

			logger.debug("Client " + authClient.getClientId() + " revoked access token " + tokenValue);

			model.addAttribute(HttpCodeView.CODE, HttpStatus.OK);
			return HttpCodeView.VIEWNAME;

		} catch (InvalidTokenException e) {

			// access token wasn't found, check the refresh token

			try {
				OAuth2RefreshTokenEntity refreshToken = tokenServices.getRefreshToken(tokenValue);
				// client acting on its own, make sure it owns the token
				if (!refreshToken.getClient().getClientId().equals(authClient.getClientId())) {
					// trying to revoke a token we don't own, throw a 403

					logger.info("Client " + authClient.getClientId() + " tried to revoke a token owned by " + refreshToken.getClient().getClientId());

					model.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
					return HttpCodeView.VIEWNAME;
				}

				// if we got this far, we're allowed to do this
				tokenServices.revokeRefreshToken(refreshToken);

				logger.debug("Client " + authClient.getClientId() + " revoked access token " + tokenValue);

				model.addAttribute(HttpCodeView.CODE, HttpStatus.OK);
				return HttpCodeView.VIEWNAME;

			} catch (InvalidTokenException e1) {

				// neither token type was found, simply say "OK" and be on our way.

				logger.debug("Failed to revoke token " + tokenValue);

				model.addAttribute(HttpCodeView.CODE, HttpStatus.OK);
				return HttpCodeView.VIEWNAME;
			}
		}
	}

}
