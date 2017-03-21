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
/**
 *
 */
package org.mitre.openid.connect.web;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

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

	@Autowired (required = false)
	private UserInfoService userInfoService;

	private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof Authentication){
			request.setAttribute("userAuthorities", gson.toJson(auth.getAuthorities()));
		}

		if (!trustResolver.isAnonymous(auth)) { // skip lookup on anonymous logins
			if (auth instanceof OIDCAuthenticationToken) {
				// if they're logging into this server from a remote OIDC server, pass through their user info
				OIDCAuthenticationToken oidc = (OIDCAuthenticationToken) auth;
				if (oidc.getUserInfo() != null) {
					request.setAttribute("userInfo", oidc.getUserInfo());
					request.setAttribute("userInfoJson", oidc.getUserInfo().toJson());
				} else {
					request.setAttribute("userInfo", null);
					request.setAttribute("userInfoJson", "null");
				}
			} else {
				// don't bother checking if we don't have a principal or a userInfoService to work with
				if (auth != null && auth.getName() != null && userInfoService != null) {

					// try to look up a user based on the principal's name
					UserInfo user = userInfoService.getByUsername(auth.getName());

					// if we have one, inject it so views can use it
					if (user != null) {
						request.setAttribute("userInfo", user);
						request.setAttribute("userInfoJson", user.toJson());
					}
				}
			}
		}

		return true;
	}

}
