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
					santiseUserInfo(userInfo);
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
						santiseUserInfo(user);
						request.setAttribute("userInfo", user);
						request.setAttribute("userInfoJson", user.toJson());
					}
				}
			}
		}

		return true;
	}

	private UserInfo santiseUserInfo(final UserInfo userInfo) {
		userInfo.setSub(nullCheckClean(userInfo.getSub()));
		userInfo.setPreferredUsername(nullCheckClean(userInfo.getPreferredUsername()));
		userInfo.setName(nullCheckClean(userInfo.getName()));
		userInfo.setGivenName(nullCheckClean(userInfo.getGivenName()));
		userInfo.setFamilyName(nullCheckClean(userInfo.getFamilyName()));
		userInfo.setMiddleName(nullCheckClean(userInfo.getMiddleName()));
		userInfo.setNickname(nullCheckClean(userInfo.getNickname()));
		userInfo.setProfile(nullCheckClean(userInfo.getProfile()));
		userInfo.setPicture(nullCheckClean(userInfo.getPicture()));
		userInfo.setWebsite(nullCheckClean(userInfo.getWebsite()));
		userInfo.setEmail(nullCheckClean(userInfo.getEmail()));
		userInfo.setGender(nullCheckClean(userInfo.getGender()));
		userInfo.setLocale(nullCheckClean(userInfo.getLocale()));
		userInfo.setPhoneNumber(nullCheckClean(userInfo.getPhoneNumber()));
		userInfo.setUpdatedTime(nullCheckClean(userInfo.getUpdatedTime()));
		userInfo.setBirthdate(nullCheckClean(userInfo.getBirthdate()));

		Address userInfoAddress = userInfo.getAddress();
		if (userInfoAddress != null) {
			userInfoAddress.setFormatted(nullCheckClean(userInfoAddress.getFormatted()));
			userInfoAddress.setStreetAddress(nullCheckClean(userInfoAddress.getStreetAddress()));
			userInfoAddress.setLocality(nullCheckClean(userInfoAddress.getLocality()));
			userInfoAddress.setRegion(nullCheckClean(userInfoAddress.getRegion()));
			userInfoAddress.setPostalCode(nullCheckClean(userInfoAddress.getPostalCode()));
			userInfoAddress.setCountry(nullCheckClean(userInfoAddress.getCountry()));
			userInfo.setAddress(userInfoAddress);
		}

		return userInfo;
	}
	
	private String nullCheckClean(String elementToClean) {
		final Whitelist whitelist = Whitelist.relaxed()
			.removeTags("a")
			.removeProtocols("img", "src", "http", "https");
		
		if (elementToClean != null) {
			return Jsoup.clean(elementToClean, whitelist);
		}
		return null;
	}

}
