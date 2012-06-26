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
package org.mitre.openid.connect.web;

import java.security.Principal;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.exception.UnknownUserInfoSchemaException;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * OpenID Connect UserInfo endpoint, as specified in Standard sec 5 and Messages sec 2.4.
 * 
 * @author AANGANES
 *
 */
@Controller
public class UserInfoEndpoint {

	@Autowired
	OAuth2TokenEntityService tokenService;
	
	@Autowired
	UserInfoService userInfoService;
	
	// Valid schemas and associated views
	private static final String openIdSchema = "openId";
	private static final String pocoSchema = "poco";
	private static final String jsonUserInfoViewName = "jsonUserInfoView";
	private static final String pocoUserInfoViewName = "pocoUserInfoView";
	
	/**
	 * Get information about the user as specified in the accessToken->idToken included in this request
	 * 
	 * @param accessToken						the Access Token associated with this request
	 * @param schema							the data schema to use, default is openid	
	 * @param mav								the ModelAndView object associated with this request
	 * @return									JSON or JWT response containing UserInfo data
	 * @throws UsernameNotFoundException		if the user does not exist or cannot be found
	 * @throws UnknownUserInfoSchemaException	if an unknown schema is used
	 */
	@RequestMapping(value="/userinfo", method= {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView getInfo(Principal p, @RequestParam("schema") String schema, ModelAndView mav) {

		
		if (p == null) {
			throw new UsernameNotFoundException("Invalid User"); 
		}
		
		String viewName = null;
		if (schema.equalsIgnoreCase( openIdSchema )){
			viewName = jsonUserInfoViewName;
		} else if (schema.equalsIgnoreCase( pocoSchema )) {
			viewName = pocoUserInfoViewName;
		} else {
			throw new UnknownUserInfoSchemaException("Unknown User Info Schema: " + schema );
		}
		String userId = p.getName(); 
		UserInfo userInfo = userInfoService.getByUserId(userId);
		
		if (userInfo == null) {
			throw new UsernameNotFoundException("Invalid User"); 
		}
		
		return new ModelAndView(viewName, "userInfo", userInfo);

	}
	
}
