/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

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

import com.google.common.collect.ImmutableSet;
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
 * View bean for full view of client entity, for admins.
 * 
 * @see ClientEntityViewForUsers
 * @author jricher
 *
 */
@Component("clientEntityViewAdmins")
public class ClientEntityViewForAdmins extends AbstractClientEntityView {

	private Set<String> blacklistedFields = ImmutableSet.of("additionalInformation");
	
	/**
	 * @return
	 */
    protected ExclusionStrategy getExclusionStrategy() {
	    return new ExclusionStrategy() {
	
	        public boolean shouldSkipField(FieldAttributes f) {
	        	if (blacklistedFields.contains(f.getName())) {
	        		return true;
	        	} else {
	        		return false;
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
