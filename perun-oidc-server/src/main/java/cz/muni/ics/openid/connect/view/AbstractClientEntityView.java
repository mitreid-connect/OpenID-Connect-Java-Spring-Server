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
/**
 *
 */
package cz.muni.ics.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.muni.ics.oauth2.model.PKCEAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;

/**
 *
 * Abstract superclass for client entity view, used with the ClientApi.
 *
 * @see ClientEntityViewForUsers
 * @see ClientEntityViewForAdmins
 *
 * @author jricher
 *
 */
@Slf4j
public abstract class AbstractClientEntityView extends AbstractView {

	private JsonParser parser = new JsonParser();

	private Gson gson = new GsonBuilder()
			.setExclusionStrategies(getExclusionStrategy())
			.registerTypeAdapter(JWSAlgorithm.class, new JsonSerializer<JWSAlgorithm>() {
				@Override
				public JsonElement serialize(JWSAlgorithm src, Type typeOfSrc, JsonSerializationContext context) {
					if (src != null) {
						return new JsonPrimitive(src.getName());
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(JWEAlgorithm.class, new JsonSerializer<JWEAlgorithm>() {
				@Override
				public JsonElement serialize(JWEAlgorithm src, Type typeOfSrc, JsonSerializationContext context) {
					if (src != null) {
						return new JsonPrimitive(src.getName());
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(EncryptionMethod.class, new JsonSerializer<EncryptionMethod>() {
				@Override
				public JsonElement serialize(EncryptionMethod src, Type typeOfSrc, JsonSerializationContext context) {
					if (src != null) {
						return new JsonPrimitive(src.getName());
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(JWKSet.class, new JsonSerializer<JWKSet>() {
				@Override
				public JsonElement serialize(JWKSet src, Type typeOfSrc, JsonSerializationContext context) {
					if (src != null) {
						return parser.parse(src.toString());
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(JWT.class, new JsonSerializer<JWT>() {
				@Override
				public JsonElement serialize(JWT src, Type typeOfSrc, JsonSerializationContext context) {
					if (src != null) {
						return new JsonPrimitive(src.serialize());
					} else {
						return null;
					}
				}

			})
			.registerTypeAdapter(PKCEAlgorithm.class, new JsonSerializer<PKCEAlgorithm>() {
				@Override
				public JsonPrimitive serialize(PKCEAlgorithm src, Type typeOfSrc, JsonSerializationContext context) {
					if (src != null) {
						return new JsonPrimitive(src.getName());
					} else {
						return null;
					}
				}
			})
			.serializeNulls()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			.create();


	/**
	 * @return
	 */
	protected abstract ExclusionStrategy getExclusionStrategy();


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
