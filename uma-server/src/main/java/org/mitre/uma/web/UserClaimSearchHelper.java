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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.impl.WebfingerIssuerService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.openid.connect.web.RootController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableSet;


/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + UserClaimSearchHelper.URL)
@PreAuthorize("hasRole('ROLE_USER')")
public class UserClaimSearchHelper {

	public static final String URL = RootController.API_URL + "/emailsearch";

	private WebfingerIssuerService webfingerIssuerService = new WebfingerIssuerService();

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private ConfigurationPropertiesBean config;


	@RequestMapping(method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String search(@RequestParam(value = "identifier") String email, Model m, Authentication auth, HttpServletRequest req) {

		// check locally first
		UserInfo localUser = userInfoService.getByEmailAddress(email);

		if (localUser != null) {
			Map<String, Object> e = new HashMap<>();
			e.put("issuer", ImmutableSet.of(config.getIssuer()));
			e.put("name", "email");
			e.put("value", localUser.getEmail());

			Map<String, Object> ev = new HashMap<>();
			ev.put("issuer", ImmutableSet.of(config.getIssuer()));
			ev.put("name", "email_verified");
			ev.put("value", localUser.getEmailVerified());

			Map<String, Object> s = new HashMap<>();
			s.put("issuer", ImmutableSet.of(config.getIssuer()));
			s.put("name", "sub");
			s.put("value", localUser.getSub());

			m.addAttribute(JsonEntityView.ENTITY, ImmutableSet.of(e, ev, s));
			return JsonEntityView.VIEWNAME;
		} else {

			// otherwise do a webfinger lookup
			IssuerServiceResponse resp = webfingerIssuerService.getIssuer(req);

			if (resp != null && resp.getIssuer() != null) {
				// we found an issuer, return that
				Map<String, Object> e = new HashMap<>();
				e.put("issuer", ImmutableSet.of(resp.getIssuer()));
				e.put("name", "email");
				e.put("value", email);

				Map<String, Object> ev = new HashMap<>();
				ev.put("issuer", ImmutableSet.of(resp.getIssuer()));
				ev.put("name", "email_verified");
				ev.put("value", true);

				m.addAttribute(JsonEntityView.ENTITY, ImmutableSet.of(e, ev));
				return JsonEntityView.VIEWNAME;
			} else {
				m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
				return JsonErrorView.VIEWNAME;
			}
		}
	}

}
