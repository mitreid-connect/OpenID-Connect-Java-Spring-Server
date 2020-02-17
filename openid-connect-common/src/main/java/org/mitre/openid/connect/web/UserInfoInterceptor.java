/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.mitre.openid.connect.model.Address;
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

	private final Whitelist whitelist = Whitelist.relaxed()
		.removeTags("a")
		.removeProtocols("img", "src", "http", "https");

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
				UserInfo userInfo = oidc.getUserInfo();
				if (userInfo != null) {
					sanitiseUserInfo(userInfo);
					request.setAttribute("userInfo", userInfo);
					request.setAttribute("userInfoJson", userInfo.toJson());
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
						sanitiseUserInfo(user);
						request.setAttribute("userInfo", user);
						request.setAttribute("userInfoJson", user.toJson());
					}
				}
			}
		}

		return true;
	}

	private void sanitiseUserInfo(final UserInfo userInfo) {
		userInfo.setSub(sanitise(userInfo.getSub()));
		userInfo.setPreferredUsername(sanitise(userInfo.getPreferredUsername()));
		userInfo.setName(sanitise(userInfo.getName()));
		userInfo.setGivenName(sanitise(userInfo.getGivenName()));
		userInfo.setFamilyName(sanitise(userInfo.getFamilyName()));
		userInfo.setMiddleName(sanitise(userInfo.getMiddleName()));
		userInfo.setNickname(sanitise(userInfo.getNickname()));
		userInfo.setProfile(sanitise(userInfo.getProfile()));
		userInfo.setPicture(sanitise(userInfo.getPicture()));
		userInfo.setWebsite(sanitise(userInfo.getWebsite()));
		userInfo.setEmail(sanitise(userInfo.getEmail()));
		userInfo.setGender(sanitise(userInfo.getGender()));
		userInfo.setLocale(sanitise(userInfo.getLocale()));
		userInfo.setPhoneNumber(sanitise(userInfo.getPhoneNumber()));
		userInfo.setUpdatedTime(sanitise(userInfo.getUpdatedTime()));
		userInfo.setBirthdate(sanitise(userInfo.getBirthdate()));

		Address userInfoAddress = userInfo.getAddress();
		if (userInfoAddress != null) {
			userInfoAddress.setFormatted(sanitise(userInfoAddress.getFormatted()));
			userInfoAddress.setStreetAddress(sanitise(userInfoAddress.getStreetAddress()));
			userInfoAddress.setLocality(sanitise(userInfoAddress.getLocality()));
			userInfoAddress.setRegion(sanitise(userInfoAddress.getRegion()));
			userInfoAddress.setPostalCode(sanitise(userInfoAddress.getPostalCode()));
			userInfoAddress.setCountry(sanitise(userInfoAddress.getCountry()));
			userInfo.setAddress(userInfoAddress);
		}

	}
	
	private String sanitise(String elementToClean) {
		if (elementToClean != null) {
			return Jsoup.clean(elementToClean, whitelist);
		}
		return null;
	}

}
