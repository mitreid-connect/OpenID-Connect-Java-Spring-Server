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
import java.util.Map;

import org.mitre.openid.connect.exception.UnknownUserInfoSchemaException;
import org.mitre.openid.connect.exception.UserNotFoundException;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableMap;

/**
 * OpenID Connect UserInfo endpoint, as specified in Standard sec 5 and Messages sec 2.4.
 * 
 * @author AANGANES
 *
 */
@Controller
public class UserInfoEndpoint {

	@Autowired
	private UserInfoService userInfoService;

	private static Logger logger = LoggerFactory.getLogger(UserInfoEndpoint.class);

	private Map<String, String> schemaToViewNameMap = ImmutableMap.of(
			openIdSchema, jsonUserInfoViewName,
			pocoSchema, pocoUserInfoViewName
			);

	// Valid schemas and associated views
	private static final String openIdSchema = "openid";
	private static final String pocoSchema = "poco";
	private static final String jsonUserInfoViewName = "jsonUserInfoView";
	private static final String pocoUserInfoViewName = "pocoUserInfoView";

	/**
	 * Get information about the user as specified in the accessToken->idToken included in this request
	 * 
	 * @throws UserNotFoundException		    if the user does not exist or cannot be found
	 * @throws UnknownUserInfoSchemaException	if an unknown schema is used
	 * @throws InvalidScopeException            if the oauth2 token doesn't have the "openid" scope
	 */
	@PreAuthorize("hasRole('ROLE_USER') and #oauth2.hasScope('openid')")
	@RequestMapping(value="/userinfo", method= {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
	public String getInfo(Principal p, @RequestParam("schema") String schema, Model model) {

		if (p == null) {
			logger.error("getInfo failed; no principal. Requester is not authorized.");
			model.addAttribute("code", HttpStatus.FORBIDDEN);
			return "httpCodeView";
		}

		String viewName = schemaToViewNameMap.get(schema);
		if (viewName == null) {
			logger.error("getInfo failed; unknown User Info schema " + schema);
			model.addAttribute("code", HttpStatus.BAD_REQUEST);
			return "httpCodeView";
		}

		String userId = p.getName();
		UserInfo userInfo = userInfoService.getBySubject(userId);

		if (userInfo == null) {
			logger.error("getInfo failed; user not found: " + userId);
			model.addAttribute("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		}

		if (p instanceof OAuth2Authentication) {
			OAuth2Authentication authentication = (OAuth2Authentication)p;

			model.addAttribute("scope", authentication.getOAuth2Request().getScope());
			model.addAttribute("requestObject", authentication.getOAuth2Request().getRequestParameters().get("request"));
		}

		model.addAttribute("userInfo", userInfo);

		return viewName;

	}

	/**
	 * @return the schemaToViewNameMap (defaults to an immutable map)
	 */
	public Map<String, String> getSchemaToViewNameMap() {
		return schemaToViewNameMap;
	}

	/**
	 * @param schemaToViewNameMap the schemaToViewNameMap to set
	 */
	public void setSchemaToViewNameMap(Map<String, String> schemaToViewNameMap) {
		this.schemaToViewNameMap = schemaToViewNameMap;
	}

}
