/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.Writer;
import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author jricher
 *
 */
public class JwkKeyListView extends AbstractView {

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		Gson gson = new GsonBuilder()
		.setExclusionStrategies(new ExclusionStrategy() {
			
			public boolean shouldSkipField(FieldAttributes f) {
				
				return false;
			}
			
			public boolean shouldSkipClass(Class<?> clazz) {
				// skip the JPA binding wrapper
				if (clazz.equals(BeanPropertyBindingResult.class)) {
					return true;
				}
				return false;
			}
							
		})
		.registerTypeAdapter(RSAPublicKey.class, new JsonSerializer<RSAPublicKey>() {

			@Override
            public JsonElement serialize(RSAPublicKey src, Type typeOfSrc, JsonSerializationContext context) {
				
				
				
				JsonObject o = new JsonObject();
				o.addProperty("mod", src.getModulus().toString());
				
				return o;
				
            }
			
		})		
		.create();

		
		response.setContentType("application/json");
		
		Writer out = response.getWriter();
		
		Object obj = model.get("entity");
		if (obj == null) {
			obj = model;
		}
		
		gson.toJson(obj, out);

	}

}
