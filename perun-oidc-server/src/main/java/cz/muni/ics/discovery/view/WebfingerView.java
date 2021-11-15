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
package cz.muni.ics.discovery.view;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author jricher
 *
 */
@Component("webfingerView")
@Slf4j
public class WebfingerView extends AbstractView {

	private final Gson gson = new GsonBuilder()
			.setExclusionStrategies(new ExclusionStrategy() {
				@Override
				public boolean shouldSkipField(FieldAttributes f) {
					return false;
				}

				@Override
				public boolean shouldSkipClass(Class<?> clazz) {
					// skip the JPA binding wrapper
					return clazz.equals(BeanPropertyBindingResult.class);
				}
			})
			.serializeNulls()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			.create();

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/jrd+json");
		HttpStatus code = (HttpStatus) model.get(HttpCodeView.CODE);
		if (code == null) {
			code = HttpStatus.OK;
		}

		response.setStatus(code.value());

		try {
			String resource = (String) model.get("resource");
			String issuer = (String) model.get("issuer");

			JsonObject obj = new JsonObject();
			obj.addProperty("subject", resource);

			JsonArray links = new JsonArray();
			JsonObject link = new JsonObject();
			link.addProperty("rel", "http://openid.net/specs/connect/1.0/issuer");
			link.addProperty("href", issuer);
			links.add(link);

			obj.add("links", links);

			Writer out = response.getWriter();
			gson.toJson(obj, out);
		} catch (IOException e) {
			log.error("IOException in WebfingerView.java: ", e);
		}
	}

}
