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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component("userInfoView")
public class UserInfoView extends AbstractView {

	private static JsonParser jsonParser = new JsonParser();

	private static Logger logger = LoggerFactory.getLogger(UserInfoView.class);

	private Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

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

		UserInfo userInfo = (UserInfo) model.get("userInfo");

		Set<String> scope = (Set<String>) model.get("scope");

		response.setContentType("application/json");

		Writer out;

		try {

			out = response.getWriter();

			JsonObject authorizedClaims = null;
			JsonObject requestedClaims = null;
			if (model.get("authorizedClaims") != null) {
				authorizedClaims = jsonParser.parse((String) model.get("authorizedClaims")).getAsJsonObject();
			}
			if (model.get("requestedClaims") != null) {
				requestedClaims = jsonParser.parse((String) model.get("requestedClaims")).getAsJsonObject();
			}
			gson.toJson(UserInfoSerializer.toJsonFromRequestObj(userInfo, scope, authorizedClaims, requestedClaims), out);

		} catch (IOException e) {

			logger.error("IOException in UserInfoView.java: ", e);

		}

	}

}
