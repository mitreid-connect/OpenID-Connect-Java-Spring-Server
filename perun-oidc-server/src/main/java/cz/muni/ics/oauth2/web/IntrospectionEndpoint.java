/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.oauth2.web;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.IntrospectionResultAssembler;
import cz.muni.ics.oauth2.service.OAuth2TokenEntityService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.UserInfoService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import cz.muni.ics.uma.model.ResourceSet;
import cz.muni.ics.uma.service.ResourceSetService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class IntrospectionEndpoint {

	/**
	 *
	 */
	public static final String URL = "introspect";

	@Autowired
	private OAuth2TokenEntityService tokenServices;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private IntrospectionResultAssembler introspectionResultAssembler;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private ResourceSetService resourceSetService;

	public IntrospectionEndpoint() {

	}

	public IntrospectionEndpoint(OAuth2TokenEntityService tokenServices) {
		this.tokenServices = tokenServices;
	}

	@RequestMapping("/" + URL)
	public String verify(@RequestParam("token") String tokenValue,
			@RequestParam(value = "token_type_hint", required = false) String tokenType,
			Authentication auth, Model model) {

		ClientDetailsEntity authClient = null;
		Set<String> authScopes = new HashSet<>();

		if (auth instanceof OAuth2Authentication) {
			// the client authenticated with OAuth, do our UMA checks
			AuthenticationUtilities.ensureOAuthScope(auth, SystemScopeService.UMA_PROTECTION_SCOPE);

			// get out the client that was issued the access token (not the token being introspected)
			OAuth2Authentication o2a = (OAuth2Authentication) auth;

			String authClientId = o2a.getOAuth2Request().getClientId();
			authClient = clientService.loadClientByClientId(authClientId);

			// the owner is the user who authorized the token in the first place
			String ownerId = o2a.getUserAuthentication().getName();

			authScopes.addAll(authClient.getScope());

			// UMA style clients also get a subset of scopes of all the resource sets they've registered
			Collection<ResourceSet> resourceSets = resourceSetService.getAllForOwnerAndClient(ownerId, authClientId);

			// collect all the scopes
			for (ResourceSet rs : resourceSets) {
				authScopes.addAll(rs.getScopes());
			}

		} else {
			// the client authenticated directly, make sure it's got the right access

			String authClientId = auth.getName(); // direct authentication puts the client_id into the authentication's name field
			authClient = clientService.loadClientByClientId(authClientId);

			// directly authenticated clients get a subset of any scopes that they've registered for
			authScopes.addAll(authClient.getScope());

			if (!AuthenticationUtilities.hasRole(auth, "ROLE_CLIENT")
					|| !authClient.isAllowIntrospection()) {

				// this client isn't allowed to do direct introspection

				log.error("Client " + authClient.getClientId() + " is not allowed to call introspection endpoint");
				model.addAttribute("code", HttpStatus.FORBIDDEN);
				return HttpCodeView.VIEWNAME;

			}

		}

		// by here we're allowed to introspect, now we need to look up the token in our token stores

		// first make sure the token is there
		if (Strings.isNullOrEmpty(tokenValue)) {
			log.error("Verify failed; token value is null");
			Map<String,Boolean> entity = ImmutableMap.of("active", Boolean.FALSE);
			model.addAttribute(JsonEntityView.ENTITY, entity);
			return JsonEntityView.VIEWNAME;
		}

		OAuth2AccessTokenEntity accessToken = null;
		OAuth2RefreshTokenEntity refreshToken = null;
		ClientDetailsEntity tokenClient;
		UserInfo user;

		try {

			// check access tokens first (includes ID tokens)
			accessToken = tokenServices.readAccessToken(tokenValue);

			tokenClient = accessToken.getClient();

			// get the user information of the user that authorized this token in the first place
			String userName = accessToken.getAuthenticationHolder().getAuthentication().getName();
			user = userInfoService.getByUsernameAndClientId(userName, tokenClient.getClientId());

		} catch (InvalidTokenException e) {
			log.info("Invalid access token. Checking refresh token.");
			try {

				// check refresh tokens next
				refreshToken = tokenServices.getRefreshToken(tokenValue);

				tokenClient = refreshToken.getClient();

				// get the user information of the user that authorized this token in the first place
				String userName = refreshToken.getAuthenticationHolder().getAuthentication().getName();
				user = userInfoService.getByUsernameAndClientId(userName, tokenClient.getClientId());

			} catch (InvalidTokenException e2) {
				log.error("Invalid refresh token");
				Map<String,Boolean> entity = ImmutableMap.of(IntrospectionResultAssembler.ACTIVE, Boolean.FALSE);
				model.addAttribute(JsonEntityView.ENTITY, entity);
				return JsonEntityView.VIEWNAME;
			}
		}

		// if it's a valid token, we'll print out information on it

		if (accessToken != null) {
			Map<String, Object> entity = introspectionResultAssembler.assembleFrom(accessToken, user, authScopes);
			model.addAttribute(JsonEntityView.ENTITY, entity);
		} else if (refreshToken != null) {
			Map<String, Object> entity = introspectionResultAssembler.assembleFrom(refreshToken, user, authScopes);
			model.addAttribute(JsonEntityView.ENTITY, entity);
		} else {
			// no tokens were found (we shouldn't get here)
			log.error("Verify failed; Invalid access/refresh token");
			Map<String,Boolean> entity = ImmutableMap.of(IntrospectionResultAssembler.ACTIVE, Boolean.FALSE);
			model.addAttribute(JsonEntityView.ENTITY, entity);
			return JsonEntityView.VIEWNAME;
		}

		return JsonEntityView.VIEWNAME;

	}

}
