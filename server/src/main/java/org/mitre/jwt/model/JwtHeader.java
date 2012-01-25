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

	/**
	 * Load all claims from the given json object into this object
     */
    @Override
    public void loadFromJsonObject(JsonObject json) {
    	
    	JsonObject pass = new JsonObject();
    	
		for (Entry<String, JsonElement> element : json.entrySet()) {
	        if (element.getKey().equals(TYPE)) {
	        	this.setType(json.get(TYPE).getAsString());
	        } else if (element.getKey().equals(ALGORITHM)) {
	        	this.setAlgorithm(json.get(ALGORITHM).getAsString());
	        } else if (element.getKey().equals(ENCRYPTION_METHOD)) {	        	
	        	this.setEncryptionMethod(json.get(ENCRYPTION_METHOD).getAsString());
	        } else {
	        	pass.add(element.getKey(), element.getValue());
	        }
        }
		
		// now load all the ones we didn't handly specially
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

}
