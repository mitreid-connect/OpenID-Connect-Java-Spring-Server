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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jricher
 *
 */
@Component("jsonEntityView")
public class JsonEntityView extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(JsonEntityView.class);

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
	.serializeNulls()
	.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
	.create();

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
