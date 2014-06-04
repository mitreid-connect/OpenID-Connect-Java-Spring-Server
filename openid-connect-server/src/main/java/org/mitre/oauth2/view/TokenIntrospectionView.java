/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
package org.mitre.oauth2.view;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.DateFormatter;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Component("tokenIntrospection")
public class TokenIntrospectionView extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(TokenIntrospectionView.class);
	
	private static DateFormatter isoDateFormatter = new DateFormatter(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));

	private Gson gson = new GsonBuilder().create();

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");

		Writer out;

		try {

			out = response.getWriter();
			UserInfo user = (UserInfo)model.get("user");
			Object obj = model.get("token");
			if (obj instanceof OAuth2AccessTokenEntity) {
				gson.toJson(renderAccessToken((OAuth2AccessTokenEntity)obj, user), out);
			} else if (obj instanceof OAuth2RefreshTokenEntity) {
				gson.toJson(renderRefreshToken((OAuth2RefreshTokenEntity)obj, user), out);
			} else {
				throw new IOException("Couldn't find a valid entity to render");
			}

		} catch (IOException e) {

			logger.error("IOException occurred in TokenIntrospectionView.java: ", e);

		}

	}

	private JsonObject renderAccessToken(OAuth2AccessTokenEntity src, UserInfo user) {
		JsonObject token = new JsonObject();

		token.addProperty("active", true);

		token.addProperty("scope", Joiner.on(" ").join(src.getScope()));

		if (src.getExpiration() != null) {
			try {
				token.addProperty("exp", isoDateFormatter.valueToString(src.getExpiration()));
			} catch (ParseException e) {
				logger.error("Problem formatting expiration date: " + src.getExpiration(), e);
			}
		}
		
		if (user != null) { 
			// if we have a UserInfo, use that for the subject
			token.addProperty("sub", user.getSub());
			token.addProperty("user_id", src.getAuthenticationHolder().getAuthentication().getName());
		} else {
			// otherwise, use the authentication's username
			token.addProperty("sub", src.getAuthenticationHolder().getAuthentication().getName());
			token.addProperty("user_id", src.getAuthenticationHolder().getAuthentication().getName());
		}

		token.addProperty("client_id", src.getAuthenticationHolder().getAuthentication().getOAuth2Request().getClientId());

		token.addProperty("token_type", src.getTokenType());

		return token;		
	}
	
	private JsonObject renderRefreshToken(OAuth2RefreshTokenEntity src, UserInfo user) {
		JsonObject token = new JsonObject();

		token.addProperty("active", true);

		token.addProperty("scope", Joiner.on(" ").join(src.getAuthenticationHolder().getAuthentication().getOAuth2Request().getScope()));

		if (src.getExpiration() != null) {
			try {
				token.addProperty("exp", isoDateFormatter.valueToString(src.getExpiration()));
			} catch (ParseException e) {
				logger.error("Problem formatting expiration date: " + src.getExpiration(), e);
			}
		}
		
		if (user != null) { 
			// if we have a UserInfo, use that for the subject
			token.addProperty("sub", user.getSub());
			token.addProperty("user_id", src.getAuthenticationHolder().getAuthentication().getName());
		} else {
			// otherwise, use the authentication's username
			token.addProperty("sub", src.getAuthenticationHolder().getAuthentication().getName());
			token.addProperty("user_id", src.getAuthenticationHolder().getAuthentication().getName());
		}

		token.addProperty("client_id", src.getAuthenticationHolder().getAuthentication().getOAuth2Request().getClientId());

		return token;
	}
	
}
