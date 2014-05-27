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

import java.security.Principal;
import java.util.Set;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/tokens")
@PreAuthorize("hasRole('ROLE_USER')")
public class TokenAPI {

	@Autowired
	private OAuth2TokenEntityService tokenService;

	private static Logger logger = LoggerFactory.getLogger(TokenAPI.class);

	@RequestMapping(value = "/access", method = RequestMethod.GET, produces = "application/json")
	public String getAllAccessTokens(ModelMap m, Principal p) {

		Set<OAuth2AccessTokenEntity> allTokens = tokenService.getAllAccessTokensForUser(p.getName());
		m.put("entity", allTokens);
		return "tokenApiView";
	}

	@RequestMapping(value = "/access/{id}", method = RequestMethod.GET, produces = "application/json")
	public String getAccessTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2AccessTokenEntity token = tokenService.getAccessTokenById(id);

		if (token == null) {
			logger.error("getToken failed; token not found: " + id);
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested token with id " + id + " could not be found.");
			return "jsonErrorView";
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("getToken failed; token does not belong to principal " + p.getName());
			m.put("code", HttpStatus.FORBIDDEN);
			m.put("errorMessage", "You do not have permission to view this token");
			return "jsonErrorView";
		} else {
			m.put("entity", token);
			return "tokenApiView";
		}
	}

	@RequestMapping(value = "/access/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public String deleteAccessTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2AccessTokenEntity token = tokenService.getAccessTokenById(id);

		if (token == null) {
			logger.error("getToken failed; token not found: " + id);
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested token with id " + id + " could not be found.");
			return "jsonErrorView";
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("getToken failed; token does not belong to principal " + p.getName());
			m.put("code", HttpStatus.FORBIDDEN);
			m.put("errorMessage", "You do not have permission to view this token");
			return "jsonErrorView";
		} else {
			tokenService.revokeAccessToken(token);

			return "httpCodeView";
		}
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET, produces = "application/json")
	public String getAllRefreshTokens(ModelMap m, Principal p) {

		Set<OAuth2RefreshTokenEntity> allTokens = tokenService.getAllRefreshTokensForUser(p.getName());
		m.put("entity", allTokens);
		return "tokenApiView";


	}

	@RequestMapping(value = "/refresh/{id}", method = RequestMethod.GET, produces = "application/json")
	public String getRefreshTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2RefreshTokenEntity token = tokenService.getRefreshTokenById(id);

		if (token == null) {
			logger.error("refresh token not found: " + id);
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested token with id " + id + " could not be found.");
			return "jsonErrorView";
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("refresh token " + id + " does not belong to principal " + p.getName());
			m.put("code", HttpStatus.FORBIDDEN);
			m.put("errorMessage", "You do not have permission to view this token");
			return "jsonErrorView";
		} else {
			m.put("entity", token);
			return "tokenApiView";
		}
	}

	@RequestMapping(value = "/refresh/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public String deleteRefreshTokenById(@PathVariable("id") Long id, ModelMap m, Principal p) {

		OAuth2RefreshTokenEntity token = tokenService.getRefreshTokenById(id);

		if (token == null) {
			logger.error("refresh token not found: " + id);
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested token with id " + id + " could not be found.");
			return "jsonErrorView";
		} else if (!token.getAuthenticationHolder().getAuthentication().getName().equals(p.getName())) {
			logger.error("refresh token " + id + " does not belong to principal " + p.getName());
			m.put("code", HttpStatus.FORBIDDEN);
			m.put("errorMessage", "You do not have permission to view this token");
			return "jsonErrorView";
		} else {
			tokenService.revokeRefreshToken(token);

			return "httpCodeView";
		}
	}

}
