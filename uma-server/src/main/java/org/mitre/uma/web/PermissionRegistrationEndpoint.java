/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

import static org.mitre.oauth2.web.AuthenticationUtilities.ensureOAuthScope;
import static org.mitre.util.JsonUtils.getAsLong;
import static org.mitre.util.JsonUtils.getAsStringSet;

import java.util.Set;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.PermissionService;
import org.mitre.uma.service.ResourceSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + PermissionRegistrationEndpoint.URL)
@PreAuthorize("hasRole('ROLE_USER')")
public class PermissionRegistrationEndpoint {
	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(PermissionRegistrationEndpoint.class);

	public static final String URL = "permission";

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ResourceSetService resourceSetService;

	@Autowired
	private SystemScopeService scopeService;

	private JsonParser parser = new JsonParser();

	@RequestMapping(method = RequestMethod.POST, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getPermissionTicket(@RequestBody String jsonString, Model m, Authentication auth) {

		ensureOAuthScope(auth, SystemScopeService.UMA_PROTECTION_SCOPE);

		try {

			// parse the permission request

			JsonElement el = parser.parse(jsonString);
			if (el.isJsonObject()) {
				JsonObject o = el.getAsJsonObject();

				Long rsid = getAsLong(o, "resource_set_id");
				Set<String> scopes = getAsStringSet(o, "scopes");

				if (rsid == null || scopes == null || scopes.isEmpty()){
					// missing information
					m.addAttribute("code", HttpStatus.BAD_REQUEST);
					m.addAttribute("errorMessage", "Missing required component of permission registration request.");
					return JsonErrorView.VIEWNAME;
				}

				// trim any restricted scopes
				Set<SystemScope> scopesRequested = scopeService.fromStrings(scopes);
				scopesRequested = scopeService.removeRestrictedAndReservedScopes(scopesRequested);
				scopes = scopeService.toStrings(scopesRequested);

				ResourceSet resourceSet = resourceSetService.getById(rsid);

				// requested resource set doesn't exist
				if (resourceSet == null) {
					m.addAttribute("code", HttpStatus.NOT_FOUND);
					m.addAttribute("errorMessage", "Requested resource set not found: " + rsid);
					return JsonErrorView.VIEWNAME;
				}

				// authorized user of the token doesn't match owner of the resource set
				if (!resourceSet.getOwner().equals(auth.getName())) {
					m.addAttribute("code", HttpStatus.FORBIDDEN);
					m.addAttribute("errorMessage", "Party requesting permission is not owner of resource set, expected " + resourceSet.getOwner() + " got " + auth.getName());
					return JsonErrorView.VIEWNAME;
				}

				// create the permission
				PermissionTicket permission = permissionService.createTicket(resourceSet, scopes);

				if (permission != null) {
					// we've created the permission, return the ticket
					JsonObject out = new JsonObject();
					out.addProperty("ticket", permission.getTicket());
					m.addAttribute("entity", out);

					m.addAttribute("code", HttpStatus.CREATED);

					return JsonEntityView.VIEWNAME;
				} else {
					// there was a failure creating the permission object

					m.addAttribute("code", HttpStatus.INTERNAL_SERVER_ERROR);
					m.addAttribute("errorMessage", "Unable to save permission and generate ticket.");

					return JsonErrorView.VIEWNAME;
				}

			} else {
				// malformed request
				m.addAttribute("code", HttpStatus.BAD_REQUEST);
				m.addAttribute("errorMessage", "Malformed JSON request.");
				return JsonErrorView.VIEWNAME;
			}
		} catch (JsonParseException e) {
			// malformed request
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			m.addAttribute("errorMessage", "Malformed JSON request.");
			return JsonErrorView.VIEWNAME;
		}

	}

}
