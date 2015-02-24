/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/
package org.mitre.openid.connect.web;

import static org.mitre.util.JsonUtils.getAsString;
import static org.mitre.util.JsonUtils.getAsStringSet;

import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.model.ResourceSet;
import org.mitre.openid.connect.service.ResourceSetService;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.openid.connect.view.ResourceSetEntityView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@Controller
@RequestMapping(ResourceSetRegistrationEndpoint.URL)
public class ResourceSetRegistrationEndpoint {

	public static final String URL = "/resource_set/resource_set";

	@Autowired
	private ResourceSetService resourceSetService;
	
	private JsonParser parser = new JsonParser();
	
	@RequestMapping(method = RequestMethod.POST, produces = MimeTypeUtils.APPLICATION_JSON_VALUE, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ROLE_USER')")
	public String createResourceSet(@RequestBody String jsonString, Model m, Authentication auth) {
		
		// if auth is OAuth, make sure we've got the right scope
		if (auth instanceof OAuth2Authentication) {
			OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) auth;
			if (oAuth2Authentication.getOAuth2Request().getScope() == null
					|| oAuth2Authentication.getOAuth2Request().getScope().contains(SystemScopeService.RESOURCE_SET_REGISTRATION_SCOPE)) {
				
				// it was an OAuth2 request but it didn't have the right scope
				m.addAttribute("code", HttpStatus.FORBIDDEN);
				return JsonErrorView.VIEWNAME;
				
			}
		}
		
		ResourceSet rs = parseResourceSet(jsonString);
		
		if (rs == null) {
			// there was no resource set in the body
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			return JsonErrorView.VIEWNAME;
		}
		
		rs.setOwner(auth.getName());
		
		m.addAttribute("code", HttpStatus.CREATED);
		m.addAttribute("entity", rs);
		return ResourceSetEntityView.VIEWNAME;
		
	}


	private ResourceSet parseResourceSet(String jsonString) {

		try {
			JsonElement el = parser.parse(jsonString);
			
			if (el.isJsonObject()) {
				JsonObject o = el.getAsJsonObject();
				
				ResourceSet rs = new ResourceSet();
				rs.setName(getAsString(o, "name"));
				rs.setIconUri(getAsString(o, "icon_uri"));
				rs.setType(getAsString(o, "type"));
				rs.setScopes(getAsStringSet(o, "scope"));
				rs.setUri(getAsString(o, "uri"));
				
				return rs;
				
			}
			
			return null;
			
		} catch (JsonParseException e) {
			return null;
		}
		
	}
	
}
