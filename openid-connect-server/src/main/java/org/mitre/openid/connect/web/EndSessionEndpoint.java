/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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

package org.mitre.openid.connect.web;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.mitre.jwt.assertion.AssertionValidator;
import org.mitre.jwt.assertion.impl.SelfAssertionValidator;
import org.mitre.openid.connect.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * @author jricher
 *
 */
@Controller
public class EndSessionEndpoint {

	public static final String URL = "endsession";
	
	private static Logger logger = LoggerFactory.getLogger(EndSessionEndpoint.class);
	
	private AssertionValidator validator = new SelfAssertionValidator();
	
	@Autowired
	private UserInfoService userInfoService;
	
	@RequestMapping(value = "/" + URL)
	public String endSession(@RequestParam (value = "id_token_hint", required = false) String idTokenHint,  
		    @RequestParam (value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri,
		    @RequestParam (value = "state", required = false) String state,
		    HttpServletRequest request,
		    Authentication auth, Model m) {

		// are we logged in or not?
		if (auth == null || !request.isUserInRole("ROLE_USER")) {
			// we're not logged in, process the logout
			return null;
		} else {
			// we are logged in, need to prompt the user before we log out
		
			// parse the ID token hint to see if it's valid
			if (!Strings.isNullOrEmpty(idTokenHint)) {
				try {
					JWT idToken = JWTParser.parse(idTokenHint);
					
					if (validator.isValid(idToken)) {
						// we issued this ID token, figure out who it's for
						String subject = idToken.getJWTClaimsSet().getSubject();
						
						userInfoService.getByUsername(subject);
						
					}
				} catch (ParseException e) {
					
				} 
				
			}
			
			// display the end session page
			return "endSession";
		}
	}
	
}
