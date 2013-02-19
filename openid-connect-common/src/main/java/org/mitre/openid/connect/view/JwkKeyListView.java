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
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
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
@Component("jwkKeyList")
public class JwkKeyListView extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(JwkKeyListView.class);
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

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
		.create();
		
		response.setContentType("application/json");
		
		
		//BiMap<String, PublicKey> keyMap = (BiMap<String, PublicKey>) model.get("keys");
		Map<String, PublicKey> keys = (Map<String, PublicKey>) model.get("keys");
		
		JsonObject obj = new JsonObject();
		JsonArray keyList = new JsonArray();
		obj.add("keys", keyList);
		
		for (String keyId : keys.keySet()) {

			PublicKey key = keys.get(keyId);

			if (key instanceof RSAPublicKey) {
				
				RSAPublicKey rsa = (RSAPublicKey) key;
				
				BigInteger mod = rsa.getModulus();
				BigInteger exp = rsa.getPublicExponent();
				
				String m64 = Base64.encodeBase64URLSafeString(mod.toByteArray());
				String e64 = Base64.encodeBase64URLSafeString(exp.toByteArray());
				
				JsonObject o = new JsonObject();

				o.addProperty("use", "sig"); // since we don't do encryption yet
				o.addProperty("alg", "RSA"); //rsaSigner.getAlgorithm()); // we know this is RSA
				o.addProperty("mod", m64);
				o.addProperty("exp", e64);
				o.addProperty("kid", keyId);

				keyList.add(o);
			} // TODO: deal with non-RSA key types
        }
		
		Writer out;
		
		try {
			
			out = response.getWriter();
			gson.toJson(obj, out);
			
		} catch (IOException e) {
			
			logger.error("IOException in JwkKeyListView.java: ", e);
			
		}

	}

}
