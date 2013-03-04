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

import org.mitre.jose.JWEAlgorithmEntity;
import org.mitre.jose.JWEEncryptionMethodEntity;
import org.mitre.jose.JWSAlgorithmEntity;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * @see ClientEntityViewForAdmins
 * @author jricher
 *
 */
@Component("clientEntityViewUser")
public class ClientEntityViewForUsers extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(ClientEntityViewForUsers.class);

	private Gson gson = new GsonBuilder()
	    .setExclusionStrategies(new ExclusionStrategy() {
	
	        public boolean shouldSkipField(FieldAttributes f) {
	        	// whitelist the handful of fields that are good 
	        	if (f.getName().equals("clientName") ||
	        			f.getName().equals("clientId") ||
	        			f.getName().equals("id") || 
	        			f.getName().equals("clientDescription") ||
	        			f.getName().equals("scope")) {
	        	
	        		return false;
	        	} else {
	        		return true;
	        	}
	        }
	
	        public boolean shouldSkipClass(Class<?> clazz) {
	            // skip the JPA binding wrapper
	            if (clazz.equals(BeanPropertyBindingResult.class)) {
	                return true;
	            }
	            return false;
	        }
	
	    })
	    .registerTypeAdapter(JWSAlgorithmEntity.class, new JsonSerializer<JWSAlgorithmEntity>() {
			@Override
            public JsonElement serialize(JWSAlgorithmEntity src, Type typeOfSrc, JsonSerializationContext context) {
				if (src != null) {
					return new JsonPrimitive(src.getAlgorithmName());
				} else {
					return null;
				}
            }
	    })
	    .registerTypeAdapter(JWEAlgorithmEntity.class, new JsonSerializer<JWEAlgorithmEntity>() {
			@Override
            public JsonElement serialize(JWEAlgorithmEntity src, Type typeOfSrc, JsonSerializationContext context) {
				if (src != null) {
					return new JsonPrimitive(src.getAlgorithmName());
				} else {
					return null;
				}
            }
	    })
	    .registerTypeAdapter(JWEEncryptionMethodEntity.class, new JsonSerializer<JWEEncryptionMethodEntity>() {
			@Override
            public JsonElement serialize(JWEEncryptionMethodEntity src, Type typeOfSrc, JsonSerializationContext context) {
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
