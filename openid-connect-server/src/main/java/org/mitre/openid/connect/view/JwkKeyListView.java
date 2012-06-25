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
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.collect.BiMap;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
		.create();

		
		response.setContentType("application/json");
		
		Writer out = response.getWriter();
		
		//BiMap<String, PublicKey> keyMap = (BiMap<String, PublicKey>) model.get("keys");
		Map<String, JwtSigner> signers = (Map<String, JwtSigner>) model.get("signers");
		
		JsonObject obj = new JsonObject();
		JsonArray keys = new JsonArray();
		obj.add("keys", keys);
		
		for (String keyId : signers.keySet()) {

			JwtSigner src = signers.get(keyId);

			if (src instanceof RsaSigner) {
				
				RsaSigner rsaSigner = (RsaSigner) src;
				
				RSAPublicKey rsa = (RSAPublicKey) rsaSigner.getPublicKey(); // we're sure this is an RSAPublicKey b/c this is an RsaSigner
				
				
				BigInteger mod = rsa.getModulus();
				BigInteger exp = rsa.getPublicExponent();
				
				String m64 = Base64.encodeBase64URLSafeString(mod.toByteArray());
				String e64 = Base64.encodeBase64URLSafeString(exp.toByteArray());
				
				JsonObject o = new JsonObject();

				o.addProperty("use", "sig"); // since we don't do encryption yet
				o.addProperty("alg", rsaSigner.getAlgorithm()); // we know this is RSA
				o.addProperty("mod", m64);
				o.addProperty("exp", e64);
				o.addProperty("kid", keyId);

				keys.add(o);
			} // TODO: deal with non-RSA key types
        }
		
		gson.toJson(obj, out);

	}

}
