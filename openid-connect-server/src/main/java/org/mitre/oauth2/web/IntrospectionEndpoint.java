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
package org.mitre.oauth2.web;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.IntrospectionAuthorizer;
import org.mitre.oauth2.service.IntrospectionResultAssembler;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@Controller
public class IntrospectionEndpoint {

	@Autowired
	private OAuth2TokenEntityService tokenServices;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private IntrospectionAuthorizer introspectionAuthorizer;

    @Autowired
    private IntrospectionResultAssembler introspectionResultAssembler;

	@Autowired
	private UserInfoService userInfoService;

	private static Logger logger = LoggerFactory.getLogger(IntrospectionEndpoint.class);

	public IntrospectionEndpoint() {

	}

	public IntrospectionEndpoint(OAuth2TokenEntityService tokenServices) {
		this.tokenServices = tokenServices;
	}

	@PreAuthorize("hasRole('ROLE_CLIENT')")
	@RequestMapping("/introspect")
	public String verify(@RequestParam("token") String tokenValue,
			@RequestParam(value = "resource_id", required = false) String resourceId,
			@RequestParam(value = "token_type_hint", required = false) String tokenType,
			Principal p, Model model) {

		if (Strings.isNullOrEmpty(tokenValue)) {
			logger.error("Verify failed; token value is null");
			Map<String,Boolean> entity = ImmutableMap.of("active", Boolean.FALSE);
			model.addAttribute("entity", entity);
			return JsonEntityView.VIEWNAME;
		}

        OAuth2AccessTokenEntity accessToken = null;
        OAuth2RefreshTokenEntity refreshToken = null;
		ClientDetailsEntity tokenClient;
		Set<String> scopes;
		UserInfo user;

		try {

			// check access tokens first (includes ID tokens)
			accessToken = tokenServices.readAccessToken(tokenValue);

			tokenClient = accessToken.getClient();
			scopes = accessToken.getScope();

            user = userInfoService.getByUsernameAndClientId(accessToken.getAuthenticationHolder().getAuthentication().getName(), tokenClient.getClientId());

		} catch (InvalidTokenException e) {
			logger.info("Verify failed; Invalid access token. Checking refresh token.");
			try {

				// check refresh tokens next
				refreshToken = tokenServices.getRefreshToken(tokenValue);

				tokenClient = refreshToken.getClient();
				scopes = refreshToken.getAuthenticationHolder().getAuthentication().getOAuth2Request().getScope();

				user = userInfoService.getByUsernameAndClientId(refreshToken.getAuthenticationHolder().getAuthentication().getName(), tokenClient.getClientId());

			} catch (InvalidTokenException e2) {
				logger.error("Verify failed; Invalid access/refresh token", e2);
				Map<String,Boolean> entity = ImmutableMap.of("active", Boolean.FALSE);
				model.addAttribute("entity", entity);
				return JsonEntityView.VIEWNAME;
			}
		}

        // clientID is the principal name in the authentication
        String clientId = p.getName();
        ClientDetailsEntity authClient = clientService.loadClientByClientId(clientId);

        if (authClient.isAllowIntrospection()) {
            if (introspectionAuthorizer.isIntrospectionPermitted(authClient, tokenClient, scopes)) {
                // if it's a valid token, we'll print out information on it
                Map<String, Object> entity = accessToken != null
                        ? introspectionResultAssembler.assembleFrom(accessToken, user)
                        : introspectionResultAssembler.assembleFrom(refreshToken, user);
                model.addAttribute("entity", entity);
                return JsonEntityView.VIEWNAME;
            } else {
                logger.error("Verify failed; client configuration or scope don't permit token introspection");
                model.addAttribute("code", HttpStatus.FORBIDDEN);
                return HttpCodeView.VIEWNAME;
            }
        } else {
            logger.error("Verify failed; client " + clientId + " is not allowed to call introspection endpoint");
            model.addAttribute("code", HttpStatus.FORBIDDEN);
            return HttpCodeView.VIEWNAME;
        }

	}

}
