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

package org.mitre.uma.web;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.oauth2.web.AuthenticationUtilities;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.uma.model.Permission;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + AuthorizationRequestEndpoint.URL)
public class AuthorizationRequestEndpoint {

	public static final String RPT = "rpt";
	public static final String TICKET = "ticket";
	public static final String URL = "authz_request";

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private OAuth2TokenEntityService tokenService;
	
	@RequestMapping(method = RequestMethod.POST, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String authorizationRequest(@RequestBody String jsonString, Model m, Authentication auth) {
		
		AuthenticationUtilities.ensureOAuthScope(auth, SystemScopeService.UMA_AUTHORIZATION_SCOPE);
		
		JsonParser parser = new JsonParser();
		JsonElement e = parser.parse(jsonString);
		
		if (e.isJsonObject()) {
			JsonObject o = e.getAsJsonObject();
			
			if (o.has(TICKET)) {
				
				OAuth2AccessTokenEntity rpt = null;
				if (o.has(RPT)) {
					String rptValue = o.get(RPT).getAsString();
					rpt = tokenService.readAccessToken(rptValue);
				}				
				
				String ticketValue = o.get(TICKET).getAsString();
				
				Permission perm = permissionService.getByTicket(ticketValue);
				
				if (perm != null) {
					// found the ticket, see if it's any good
					
					ResourceSet rs = perm.getResourceSet();
					
					
					
					
				} else {
					// ticket wasn't found, return an error
					m.addAttribute(HttpStatus.BAD_REQUEST);
					m.addAttribute(JsonErrorView.ERROR, "invalid_ticket");
					return JsonErrorView.VIEWNAME;
				}
				
			} else {
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Missing JSON elements.");
				return JsonErrorView.VIEWNAME;
			}
			
			
		} else {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Malformed JSON request.");
			return JsonErrorView.VIEWNAME;
		}
		
	}
	
	
}
