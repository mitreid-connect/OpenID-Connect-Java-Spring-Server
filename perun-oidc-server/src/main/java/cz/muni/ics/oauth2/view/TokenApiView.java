/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.oauth2.view;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

@Component(TokenApiView.VIEWNAME)
@Slf4j
public class TokenApiView extends AbstractView {

	public static final String VIEWNAME = "tokenApiView";

	private Gson gson = new GsonBuilder()
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

			})
			.registerTypeAdapter(OAuth2AccessTokenEntity.class, new JsonSerializer<OAuth2AccessTokenEntity>() {

				@Override
				public JsonElement serialize(OAuth2AccessTokenEntity src,
						Type typeOfSrc, JsonSerializationContext context) {


					JsonObject o = new JsonObject();

					o.addProperty("value", src.getValue());
					o.addProperty("id", src.getId());
					o.addProperty("refreshTokenId", src.getRefreshToken() != null ? src.getRefreshToken().getId() : null);

					o.add("scopes", context.serialize(src.getScope()));

					o.addProperty("clientId", src.getClient().getClientId());
					o.addProperty("userId", src.getAuthenticationHolder().getAuthentication().getName());

					o.add("expiration", context.serialize(src.getExpiration()));

					return o;
				}

			})
			.registerTypeAdapter(OAuth2RefreshTokenEntity.class, new JsonSerializer<OAuth2RefreshTokenEntity>() {

				@Override
				public JsonElement serialize(OAuth2RefreshTokenEntity src,
						Type typeOfSrc, JsonSerializationContext context) {
					JsonObject o = new JsonObject();

					o.addProperty("value", src.getValue());
					o.addProperty("id", src.getId());

					o.add("scopes", context.serialize(src.getAuthenticationHolder().getAuthentication().getOAuth2Request().getScope()));

					o.addProperty("clientId", src.getClient().getClientId());
					o.addProperty("userId", src.getAuthenticationHolder().getAuthentication().getName());

					o.add("expiration", context.serialize(src.getExpiration()));

					return o;
				}

			})
			.serializeNulls()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			.create();

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);


		HttpStatus code = (HttpStatus) model.get(HttpCodeView.CODE);
		if (code == null) {
			code = HttpStatus.OK; // default to 200
		}

		response.setStatus(code.value());

		try {

			Writer out = response.getWriter();
			Object obj = model.get(JsonEntityView.ENTITY);
			gson.toJson(obj, out);

		} catch (IOException e) {

			log.error("IOException in JsonEntityView.java: ", e);

		}
	}

}
