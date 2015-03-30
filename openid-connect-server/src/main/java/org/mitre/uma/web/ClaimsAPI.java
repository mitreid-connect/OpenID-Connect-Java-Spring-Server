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

import java.util.Collection;
import java.util.Set;

import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.web.RootController;
import org.mitre.uma.model.Claim;
import org.mitre.uma.model.ResourceSet;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + ClaimsAPI.URL)
@PreAuthorize("hasRole('ROLE_USER')")
public class ClaimsAPI {
	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(ClaimsAPI.class);
	
	public static final String URL = RootController.API_URL + "/claims";
	
	@Autowired
	private ResourceSetService resourceSetService;
	
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getResourceSetsForCurrentUser(Model m, Authentication auth) {
		
		Collection<ResourceSet> resourceSets = resourceSetService.getAllForOwner(auth.getName());
		
		m.addAttribute(JsonEntityView.ENTITY, resourceSets);
		
		return JsonEntityView.VIEWNAME;
	}
	
	@RequestMapping(value = "/{rsid}", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String getClaimsForResourceSet(@PathVariable (value = "rsid") Long rsid, Model m, Authentication auth) {
		
		ResourceSet rs = resourceSetService.getById(rsid);

		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}
		
		if (!rs.getOwner().equals(auth.getName())) {
			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}
				
		m.addAttribute(JsonEntityView.ENTITY, rs.getClaimsRequired());
		
		return JsonEntityView.VIEWNAME;
	}
	
	@RequestMapping(value = "/{rsid}", method = RequestMethod.PUT, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String setClaimsForResourceSet(@PathVariable (value = "rsid") Long rsid, @RequestBody String jsonString, Model m, Authentication auth) {

		ResourceSet rs = resourceSetService.getById(rsid);
		
		if (rs == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}
		
		if (!rs.getOwner().equals(auth.getName())) {
			// authenticated user didn't match the owner of the resource set
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}
				
		@SuppressWarnings("serial")
		Set<Claim> claims = (new Gson()).fromJson(jsonString, new TypeToken<Set<Claim>>() {}.getType());
		
		rs.setClaimsRequired(claims);
		
		resourceSetService.update(rs, rs);

		m.addAttribute(JsonEntityView.ENTITY, rs.getClaimsRequired());
		
		return JsonEntityView.VIEWNAME;
	}
	
}
