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

import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class JsonWebKeyEndpoint {

	@Autowired
	JwtSigningAndValidationService jwtService;
	
	@RequestMapping("/jwk")
	public ModelAndView getJwk() {
		
		Collection<PublicKey> keys = jwtService.getAllPublicKeys().values();
		
		// TODO: check if keys are empty, return a 404 here or just an empty list?
		
		Map<String, Object> jwk = new HashMap<String, Object>();
		jwk.put("jwk", keys);
		
		return new ModelAndView("jwkKeyList", "entity", jwk);
	}
	
}
