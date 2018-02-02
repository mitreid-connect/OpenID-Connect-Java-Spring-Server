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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mitre.oauth2.web.IntrospectionEndpoint;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.web.DynamicClientRegistrationEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * @author jricher
 *
 */
@Controller
public class UmaDiscoveryEndpoint {

	@Autowired
	private ConfigurationPropertiesBean config;

	@RequestMapping(".well-known/uma-configuration")
	public String umaConfiguration(Model model) {

		Map<String, Object> m = new HashMap<>();

		String issuer = config.getIssuer();
		ImmutableSet<String> tokenProfiles = ImmutableSet.of("bearer");
		ArrayList<String> grantTypes = Lists.newArrayList("authorization_code", "implicit", "urn:ietf:params:oauth:grant-type:jwt-bearer", "client_credentials", "urn:ietf:params:oauth:grant_type:redelegate");

		m.put("version", "1.0");
		m.put("issuer", issuer);
		m.put("pat_profiles_supported", tokenProfiles);
		m.put("aat_profiles_supported", tokenProfiles);
		m.put("rpt_profiles_supported", tokenProfiles);
		m.put("pat_grant_types_supported", grantTypes);
		m.put("aat_grant_types_supported", grantTypes);
		m.put("claim_token_profiles_supported", ImmutableSet.of());
		m.put("uma_profiles_supported", ImmutableSet.of());
		m.put("dynamic_client_endpoint", issuer + DynamicClientRegistrationEndpoint.URL);
		m.put("token_endpoint", issuer + "token");
		m.put("authorization_endpoint", issuer + "authorize");
		m.put("requesting_party_claims_endpoint", issuer + ClaimsCollectionEndpoint.URL);
		m.put("introspection_endpoint", issuer + IntrospectionEndpoint.URL);
		m.put("resource_set_registration_endpoint", issuer + ResourceSetRegistrationEndpoint.DISCOVERY_URL);
		m.put("permission_registration_endpoint", issuer + PermissionRegistrationEndpoint.URL);
		m.put("rpt_endpoint", issuer + AuthorizationRequestEndpoint.URL);



		model.addAttribute("entity", m);
		return JsonEntityView.VIEWNAME;
	}


}
