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
/**
 * 
 */
package org.mitre.swd.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
@Component("jsonXrdResponseView")
public class XrdJsonResponse extends AbstractView {
	
	private static Logger logger = LoggerFactory.getLogger(XrdJsonResponse.class);

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
		Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				return false;
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				// skip the JPA binding wrapper
				if (clazz.equals(BeanPropertyBindingResult.class)) {
					return true;
				} else {
					return false;
				}
			}

		})
		.create();

		response.setContentType("application/json");

		Map<String, String> links = (Map<String, String>) model.get("links");

		JsonObject obj = new JsonObject();
		JsonArray linksList = new JsonArray();
		obj.add("links", linksList);
		
		// map of "rel" -> "link" values
		for (Map.Entry<String, String> link : links.entrySet()) {
	        JsonObject l = new JsonObject();
	        l.addProperty("rel", link.getKey());
	        l.addProperty("link", link.getValue());
	        
	        linksList.add(l);
        }
		
		Writer out;
		
        try {
        	
	        out = response.getWriter();
	        gson.toJson(obj, out);
	        
        } catch (IOException e) {
	        
        	logger.error("IOException in XrdJsonResponse.java: ", e);
        	
        }		
	}
}
