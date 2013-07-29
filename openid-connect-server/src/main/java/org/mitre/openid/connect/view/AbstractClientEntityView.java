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
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;
import org.mitre.jose.JWSAlgorithmEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
public abstract class AbstractClientEntityView extends AbstractView {
	private static Logger logger = LoggerFactory.getLogger(ClientEntityViewForAdmins.class);

	private Gson gson = new GsonBuilder()
	.setExclusionStrategies(getExclusionStrategy())
	.registerTypeAdapter(JWSAlgorithmEmbed.class, new JsonSerializer<JWSAlgorithmEmbed>() {
		@Override
		public JsonElement serialize(JWSAlgorithmEmbed src, Type typeOfSrc, JsonSerializationContext context) {
			if (src != null) {
				return new JsonPrimitive(src.getAlgorithmName());
			} else {
				return null;
			}
		}
	})
	.registerTypeAdapter(JWEAlgorithmEmbed.class, new JsonSerializer<JWEAlgorithmEmbed>() {
		@Override
		public JsonElement serialize(JWEAlgorithmEmbed src, Type typeOfSrc, JsonSerializationContext context) {
			if (src != null) {
				return new JsonPrimitive(src.getAlgorithmName());
			} else {
				return null;
			}
		}
	})
	.registerTypeAdapter(JWEEncryptionMethodEmbed.class, new JsonSerializer<JWEEncryptionMethodEmbed>() {
		@Override
		public JsonElement serialize(JWEEncryptionMethodEmbed src, Type typeOfSrc, JsonSerializationContext context) {
			if (src != null) {
				return new JsonPrimitive(src.getAlgorithmName());
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

		response.setContentType("application/json");


		HttpStatus code = (HttpStatus) model.get("code");
		if (code == null) {
			code = HttpStatus.OK; // default to 200
		}

		response.setStatus(code.value());

		try {

			Writer out = response.getWriter();
			Object obj = model.get("entity");
			gson.toJson(obj, out);

		} catch (IOException e) {

			logger.error("IOException in JsonEntityView.java: ", e);

		}
	}

}
