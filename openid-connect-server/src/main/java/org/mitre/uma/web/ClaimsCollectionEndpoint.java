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

import org.mitre.openid.connect.view.JsonErrorView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * Collect claims interactively from the end user.
 * 
 * @author jricher
 *
 */
@Controller
@PreAuthorize("hasRole('ROLE_EXTERNAL_USER')")
@RequestMapping("/" + ClaimsCollectionEndpoint.URL)
public class ClaimsCollectionEndpoint {

	public static final String URL = "rqp_claims";

	
	@RequestMapping(method = RequestMethod.GET)
	public String collectClaims(@RequestParam("client_id") String clientId, @RequestParam("redirect_uri") String redirectUri, 
			@RequestParam("ticket") String ticket, @RequestParam("state") String state,
			Model m, Authentication auth) {

		
		
		return JsonErrorView.VIEWNAME;
	}
	
}
