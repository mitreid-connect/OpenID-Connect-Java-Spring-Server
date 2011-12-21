package org.mitre.jwt;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

public class JwtHeader {

	private String type;
	
	private String algorithm;
	
	private String encryptionMethod;
	
	private Map<String, Object> claims = new HashMap<String, Object>();

	
	public JwtHeader() {
		
    }


	/**
     * @return the type
     */
    public String getType() {
    	return type;
    }


	/**
     * @param type the type to set
     */
    public void setType(String type) {
    	this.type = type;
    }


	/**
     * @return the algorithm
     */
    public String getAlgorithm() {
    	return algorithm;
    }


	/**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
    	this.algorithm = algorithm;
    }


	/**
     * @return the encryptionMethod
     */
    public String getEncryptionMethod() {
    	return encryptionMethod;
    }


	/**
     * @param encryptionMethod the encryptionMethod to set
     */
    public void setEncryptionMethod(String encryptionMethod) {
    	this.encryptionMethod = encryptionMethod;
    }

    /**
     * Get an extension claim
     */
    public Object getClaim(String key) {
    	return claims.get(key);
    }
    
    /**
     * Set an extension claim
     */
    public void setClaim(String key, Object value) {
    	claims.put(key, value);
    }

    /**
     * Remove an extension claim
     */
    public Object removeClaim(String key) {
    	return claims.remove(key);
    }
    
	/**
	 * Get a copy of this header as a JsonObject. The JsonObject is not
	 * backed by a live copy of this JwtHeader.
	 * @return a copy of the data in this header in a JsonObject
	 */
	public JsonObject getAsJsonObject() {
		JsonObject o = new JsonObject();
		
		o.addProperty("typ", this.type);
		if (this.algorithm != null) {
			o.addProperty("alg", this.algorithm);
		}
		
		if (this.encryptionMethod != null) {
			o.addProperty("enc", this.encryptionMethod);
		}
		
		if (this.claims != null) {
			for (Map.Entry<String, Object> claim : this.claims.entrySet()) {
				if (claim.getValue() instanceof String) {
					o.addProperty(claim.getKey(), (String)claim.getValue());
				} else if (claim.getValue() instanceof Number) {
					o.addProperty(claim.getKey(), (Number)claim.getValue());
				} else if (claim.getValue() instanceof Boolean) {
					o.addProperty(claim.getKey(), (Boolean)claim.getValue());
				} else if (claim.getValue() instanceof Character) {
					o.addProperty(claim.getKey(), (Character)claim.getValue());
				} else if (claim.getValue() != null) {
					// try to put it in as a string
					o.addProperty(claim.getKey(), claim.getValue().toString());
				} else {
					// otherwise add in as a null
					o.add(claim.getKey(), null);
				}
	        }
		}
		
		return o;
	}


}
