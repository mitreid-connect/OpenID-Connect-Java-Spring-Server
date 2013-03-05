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
@Component("clientEntityViewUsers")
public class ClientEntityViewForUsers extends AbstractClientEntityView {

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.view.AbstractClientEntityView#getExclusionStrategy()
	 */
    @Override
    protected ExclusionStrategy getExclusionStrategy() {
    	return new ExclusionStrategy() {
    		
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
	
	    };
    }
	
}
