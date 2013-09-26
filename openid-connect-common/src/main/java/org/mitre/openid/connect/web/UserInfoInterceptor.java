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
/**
 * 
 */
package org.mitre.openid.connect.web;

import java.lang.reflect.Type;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Injects the UserInfo object for the current user into the current model's context, if both exist. Allows JSPs and the like to call "userInfo.name" and other fields.
 * 
 * @author jricher
 *
 */
public class UserInfoInterceptor extends HandlerInterceptorAdapter {
	
	private Gson gson = new GsonBuilder()
		.registerTypeHierarchyAdapter(GrantedAuthority.class, new JsonSerializer<GrantedAuthority>() {
			@Override
            public JsonElement serialize(GrantedAuthority src, Type typeOfSrc, JsonSerializationContext context) {
	            return new JsonPrimitive(src.getAuthority());
            }
		})
		.create();

	@Autowired
	private UserInfoService userInfoService;

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

		if (modelAndView != null && !modelAndView.getModel().containsKey("userInfo")) { // skip checking at all if we have no model and view to hand the user to 
																						// or if there's already a userInfo object in there

			// TODO: this is a patch to get around a potential information leak from #492
			if (!(modelAndView.getView() instanceof RedirectView)) {
			
				// get our principal from the security context
				Principal p = request.getUserPrincipal();
	
				if (p instanceof Authentication && !modelAndView.getModel().containsKey("userAuthorities")){
					Authentication auth = (Authentication)p;
					modelAndView.addObject("userAuthorities", gson.toJson(auth.getAuthorities()));
				}
				
				if (p instanceof OIDCAuthenticationToken) {
					// if they're logging into this server from a remote OIDC server, pass through their user info
					OIDCAuthenticationToken oidc = (OIDCAuthenticationToken) p;
					modelAndView.addObject("userInfo", oidc.getUserInfo());
					modelAndView.addObject("userInfoJson", oidc.getUserInfo().toJson());
				} else {
					if (p != null && p.getName() != null) { // don't bother checking if we don't have a principal
	
						// try to look up a user based on the principal's name
						UserInfo user = userInfoService.getByUsername(p.getName());
	
						// if we have one, inject it so views can use it
						if (user != null) {
							modelAndView.addObject("userInfo", user);
							modelAndView.addObject("userInfoJson", user.toJson());
						}
					}
				}
			}
		}

	}




}
