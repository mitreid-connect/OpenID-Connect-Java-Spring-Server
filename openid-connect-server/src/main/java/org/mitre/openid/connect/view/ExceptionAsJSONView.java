/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.JsonObject;

/**
 * @author nemonik
 *
 */
@Component("exceptionAsJSONView")
public class ExceptionAsJSONView extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(ExceptionAsJSONView.class);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel
	 * (java.util.Map, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest requesr, HttpServletResponse response) {

		response.setContentType("application/json");

		response.setStatus(HttpStatus.BAD_REQUEST.value());
		
		final JsonObject jsonObject = new JsonObject();

		Object ex = model.get("exception");

		jsonObject.addProperty("error", ex.getClass().getName());
		jsonObject.addProperty("error_description",
				((Exception) ex).getMessage());

		try {
			
			response.getWriter().write(jsonObject.toString());
			
		} catch (IOException e) {
			
			logger.error("IOException in ExceptionAsJSONView.java: " + e.getStackTrace());
			
		}
	}

}
