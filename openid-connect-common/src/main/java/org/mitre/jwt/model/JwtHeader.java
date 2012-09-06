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
package org.mitre.jwt.model;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JwtHeader extends ClaimSet {

	public static final String TYPE = "typ";
	public static final String ALGORITHM = "alg";
	public static final String ENCRYPTION_METHOD = "enc";
	public static final String CONTENT_TYPE = "cty";

	/**
	 * Make an empty header
	 */
	public JwtHeader() {
		super();
    }
	
	/**
	 * Build a header from a JSON object
	 * @param json
	 */
	public JwtHeader(JsonObject json) {
		super(json);
	}
		
	
	public JwtHeader(String b64) {
		super(b64);
    }

	public JwtHeader(JwtHeader jwtHeader) {
		super(jwtHeader);
	}
	
	/**
	 * Load all claims from the given json object into this object
     */
    @Override
    public void loadFromJsonObject(JsonObject json) {
    	
    	JsonObject pass = new JsonObject();
    	
		for (Entry<String, JsonElement> element : json.entrySet()) {
			if (element.getValue().isJsonNull()) {
				pass.add(element.getKey(), element.getValue());
			} else if (element.getKey().equals(TYPE)) {
	        	this.setType(element.getValue().getAsString());
	        } else if (element.getKey().equals(ALGORITHM)) {
	        	this.setAlgorithm(element.getValue().getAsString());
	        } else if (element.getKey().equals(ENCRYPTION_METHOD)) {	        	
	        	this.setEncryptionMethod(element.getValue().getAsString());
	        } else if (element.getKey().equals(CONTENT_TYPE)) {
	        	this.setContentType(element.getValue().getAsString());
	        } else {
	        	pass.add(element.getKey(), element.getValue());
	        }
        }
		
		// now load all the ones we didn't handle specially
		super.loadFromJsonObject(pass);
	}

	/**
     * @return the type
     */
    public String getType() {
    	return getClaimAsString(TYPE);
    }


	/**
     * @param type the type to set
     */
    public void setType(String type) {
    	setClaim(TYPE, type);
    }


	/**
     * @return the algorithm
     */
    public String getAlgorithm() {
    	return getClaimAsString(ALGORITHM);
    }


	/**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
    	setClaim(ALGORITHM, algorithm);
    }


	/**
     * @return the encryptionMethod
     */
    public String getEncryptionMethod() {
    	return getClaimAsString(ENCRYPTION_METHOD);
    }


	/**
     * @param encryptionMethod the encryptionMethod to set
     */
    public void setEncryptionMethod(String encryptionMethod) {
    	setClaim(ENCRYPTION_METHOD, encryptionMethod);
    }

	public static String getContentType() {
		return CONTENT_TYPE;
	}
	
	public void setContentType(String cty) {
		setClaim(CONTENT_TYPE, cty);
	}

}
