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
import java.util.ArrayList;
import java.util.List;
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
import com.nimbusds.jose.JWK;
import com.nimbusds.jose.JWKSet;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.RSAKey;
import com.nimbusds.jose.Use;
import com.nimbusds.jose.util.Base64URL;

/**
 * @author jricher
 *
 */
@Component("jwkKeyList")
public class JwkKeyListView extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(JwkKeyListView.class);
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");
		
		
		//BiMap<String, PublicKey> keyMap = (BiMap<String, PublicKey>) model.get("keys");
		Map<String, PublicKey> keys = (Map<String, PublicKey>) model.get("keys");
		
		List<JWK> jwks = new ArrayList<JWK>();
		
		for (String keyId : keys.keySet()) {

			PublicKey key = keys.get(keyId);

			if (key instanceof RSAPublicKey) {
				
				RSAPublicKey rsa = (RSAPublicKey) key;
				
				BigInteger mod = rsa.getModulus();
				BigInteger exp = rsa.getPublicExponent();
				
				RSAKey rsaKey = new RSAKey(Base64URL.encode(mod.toByteArray()), Base64URL.encode(exp.toByteArray()), Use.SIGNATURE, JWSAlgorithm.RS256, keyId);

				jwks.add(rsaKey);
			} // TODO: deal with non-RSA key types
        }
		
		JWKSet jwkSet = new JWKSet(jwks);
		
		try {
			
			Writer out = response.getWriter();
			out.write(jwkSet.toString());
			
		} catch (IOException e) {
			
			logger.error("IOException in JwkKeyListView.java: ", e);
			
		}

	}

}
