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

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IntrospectionEndpoint {

	@Autowired
	OAuth2TokenEntityService tokenServices;
	
	public IntrospectionEndpoint() {
		
	}
	
	public IntrospectionEndpoint(OAuth2TokenEntityService tokenServices) {
		this.tokenServices = tokenServices;
	}
	
	@RequestMapping("/oauth/verify")
	public ModelAndView verify(Principal p, ModelAndView modelAndView) {
		
		// assume the token's not valid until proven otherwise
		modelAndView.setViewName("tokenNotFound");
		
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
		}
		
		return modelAndView;
	}
	
}
