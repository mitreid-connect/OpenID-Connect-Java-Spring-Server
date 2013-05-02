/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
 ******************************************************************************/
package org.mitre.openid.connect.web;

import java.util.Map;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nimbusds.jose.jwk.JWK;

@Controller
public class JsonWebKeyEndpoint {

	@Autowired
	private JwtSigningAndValidationService jwtService;

	@RequestMapping(value = "/jwk", produces = "application/json")
	public String getJwk(Model m) {

		// map from key id to key
		Map<String, JWK> keys = jwtService.getAllPublicKeys();

		// TODO: check if keys are empty, return a 404 here or just an empty list?

		m.addAttribute("keys", keys);

		return "jwkKeyList";
	}

	/**
	 * @return the jwtService
	 */
	public JwtSigningAndValidationService getJwtService() {
		return jwtService;
	}

	/**
	 * @param jwtService the jwtService to set
	 */
	public void setJwtService(JwtSigningAndValidationService jwtService) {
		this.jwtService = jwtService;
	}

}
