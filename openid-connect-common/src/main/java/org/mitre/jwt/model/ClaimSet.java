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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Generic container for JSON-based claims. Backed with a {@link Map} that preserves
 * insertion order. Several convenience methods for getting and setting claims in different
 * formats.
 * @author jricher
 *
 */
public class ClaimSet {

	// the LinkedHashMap preserves insertion order
	private Map<String, Object> claims = new LinkedHashMap<String, Object>();
	
	public ClaimSet() {
		
	}
	
	public ClaimSet(JsonObject json) {
		loadFromJsonObject(json);
	}
	
	public ClaimSet(String b64) {
		loadFromBase64JsonObjectString(b64);
	}
	
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
    	if (prim == null) {
    		// in case we get here with a primitive null
    		claims.put(key, prim);
    	} else if (prim.isBoolean()) {
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
     * Clear all claims from this ClaimSet 
     * @see java.util.Map#clear()
     */
    public void clear() {
	    claims.clear();
    }

    /**
	 * Get a copy of this claim set as a JsonObject. The JsonObject is not
	 * backed by a live copy of this ClaimSet.
	 * @return a copy of the data in this header in a JsonObject
	 */
	public JsonObject getAsJsonObject() {
		JsonObject o = new JsonObject();
		
		
		/*
		 * We step through the claims object and serialize the internal values as 
		 * appropriate to JsonElements. 
		 */
		
		if (this.claims != null) {
			for (Map.Entry<String, Object> claim : this.claims.entrySet()) {
				if (claim.getValue() instanceof JsonElement) {
					// raw JSON elements get passed through directly
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

	/**
	 * Load new claims from the given json object. Will replace any existing claims, but does not clear claim set.  
	 * @param json
	 */
	public void loadFromJsonObject(JsonObject json) {
		for (Entry<String, JsonElement> element : json.entrySet()) {
			if (element.getValue().isJsonNull()) {
				// nulls get stored as java nulls
				setClaim(element.getKey(), null);
			} else if (element.getValue().isJsonPrimitive()){
	        	// we handle all primitives in here
	        	JsonPrimitive prim = element.getValue().getAsJsonPrimitive();
	        	setClaim(element.getKey(), prim);
	        } else {
	        	setClaim(element.getKey(), element.getValue());
	        }
        }
	}

	/**
	 * Load a new claims set from a Base64 encoded JSON Object string
	 */
	public void loadFromBase64JsonObjectString(String b64) {
		byte[] b64decoded = Base64.decodeBase64(b64);
		
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(new InputStreamReader(new ByteArrayInputStream(b64decoded))).getAsJsonObject();
		
		loadFromJsonObject(json);
	}
	
}
