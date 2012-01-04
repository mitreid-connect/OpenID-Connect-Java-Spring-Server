package org.mitre.jwt.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ClaimSet {

	private Map<String, Object> claims = new LinkedHashMap<String, Object>();
	
    /**
     * Get an extension claim
     */
    public Object getClaim(String key) {
    	return claims.get(key);
    }
    
    /**
     * Get a claim as a string
     */
    public String getClaimAsString(String key) {
    	Object v = claims.get(key);
    	if (v != null) {
    		return v.toString();
    	} else {
    		return null;
    	}
    }
    
    /**
     * Get a claim as a Date
     */
    public Date getClaimAsDate(String key) {
    	Object v = claims.get(key);
    	if (v != null) {
    		if (v instanceof Date) {
    			return (Date) v;
    		} else if (v instanceof Long) {
    			return new Date((Long) v);
    		} else {
    			return null;
    		}
    	} else {
    		return null;
    	}
    }
    
    /**
     * Set an extension claim
     */
    public void setClaim(String key, Object value) {
    	claims.put(key, value);
    }

    /**
     * Set a primitive claim
     */
    public void setClaim(String key, JsonPrimitive prim) {
    	if (prim.isBoolean()) {
    		claims.put(key, prim.getAsBoolean());
    	} else if (prim.isNumber()) {
    		claims.put(key, prim.getAsNumber());
    	} else if (prim.isString()) {
    		claims.put(key, prim.getAsString());
    	}    	
    }
    
    /**
     * Remove an extension claim
     */
    public Object removeClaim(String key) {
    	return claims.remove(key);
    }
    
	
	/**
	 * Get a copy of this claim set as a JsonObject. The JsonObject is not
	 * backed by a live copy of this ClaimSet.
	 * @return a copy of the data in this header in a JsonObject
	 */
	public JsonObject getAsJsonObject() {
		JsonObject o = new JsonObject();
		
		if (this.claims != null) {
			for (Map.Entry<String, Object> claim : this.claims.entrySet()) {
				if (claim.getValue() instanceof JsonElement) {
					o.add(claim.getKey(), (JsonElement)claim.getValue());
				} else if (claim.getValue() instanceof String) {
					o.addProperty(claim.getKey(), (String)claim.getValue());
				} else if (claim.getValue() instanceof Number) {
					o.addProperty(claim.getKey(), (Number)claim.getValue());
				} else if (claim.getValue() instanceof Boolean) {
					o.addProperty(claim.getKey(), (Boolean)claim.getValue());
				} else if (claim.getValue() instanceof Character) {
					o.addProperty(claim.getKey(), (Character)claim.getValue());
				} else if (claim.getValue() instanceof Date) {
					// dates get serialized out as integers
					o.addProperty(claim.getKey(), ((Date)claim.getValue()).getTime() / 1000L);
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
