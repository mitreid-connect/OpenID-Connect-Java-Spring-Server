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

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class JwtClaims extends ClaimSet {
	
	public static final String TYPE = "typ";
	public static final String JWT_ID = "jti";
	public static final String SUBJECT = "sub";
	public static final String AUDIENCE = "aud";
	public static final String ISSUER = "iss";
	public static final String ISSUED_AT = "iat";
	public static final String NOT_BEFORE = "nbf";
	public static final String EXPIRATION = "exp";
	public static final String NONCE = "nonce";

	/**
	 * ISO8601 / RFC3339 Date Format
	 */
	//public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

	public JwtClaims() {
		super();
	}
	
	public JwtClaims(JsonObject json) {
		super(json);
	}
	
	public JwtClaims(String b64) {
		super(b64);
	}

	public JwtClaims(JwtClaims jwtClaims) {
		super(jwtClaims);
	}
	
	@Override
	public void loadFromJsonObject(JsonObject json) {
		JsonObject pass = new JsonObject();
		
		for (Entry<String, JsonElement> element : json.entrySet()) {
			if (element.getValue().isJsonNull()) {
				pass.add(element.getKey(), element.getValue());
			} else if (element.getKey().equals(EXPIRATION)) {
                setExpiration(new Date(element.getValue().getAsLong() * 1000L));
	        } else if (element.getKey().equals(NOT_BEFORE)) {
                setNotBefore(new Date(element.getValue().getAsLong() * 1000L));
	        } else if (element.getKey().equals(ISSUED_AT)) {	        	
                setIssuedAt(new Date(element.getValue().getAsLong() * 1000L));
	        } else if (element.getKey().equals(ISSUER)) {	        	
	        	setIssuer(element.getValue().getAsString());
	        } else if (element.getKey().equals(AUDIENCE)) {
	        	if (element.getValue().isJsonArray()) {
	        		// it's an array of strings, set it as such
	        		//setAudience(element.getValue().getAsJsonArray());
	        		Type collectionType = new TypeToken<List<String>>(){}.getType();
	        		List<String> values = new Gson().fromJson(element.getValue(), collectionType);
	        		setAudience(values);
	        	} else {
	        		// it's a single value
	        		setAudience(element.getValue().getAsString());
	        	}
	        } else if (element.getKey().equals(SUBJECT)) {	        	
	        	setSubject(element.getValue().getAsString());
	        } else if (element.getKey().equals(JWT_ID)) {	        	
	        	setJwtId(element.getValue().getAsString());
	        } else if (element.getKey().equals(TYPE)) {	        	
	        	setType(element.getValue().getAsString());
	        } else if (element.getKey().equals(NONCE)){
	        	setNonce(element.getValue().getAsString());
	        }else {
	        	pass.add(element.getKey(), element.getValue());
	        }
        }
		
		// load all the generic claims into this object
		super.loadFromJsonObject(pass);
    }

	/**
     * @return the expiration
     */
    public Date getExpiration() {
    	return getClaimAsDate(EXPIRATION);
    }

	/**
     * @param expiration the expiration to set
     */
    public void setExpiration(Date expiration) {
    	setClaim(EXPIRATION, expiration);
    }

	/**
     * @return the notBefore
     */
    public Date getNotBefore() {
    	return getClaimAsDate(NOT_BEFORE);
    }

	/**
     * @param notBefore the notBefore to set
     */
    public void setNotBefore(Date notBefore) {
    	setClaim(NOT_BEFORE, notBefore);
    }

	/**
     * @return the issuedAt
     */
    public Date getIssuedAt() {
    	return getClaimAsDate(ISSUED_AT);
    }

	/**
     * @param issuedAt the issuedAt to set
     */
    public void setIssuedAt(Date issuedAt) {
    	setClaim(ISSUED_AT, issuedAt);
    }

	/**
     * @return the issuer
     */
    public String getIssuer() {
    	return getClaimAsString(ISSUER);
    }

	/**
     * @param issuer the issuer to set
     */
    public void setIssuer(String issuer) {
    	setClaim(ISSUER, issuer);
    }

    /**
     * @return the audience
     */
    public List<String> getAudience() {
    	return (List<String>) getClaimAsList(AUDIENCE);
    }

	/**
     * @param audience the audience to set
     */
    public void setAudience(String audience) {
    	setClaim(AUDIENCE, Lists.newArrayList(audience));
    }

    
    public void setAudience(List<String> audience) {
    	setClaim(AUDIENCE, audience);
    }
    
	/**
     * @return the principal
     */
    public String getSubject() {
    	return getClaimAsString(SUBJECT);
    }

	/**
     * @param principal the principal to set
     */
    public void setSubject(String principal) {
    	setClaim(SUBJECT, principal);
    }

	/**
     * @return the jwtId
     */
    public String getJwtId() {
    	return getClaimAsString(JWT_ID);
    }

	/**
     * @param jwtId the jwtId to set
     */
    public void setJwtId(String jwtId) {
    	setClaim(JWT_ID, jwtId);
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
     * @return the nonce
     */
    public String getNonce() {
    	return getClaimAsString(NONCE);
    }
    
    /**
     * @param nonce the nonce to set
     */
    public void setNonce(String nonce) {
    	setClaim(NONCE, nonce);
    }

	/* (non-Javadoc)
     * @see org.mitre.jwt.model.ClaimSet#getAsJsonObject()
     */
    @Override
    public JsonObject getAsJsonObject() {
	    JsonObject o = super.getAsJsonObject();
	    
	    // special handling for audience claim
	    if (o.has(AUDIENCE) && o.get(AUDIENCE).isJsonArray()) {
	    	JsonArray aud = o.get(AUDIENCE).getAsJsonArray();
	    	// overwrite single-sized arrays as a string
	    	if (aud.size() == 1) {
	    		o.addProperty(AUDIENCE, aud.get(0).getAsString());
	    	}
	    }
	    
	    return o;
    }

}
