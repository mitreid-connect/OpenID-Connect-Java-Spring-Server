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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

@Controller
public class IntrospectionEndpoint {

	@Autowired
	private OAuth2TokenEntityService tokenServices;
	
	@Autowired
	private ClientDetailsEntityService clientService;
	
	private static Logger logger = LoggerFactory.getLogger(IntrospectionEndpoint.class);
	
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
		
		logger.error("InvalidTokenException: " + ex.getStackTrace().toString());
		
		model.put("code", HttpStatus.BAD_REQUEST);
		
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
			logger.error("Verify failed; token value is null");
			modelAndView.addObject("code", HttpStatus.BAD_REQUEST);
			modelAndView.setViewName("httpCodeView");
			return modelAndView;
		}
		
		OAuth2AccessTokenEntity token = null;
		
		try {
			token = tokenServices.readAccessToken(tokenValue);		
		} catch (AuthenticationException e) {
			logger.error("Verify failed; AuthenticationException: " + e.getStackTrace().toString());
			modelAndView.addObject("code", HttpStatus.FORBIDDEN);
			modelAndView.setViewName("httpCodeView");
			return modelAndView;
		}
			
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
					logger.error("Verify failed; client tried to introspect a token of an incorrect scope");
					modelAndView.addObject("code", HttpStatus.BAD_REQUEST);
					modelAndView.setViewName("httpCodeView");
					return modelAndView;
				}
			} else {
				logger.error("Verify failed; client " + clientId + " is not allowed to call introspection endpoint");
				modelAndView.addObject("code", HttpStatus.BAD_REQUEST);
				modelAndView.setViewName("httpCodeView");
				return modelAndView;
			}
		} else {
			//TODO: Log error client not found
			logger.error("Verify failed; client " + clientId + " not found.");
			modelAndView.addObject("code", HttpStatus.NOT_FOUND);
			modelAndView.setViewName("httpCodeView");
			return modelAndView;
		}
		
	}
	
}
