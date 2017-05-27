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

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.view.TokenApiView;
import org.mitre.openid.connect.service.OIDCTokenService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.openid.connect.web.RootController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * REST-ish API for managing access tokens (GET/DELETE only)
 * @author Amanda Anganes
 *
 */
@Controller
@RequestMapping("/" + TokenAPI.URL)
@PreAuthorize("hasRole('ROLE_USER')")
public class TokenAPI {

	public static final String URL = RootController.API_URL + "/tokens";

	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private OIDCTokenService oidcTokenService;

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(TokenAPI.class);

	@RequestMapping(value = "/access", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllAccessTokens(ModelMap m, Principal p) {

		Set<OAuth2AccessTokenEntity> allTokens = tokenService.getAllAccessTokensForUser(p.getName());
		m.put(JsonEntityView.ENTITY, allTokens);
		return TokenApiView.VIEWNAME;
	}

	@RequestMapping(value = "/access/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAccessTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2AccessTokenEntity token = tokenService.getAccessTokenById(id);

		if (token == null) {
			logger.error("getToken failed; token not found: " + id);
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested token with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("getToken failed; token does not belong to principal " + p.getName());
			m.put(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			m.put(JsonErrorView.ERROR_MESSAGE, "You do not have permission to view this token");
			return JsonErrorView.VIEWNAME;
		} else {
			m.put(JsonEntityView.ENTITY, token);
			return TokenApiView.VIEWNAME;
		}
	}

	@RequestMapping(value = "/access/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteAccessTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2AccessTokenEntity token = tokenService.getAccessTokenById(id);

		if (token == null) {
			logger.error("getToken failed; token not found: " + id);
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested token with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("getToken failed; token does not belong to principal " + p.getName());
			m.put(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			m.put(JsonErrorView.ERROR_MESSAGE, "You do not have permission to view this token");
			return JsonErrorView.VIEWNAME;
		} else {
			tokenService.revokeAccessToken(token);

			return HttpCodeView.VIEWNAME;
		}
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/client/{clientId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAccessTokensByClientId(@PathVariable("clientId") String clientId, ModelMap m, Principal p) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null) {
			List<OAuth2AccessTokenEntity> tokens = tokenService.getAccessTokensForClient(client);
			m.put(JsonEntityView.ENTITY, tokens);
			return TokenApiView.VIEWNAME;
		} else {
			// client not found
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested client with id " + clientId + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}

	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/registration/{clientId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getRegistrationTokenByClientId(@PathVariable("clientId") String clientId, ModelMap m, Principal p) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null) {
			OAuth2AccessTokenEntity token = tokenService.getRegistrationAccessTokenForClient(client);
			if (token != null) {
				m.put(JsonEntityView.ENTITY, token);
				return TokenApiView.VIEWNAME;
			} else {
				m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
				m.put(JsonErrorView.ERROR_MESSAGE, "No registration token could be found.");
				return JsonErrorView.VIEWNAME;
			}
		} else {
			// client not found
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested client with id " + clientId + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}

	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/registration/{clientId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public String rotateRegistrationTokenByClientId(@PathVariable("clientId") String clientId, ModelMap m, Principal p) {
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null) {
			OAuth2AccessTokenEntity token = oidcTokenService.rotateRegistrationAccessTokenForClient(client);
			token = tokenService.saveAccessToken(token);

			if (token != null) {
				m.put(JsonEntityView.ENTITY, token);
				return TokenApiView.VIEWNAME;
			} else {
				m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
				m.put(JsonErrorView.ERROR_MESSAGE, "No registration token could be found.");
				return JsonErrorView.VIEWNAME;
			}
		} else {
			// client not found
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested client with id " + clientId + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}

	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllRefreshTokens(ModelMap m, Principal p) {

		Set<OAuth2RefreshTokenEntity> allTokens = tokenService.getAllRefreshTokensForUser(p.getName());
		m.put(JsonEntityView.ENTITY, allTokens);
		return TokenApiView.VIEWNAME;


	}

	@RequestMapping(value = "/refresh/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getRefreshTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2RefreshTokenEntity token = tokenService.getRefreshTokenById(id);

		if (token == null) {
			logger.error("refresh token not found: " + id);
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested token with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("refresh token " + id + " does not belong to principal " + p.getName());
			m.put(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			m.put(JsonErrorView.ERROR_MESSAGE, "You do not have permission to view this token");
			return JsonErrorView.VIEWNAME;
		} else {
			m.put(JsonEntityView.ENTITY, token);
			return TokenApiView.VIEWNAME;
		}
	}

	@RequestMapping(value = "/refresh/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteRefreshTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2RefreshTokenEntity token = tokenService.getRefreshTokenById(id);

		if (token == null) {
			logger.error("refresh token not found: " + id);
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested token with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("refresh token " + id + " does not belong to principal " + p.getName());
			m.put(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			m.put(JsonErrorView.ERROR_MESSAGE, "You do not have permission to view this token");
			return JsonErrorView.VIEWNAME;
		} else {
			tokenService.revokeRefreshToken(token);

			return HttpCodeView.VIEWNAME;
		}
	}
}
