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

package org.mitre.oauth2.web;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.DeviceCode;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.DeviceCodeService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.oauth2.token.DeviceTokenGranter;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Implements https://tools.ietf.org/html/draft-ietf-oauth-device-flow
 *
 * @see DeviceTokenGranter
 *
 * @author jricher
 *
 */
@Controller
public class DeviceEndpoint {

	public static final String URL = "/device";
	
	public static final Logger logger = LoggerFactory.getLogger(DeviceEndpoint.class);
	
	@Autowired
	private ClientDetailsEntityService clientService;
	
	@Autowired
	private SystemScopeService scopeService;
	
	@Autowired
	private ConfigurationPropertiesBean config;
	
	@Autowired
	private DeviceCodeService deviceCodeService;
	
	private RandomValueStringGenerator randomGenerator = new RandomValueStringGenerator();
	
	@RequestMapping(value = URL, method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String requestDeviceCode(@RequestParam("client_id") String clientId, @RequestParam(name="scope", required=false) String scope, Map<String, String> parameters, ModelMap model) {
		
		ClientDetailsEntity client;
		try {
			client = clientService.loadClientByClientId(clientId);
			
			// make sure this client can do the device flow
			
			Collection<String> authorizedGrantTypes = client.getAuthorizedGrantTypes();
			if (authorizedGrantTypes != null && !authorizedGrantTypes.isEmpty()
					&& !authorizedGrantTypes.contains(DeviceTokenGranter.GRANT_TYPE)) {
				throw new InvalidClientException("Unauthorized grant type: " + DeviceTokenGranter.GRANT_TYPE);
			}
			
		} catch (OAuth2Exception e) {
			logger.error("OAuth2Exception was thrown when attempting to load client", e);
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException was thrown when attempting to load client", e);
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		}

		if (client == null) {
			logger.error("could not find client " + clientId);
			model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		// make sure the client is allowed to ask for those scopes
		Set<String> requestedScopes = OAuth2Utils.parseParameterList(scope);
		Set<String> allowedScopes = client.getScope();
		
		if (!scopeService.scopesMatch(allowedScopes, requestedScopes)) {
			// client asked for scopes it can't have
			logger.error("Client asked for " + requestedScopes + " but is allowed " + allowedScopes);
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			model.put(JsonErrorView.ERROR, "invalid_scope");
			return JsonErrorView.VIEWNAME;
		}
		
		// if we got here the request is legit
		
		// create a device code, should be big and random
		String deviceCode = UUID.randomUUID().toString();
		
		// create a user code, should be random but small and typable
		String userCode = randomGenerator.generate();

		// TODO: expiration
		model.put(JsonEntityView.ENTITY, ImmutableMap.of(
				"device_code", deviceCode,
				"user_code", userCode,
				"verification_uri", config.getIssuer() + URL
				));

		DeviceCode dc = new DeviceCode(deviceCode, userCode, requestedScopes, clientId, parameters);
		
		
		deviceCodeService.save(dc);
		
		return JsonEntityView.VIEWNAME;
		
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = URL, method = RequestMethod.GET)
	public String requestUserCode(ModelMap model) {
		
		// print out a page that asks the user to enter their user code
		// user must be logged in
		
		return "requestUserCode";
	}
	
	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = URL + "/verify", method = RequestMethod.POST)
	public String readUserCode(@RequestParam("userCode") String userCode, ModelMap model) {

		// look up the request based on the user code
		DeviceCode dc = deviceCodeService.lookUpByUserCode(userCode);
		
		ClientDetailsEntity client = clientService.loadClientByClientId(dc.getClientId());
		
		model.put("client", client);
		model.put("dc", dc);
		
		// pre-process the scopes
		Set<SystemScope> scopes = scopeService.fromStrings(dc.getScope());

		Set<SystemScope> sortedScopes = new LinkedHashSet<>(scopes.size());
		Set<SystemScope> systemScopes = scopeService.getAll();

		// sort scopes for display based on the inherent order of system scopes
		for (SystemScope s : systemScopes) {
			if (scopes.contains(s)) {
				sortedScopes.add(s);
			}
		}

		// add in any scopes that aren't system scopes to the end of the list
		sortedScopes.addAll(Sets.difference(scopes, systemScopes));

		model.put("scopes", sortedScopes);

		return "approveDevice";
	}
	
	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = URL + "/approve", method = RequestMethod.POST)
	public String approveDevice(@RequestParam("userCode") String userCode, @RequestParam(value = "approve", required = false) String approve, ModelMap model) {
		
		
		DeviceCode dc = deviceCodeService.lookUpByUserCode(userCode);

		DeviceCode approvedCode = deviceCodeService.approveDeviceCode(dc);
		
		ClientDetailsEntity client = clientService.loadClientByClientId(dc.getClientId());
		
		model.put("client", client);
		
		// pre-process the scopes
		Set<SystemScope> scopes = scopeService.fromStrings(dc.getScope());

		Set<SystemScope> sortedScopes = new LinkedHashSet<>(scopes.size());
		Set<SystemScope> systemScopes = scopeService.getAll();

		// sort scopes for display based on the inherent order of system scopes
		for (SystemScope s : systemScopes) {
			if (scopes.contains(s)) {
				sortedScopes.add(s);
			}
		}

		// add in any scopes that aren't system scopes to the end of the list
		sortedScopes.addAll(Sets.difference(scopes, systemScopes));

		model.put("scopes", sortedScopes);
		
		
		return "deviceApproved";
	}
}
