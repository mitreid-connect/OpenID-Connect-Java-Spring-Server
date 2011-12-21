package org.mitre.jwt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JwtClaims {
	
	/**
	 * ISO8601 / RFC3339 Date Format
	 */
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

	/*
	 * TODO: Should we instead be using a generic claims map with well-named accessor methods?
	 */
	
	private Date expiration;
	
	private Date notBefore;
	
	private Date issuedAt;
	
	private String issuer;
	
	private String audience;
	
	private String principal;
	
	private String jwtId;
	
	private String type;
	
	private Map<String, Object> claims = new HashMap<String, Object>();

	public JwtClaims() {
		
	}
	
	public JwtClaims(JsonObject json) {
		for (Entry<String, JsonElement> element : json.entrySet()) {
	        if (element.getKey().equals("exp")) {
                expiration = new Date(element.getValue().getAsLong() * 1000L);
	        } else if (element.getKey().equals("nbf")) {
                notBefore = new Date(element.getValue().getAsLong() * 1000L);
	        } else if (element.getKey().equals("iat")) {	        	
                issuedAt = new Date(element.getValue().getAsLong() * 1000L);
	        } else if (element.getKey().equals("iss")) {	        	
	        	issuer = element.getValue().getAsString();
	        } else if (element.getKey().equals("aud")) {	        	
	        	audience = element.getValue().getAsString();
	        } else if (element.getKey().equals("prn")) {	        	
	        	principal = element.getValue().getAsString();
	        } else if (element.getKey().equals("jti")) {	        	
	        	jwtId = element.getValue().getAsString();
	        } else if (element.getKey().equals("typ")) {	        	
	        	type = element.getValue().getAsString();
	        } else if (element.getValue().isJsonPrimitive()){
	        	// we handle all primitives in here
	        	JsonPrimitive prim = element.getValue().getAsJsonPrimitive();
	        	
	        	if (prim.isBoolean()) {
	        		claims.put(element.getKey(), prim.getAsBoolean());
	        	} else if (prim.isNumber()) {
	        		claims.put(element.getKey(), prim.getAsNumber());
	        	} else if (prim.isString()) {
	        		claims.put(element.getKey(), prim.getAsString());
	        	}
	        } else {
	        	// everything else gets handled as a raw JsonElement
	        	claims.put(element.getKey(), element.getValue());
	        }
        }
    }

	/**
     * @return the expiration
     */
    public Date getExpiration() {
    	return expiration;
    }

	/**
     * @param expiration the expiration to set
     */
    public void setExpiration(Date expiration) {
    	this.expiration = expiration;
    }

	/**
     * @return the notBefore
     */
    public Date getNotBefore() {
    	return notBefore;
    }

	/**
     * @param notBefore the notBefore to set
     */
    public void setNotBefore(Date notBefore) {
    	this.notBefore = notBefore;
    }

	/**
     * @return the issuedAt
     */
    public Date getIssuedAt() {
    	return issuedAt;
    }

	/**
     * @param issuedAt the issuedAt to set
     */
    public void setIssuedAt(Date issuedAt) {
    	this.issuedAt = issuedAt;
    }

	/**
     * @return the issuer
     */
    public String getIssuer() {
    	return issuer;
    }

	/**
     * @param issuer the issuer to set
     */
    public void setIssuer(String issuer) {
    	this.issuer = issuer;
    }

	/**
     * @return the audience
     */
    public String getAudience() {
    	return audience;
    }

	/**
     * @param audience the audience to set
     */
    public void setAudience(String audience) {
    	this.audience = audience;
    }

	/**
     * @return the principal
     */
    public String getPrincipal() {
    	return principal;
    }

	/**
     * @param principal the principal to set
     */
    public void setPrincipal(String principal) {
    	this.principal = principal;
    }

	/**
     * @return the jwtId
     */
    public String getJwtId() {
    	return jwtId;
    }

	/**
     * @param jwtId the jwtId to set
     */
    public void setJwtId(String jwtId) {
    	this.jwtId = jwtId;
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

		if (this.expiration != null) {
			o.addProperty("exp", this.expiration.getTime() / 1000L);
		}
		
		if (this.notBefore != null) {
			o.addProperty("nbf", this.notBefore.getTime() / 1000L);
		}
		
		if (this.issuedAt != null) {
			o.addProperty("iat", this.issuedAt.getTime() / 1000L);
		}
		
		if (this.issuer != null) {
			o.addProperty("iss", this.issuer);
		}
		
		if (this.audience != null) {
			o.addProperty("aud", this.audience);
		}
		
		if (this.principal != null) {
			o.addProperty("prn", this.principal);
		}
		
		if (this.jwtId != null) {
			o.addProperty("jti", this.jwtId);
		}
		
		if (this.type != null) {
			o.addProperty("typ", this.type);
		}
		
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

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return "JwtClaims [expiration=" + expiration + ", notBefore=" + notBefore + ", issuedAt=" + issuedAt + ", issuer=" + issuer + ", audience=" + audience + ", principal=" + principal + ", jwtId=" + jwtId + ", type=" + type + ", claims=" + claims + "]";
    }



}
