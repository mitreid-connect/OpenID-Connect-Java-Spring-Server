package org.mitre.jwt.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JwtHeader extends ClaimSet {

	public static final String TYPE = "typ";
	public static final String ALGORITHM = "alg";
	public static final String ENCRYPTION_METHOD = "enc";

	/**
	 * Make an empty header
	 */
	public JwtHeader() {
		
    }
	
	/**
	 * Build a header from a JSON object
	 * @param json
	 */
	public JwtHeader(JsonObject json) {
		
		for (Entry<String, JsonElement> element : json.entrySet()) {
	        if (element.getKey().equals(TYPE)) {
	        	this.setType(json.get(TYPE).getAsString());
	        } else if (element.getKey().equals(ALGORITHM)) {
	        	this.setAlgorithm(json.get(ALGORITHM).getAsString());
	        } else if (element.getKey().equals(ENCRYPTION_METHOD)) {	        	
	        	this.setEncryptionMethod(json.get(ENCRYPTION_METHOD).getAsString());
	        } else {
	        	if (element.getValue().isJsonPrimitive()){
		        	// we handle all primitives in here
		        	JsonPrimitive prim = element.getValue().getAsJsonPrimitive();
		        	setClaim(element.getKey(), prim);
		        } else {
		        	setClaim(element.getKey(), element.getValue());
		        }
	        }
        }
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

}
