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

import com.google.common.base.Strings;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author aanganes, jricher
 *
 */
@Component(JsonErrorView.VIEWNAME)
@Slf4j
public class JsonErrorView extends AbstractView {

	/**
	 *
	 */
	public static final String ERROR_MESSAGE = "errorMessage";

	/**
	 *
	 */
	public static final String ERROR = "error";

	public static final String VIEWNAME = "jsonErrorView";

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

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);


		HttpStatus code = (HttpStatus) model.get(HttpCodeView.CODE);
		if (code == null) {
			code = HttpStatus.INTERNAL_SERVER_ERROR; // default to 500
		}

		response.setStatus(code.value());

		try {

			Writer out = response.getWriter();

			String errorTitle = (String) model.get(ERROR);
			if (Strings.isNullOrEmpty(errorTitle)) {
				errorTitle = "mitreid_error";
			}
			String errorMessage = (String) model.get(ERROR_MESSAGE);
			JsonObject obj = new JsonObject();
			obj.addProperty("error", errorTitle);
			obj.addProperty("error_description", errorMessage);
			gson.toJson(obj, out);

		} catch (IOException e) {

			log.error("IOException in JsonErrorView.java: ", e);

		}
	}

}
