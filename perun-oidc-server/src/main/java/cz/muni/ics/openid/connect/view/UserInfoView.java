/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.ScopeClaimTranslationService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component(UserInfoView.VIEWNAME)
@Slf4j
public class UserInfoView extends AbstractView {

	public static final String REQUESTED_CLAIMS = "requestedClaims";
	public static final String AUTHORIZED_CLAIMS = "authorizedClaims";
	public static final String SCOPE = "scope";
	public static final String USER_INFO = "userInfo";

	public static final String VIEWNAME = "userInfoView";

	private static JsonParser jsonParser = new JsonParser();

	@Autowired
	private ScopeClaimTranslationService translator;

	protected Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

		@Override
		public boolean shouldSkipField(FieldAttributes f) {

			return false;
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			// skip the JPA binding wrapper
			if (clazz.equals(BeanPropertyBindingResult.class)) {
				return true;
			}
			return false;
		}

	}).create();

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel
	 * (java.util.Map, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		UserInfo userInfo = (UserInfo) model.get(USER_INFO);

		Set<String> scope = (Set<String>) model.get(SCOPE);

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");


		JsonObject authorizedClaims = null;
		JsonObject requestedClaims = null;
		if (model.get(AUTHORIZED_CLAIMS) != null) {
			authorizedClaims = jsonParser.parse((String) model.get(AUTHORIZED_CLAIMS)).getAsJsonObject();
		}
		if (model.get(REQUESTED_CLAIMS) != null) {
			requestedClaims = jsonParser.parse((String) model.get(REQUESTED_CLAIMS)).getAsJsonObject();
		}
		JsonObject json = toJsonFromRequestObj(userInfo, scope, authorizedClaims, requestedClaims);

		writeOut(json, model, request, response);
	}

	protected void writeOut(JsonObject json, Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
		try {
			Writer out = response.getWriter();
			gson.toJson(json, out);
		} catch (IOException e) {

			log.error("IOException in UserInfoView.java: ", e);

		}

	}

	/**
	 * Build a JSON response according to the request object received.
	 *
	 * Claims requested in requestObj.userinfo.claims are added to any
	 * claims corresponding to requested scopes, if any.
	 *
	 * @param ui the UserInfo to filter
	 * @param scope the allowed scopes to filter by
	 * @param authorizedClaims the claims authorized by the client or user
	 * @param requestedClaims the claims requested in the RequestObject
	 * @return the filtered JsonObject result
	 */
	private JsonObject toJsonFromRequestObj(UserInfo ui, Set<String> scope, JsonObject authorizedClaims, JsonObject requestedClaims) {

		// get the base object
		JsonObject obj = ui.toJson();

		Set<String> allowedByScope = translator.getClaimsForScopeSet(scope);
		Set<String> authorizedByClaims = extractUserInfoClaimsIntoSet(authorizedClaims);
		Set<String> requestedByClaims = extractUserInfoClaimsIntoSet(requestedClaims);

		// Filter claims by performing a manual intersection of claims that are allowed by the given scope, requested, and authorized.
		// We cannot use Sets.intersection() or similar because Entry<> objects will evaluate to being unequal if their values are
		// different, whereas we are only interested in matching the Entry<>'s key values.
		JsonObject result = new JsonObject();
		for (Entry<String, JsonElement> entry : obj.entrySet()) {

			if (allowedByScope.contains(entry.getKey())
					|| authorizedByClaims.contains(entry.getKey())) {
				// it's allowed either by scope or by the authorized claims (either way is fine with us)

				if (requestedByClaims.isEmpty() || requestedByClaims.contains(entry.getKey())) {
					// the requested claims are empty (so we allow all), or they're not empty and this claim was specifically asked for
					result.add(entry.getKey(), entry.getValue());
				} // otherwise there were specific claims requested and this wasn't one of them
			}
		}

		return result;
	}

	/**
	 * Pull the claims that have been targeted into a set for processing.
	 * Returns an empty set if the input is null.
	 * @param claims the claims request to process
	 */
	private Set<String> extractUserInfoClaimsIntoSet(JsonObject claims) {
		Set<String> target = new HashSet<>();
		if (claims != null) {
			JsonObject userinfoAuthorized = claims.getAsJsonObject("userinfo");
			if (userinfoAuthorized != null) {
				for (Entry<String, JsonElement> entry : userinfoAuthorized.entrySet()) {
					target.add(entry.getKey());
				}
			}
		}
		return target;
	}
}
