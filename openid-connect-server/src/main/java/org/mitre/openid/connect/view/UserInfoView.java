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
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

@Component("userInfoView")
public class UserInfoView extends AbstractView {
	
	private static JsonParser jsonParser = new JsonParser();

	private static Logger logger = LoggerFactory.getLogger(UserInfoView.class);

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		UserInfo userInfo = (UserInfo) model.get("userInfo");

		Set<String> scope = (Set<String>) model.get("scope");
		
		String claimsRequestJsonString = (String) model.get("claimsRequest");
		
		// getting the 'claims request parameter' from the model
		JsonObject claimsRequest = null;
		if (!Strings.isNullOrEmpty(claimsRequestJsonString)) {
			JsonElement parsed = jsonParser.parse(claimsRequestJsonString);
			if (parsed.isJsonObject()) {
				claimsRequest = parsed.getAsJsonObject();
			} else {
				// claimsRequest stays null
				logger.warn("Claims parameter not a valid JSON object: " + claimsRequestJsonString);
			}
		}
		

		Gson gson = new GsonBuilder()
		.setExclusionStrategies(new ExclusionStrategy() {

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

		response.setContentType("application/json");

		Writer out;

		try {

			out = response.getWriter();

			if (model.get("requestObject") != null) {

				try {
					String jwtString = (String)model.get("requestObject");
					JWT requestObject = JWTParser.parse(jwtString);

					// FIXME: move to GSON for easier processing
					JsonObject obj = (JsonObject) jsonParser.parse(requestObject.getJWTClaimsSet().toJSONObject().toJSONString());

					gson.toJson(toJsonFromRequestObj(userInfo, scope, obj, claimsRequest), out);
				} catch (JsonSyntaxException e) {
					logger.error("JsonSyntaxException in UserInfoView.java: ", e);
				} catch (JsonIOException e) {
					logger.error("JsonIOException in UserInfoView.java: ", e);
				} catch (ParseException e) {
					logger.error("ParseException in UserInfoView.java: ", e);
				}

			} else {

				gson.toJson(toJson(userInfo, scope), out);

			}

		} catch (IOException e) {

			logger.error("IOException in UserInfoView.java: ", e);

		}

	}

	private JsonObject toJson(UserInfo ui, Set<String> scope) {

		JsonObject obj = new JsonObject();

		if (scope.contains("openid")) {
			obj.addProperty("sub", ui.getSub());
		}

		if (scope.contains("profile")) {
			obj.addProperty("name", ui.getName());
			obj.addProperty("preferred_username", ui.getPreferredUsername());
			obj.addProperty("given_name", ui.getGivenName());
			obj.addProperty("family_name", ui.getFamilyName());
			obj.addProperty("middle_name", ui.getMiddleName());
			obj.addProperty("nickname", ui.getNickname());
			obj.addProperty("profile", ui.getProfile());
			obj.addProperty("picture", ui.getPicture());
			obj.addProperty("website", ui.getWebsite());
			obj.addProperty("gender", ui.getGender());
			obj.addProperty("zone_info", ui.getZoneinfo());
			obj.addProperty("locale", ui.getLocale());
			obj.addProperty("updated_time", ui.getUpdatedTime());
			obj.addProperty("birthdate", ui.getBirthdate());
		}

		if (scope.contains("email")) {
			obj.addProperty("email", ui.getEmail());
			obj.addProperty("email_verified", ui.getEmailVerified());
		}

		if (scope.contains("phone")) {
			obj.addProperty("phone_number", ui.getPhoneNumber());
		}

		if (scope.contains("address") && ui.getAddress() != null) {

			JsonObject addr = new JsonObject();
			addr.addProperty("formatted", ui.getAddress().getFormatted());
			addr.addProperty("street_address", ui.getAddress().getStreetAddress());
			addr.addProperty("locality", ui.getAddress().getLocality());
			addr.addProperty("region", ui.getAddress().getRegion());
			addr.addProperty("postal_code", ui.getAddress().getPostalCode());
			addr.addProperty("country", ui.getAddress().getCountry());

			obj.add("address", addr);
		}


		return obj;
	}

	/**
	 * Build a JSON response according to the request object received.
	 * 
	 * Claims requested in requestObj.userinfo.claims are added to any
	 * claims corresponding to requested scopes, if any.
	 * 
	 * @param ui
	 * @param scope
	 * @param requestObj
	 * @param claimsRequest the claims request parameter object.
	 * @return
	 */
	private JsonObject toJsonFromRequestObj(UserInfo ui, Set<String> scope, JsonObject requestObj, JsonObject claimsRequest) {

		JsonObject obj = toJson(ui, scope);

		//Process list of requested claims out of the request object
		JsonElement claims = requestObj.get("claims");
		if (claims == null || !claims.isJsonObject()) {
			return obj;
		}

		JsonElement userinfo = claims.getAsJsonObject().get("userinfo");
		if (userinfo == null || !userinfo.isJsonObject()) {
			return obj;
		}

		
		// Filter claims from the request object with the claims from the claims request parameter, if it exists
		
		// Doing the set intersection manually because the claim entries may be referring to
		// the same claim but have different 'individual claim values', causing the Entry<> to be unequal, 
		// which doesn't allow the use of the more compact Sets.intersection() type method.
		Set<Entry<String, JsonElement>> requestClaimsSet = Sets.newHashSet();
		if (claimsRequest != null) {
			
			for (Entry<String, JsonElement> entry : userinfo.getAsJsonObject().entrySet()) {
				if (claimsRequest.has(entry.getKey())) {
					requestClaimsSet.add(entry);
				}
			}
			
		}
		
		// TODO: this method is likely to be fragile if the data model changes at all

		//For each claim found, add it if not already present
		for (Entry<String, JsonElement> i : requestClaimsSet) {
			String claimName = i.getKey();
			if (!obj.has(claimName)) {
				String value = "";


				//Process claim names to go from "claim_name" to "ClaimName"
				String camelClaimName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, claimName);
				//Now we have "getClaimName"
				String methodName = "get" + camelClaimName;
				Method getter = null;
				try {
					getter = ui.getClass().getMethod(methodName);
					value = (String) getter.invoke(ui);
					obj.addProperty(claimName, value);
				} catch (SecurityException e) {
					logger.error("SecurityException in UserInfoView.java: ", e);
				} catch (NoSuchMethodException e) {
					logger.error("NoSuchMethodException in UserInfoView.java: ", e);
				} catch (IllegalArgumentException e) {
					logger.error("IllegalArgumentException in UserInfoView.java: ", e);
				} catch (IllegalAccessException e) {
					logger.error("IllegalAccessException in UserInfoView.java: ", e);
				} catch (InvocationTargetException e) {
					logger.error("InvocationTargetException in UserInfoView.java: ", e);
				}
			}
		}



		return obj;

	}
}
