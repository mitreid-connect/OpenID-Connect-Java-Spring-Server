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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@Controller
public class IntrospectionEndpoint {

	@Autowired
	private OAuth2TokenEntityService tokenServices;
	
	@Autowired
	private ClientDetailsEntityService clientService;
	
	public IntrospectionEndpoint() {
		
	}
	
	public IntrospectionEndpoint(OAuth2TokenEntityService tokenServices) {
		this.tokenServices = tokenServices;
	}
	
	@ExceptionHandler(InvalidTokenException.class)
	public ModelAndView tokenNotFound(InvalidTokenException ex) {
		Map<String,Boolean> e = ImmutableMap.of("valid", Boolean.FALSE);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("entity", e);
		// TODO: http code?
		
		return new ModelAndView("jsonEntityView", model);
	}
	
	@PreAuthorize("hasRole('ROLE_CLIENT')")
	@RequestMapping("/introspect")
	public ModelAndView verify(@RequestParam("token") String tokenValue, Principal p, ModelAndView modelAndView) {
		
		/*
		if (p != null && p instanceof OAuth2Authentication) {
			OAuth2Authentication auth = (OAuth2Authentication)p;
			
			if (auth.getDetails() != null && auth.getDetails() instanceof OAuth2AuthenticationDetails) {
				OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)auth.getDetails();
				
				String tokenValue = details.getTokenValue();
				
				OAuth2AccessTokenEntity token = tokenServices.readAccessToken(tokenValue);
		
				if (token != null) {
					// if it's a valid token, we'll print out the scope and expiration
					modelAndView.setViewName("tokenIntrospection");
					modelAndView.addObject("entity", token);
				}
			}
		}*/
		
		if (Strings.isNullOrEmpty(tokenValue)) {
			throw new InvalidTokenException("No token found!");
		}
		
		OAuth2AccessTokenEntity token = tokenServices.readAccessToken(tokenValue);
	
		ClientDetailsEntity tokenClient = token.getClient();
		// clientID is the principal name in the authentication
		String clientId = p.getName();
		ClientDetailsEntity authClient = clientService.loadClientByClientId(clientId);
		
		if (tokenClient != null && authClient != null) {
			if (authClient.isAllowIntrospection()) {
				
				// if it's the same client that the token was issued to, or it at least has all the scopes the token was issued with
				if (authClient.equals(tokenClient) || authClient.getScope().containsAll(token.getScope())) {
				
					// if it's a valid token, we'll print out information on it
					modelAndView.setViewName("tokenIntrospection");
					modelAndView.addObject("entity", token);
					return modelAndView;
				} else {
					throw new InvalidScopeException("Tried to introspect a token of different scope");
				}
			} else {
				throw new InvalidClientException("Client is not allowed to call introspection endpoint.");
			}
		} else {
			throw new InvalidClientException("Client not found.");
		}
		
	}
	
}
