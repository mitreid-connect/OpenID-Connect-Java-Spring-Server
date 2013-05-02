/**
 * 
 */
package org.mitre.discovery.view;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
@Component("webfingerView")
public class WebfingerView extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(WebfingerView.class);

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

		response.setContentType("application/jrd+json");


		HttpStatus code = (HttpStatus) model.get("code");
		if (code == null) {
			code = HttpStatus.OK; // default to 200
		}

		response.setStatus(code.value());

		try {

			String resource = (String)model.get("resource");
			String issuer = (String)model.get("issuer");

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

			//TODO: Error Handling
			logger.error("IOException in JsonEntityView.java: ", e);

		}
	}

}
