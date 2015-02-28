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

import static org.mitre.uma.web.OAuthScopeEnforcementUtilities.ensureOAuthScope;

import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.uma.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + PermissionRegistrationEndpoint.URL)
@PreAuthorize("hasRole('ROLE_USER')")
public class PermissionRegistrationEndpoint {

	public static final String URL = "permission";
	
	@Autowired
	private PermissionService permissionService;
	
	@RequestMapping(method = RequestMethod.POST, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getPermissionTicket(@RequestBody String jsonString, Model m, Authentication auth) {
		
		ensureOAuthScope(auth, SystemScopeService.UMA_PROTECTION_SCOPE);
		
		
		
		return JsonEntityView.VIEWNAME;
		
	}
	
}
