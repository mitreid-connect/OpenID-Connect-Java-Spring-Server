package org.mitre.uma.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.uma.model.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.base.Strings;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;

@Component(ResourceSetEntityAbbreviatedView.VIEWNAME)
public class ResourceSetEntityAbbreviatedView extends AbstractView {
	private static Logger logger = LoggerFactory.getLogger(JsonEntityView.class);

	public static final String VIEWNAME = "resourceSetEntityAbbreviatedView";
	
	@Autowired
	private ConfigurationPropertiesBean config;

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
		.setLongSerializationPolicy(LongSerializationPolicy.STRING)
		.create();

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");


		HttpStatus code = (HttpStatus) model.get("code");
		if (code == null) {
			code = HttpStatus.OK; // default to 200
		}

		response.setStatus(code.value());

		String location = (String) model.get("location");
		if (!Strings.isNullOrEmpty(location)) {
			response.setHeader(HttpHeaders.LOCATION, location);
		}
		
		try {

			Writer out = response.getWriter();
			ResourceSet rs = (ResourceSet) model.get("entity");

			JsonObject o = new JsonObject();
			
			o.addProperty("_id", rs.getId().toString()); // set the ID to a string
			o.addProperty("user_access_policy_uri", config.getIssuer() + "manage/resource/" + rs.getId());

			
			gson.toJson(o, out);
			
		} catch (IOException e) {

			logger.error("IOException in ResourceSetEntityView.java: ", e);

		}
	}

}
