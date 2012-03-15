/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
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
		.registerTypeHierarchyAdapter(PublicKey.class, new JsonSerializer<PublicKey>() {

			@Override
            public JsonElement serialize(PublicKey src, Type typeOfSrc, JsonSerializationContext context) {
				
				
				if (src instanceof RSAPublicKey) {
				
					RSAPublicKey rsa = (RSAPublicKey)src;
					
					
					BigInteger mod = rsa.getModulus();
					BigInteger exp = rsa.getPublicExponent();
					
					String m64 = Base64.encodeBase64URLSafeString(mod.toByteArray());
					String e64 = Base64.encodeBase64URLSafeString(exp.toByteArray());
					
					JsonObject o = new JsonObject();

					o.addProperty("use", "sig");
					o.addProperty("alg", "RSA");
					o.addProperty("mod", m64);
					o.addProperty("exp", e64);
					
					return o;
				} else if (src instanceof ECPublicKey) {
					
					@SuppressWarnings("unused")
					ECPublicKey ec = (ECPublicKey)src;

					// TODO: serialize the EC
					
					return null;
					
				} else {
					
					// skip this class ... we shouldn't have any keys in here that aren't encodable by this serializer
					return null;
				}
				
				
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
