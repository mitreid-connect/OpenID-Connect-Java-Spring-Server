package org.mitre.openid.connect.model;

import java.util.Date;
import java.util.Map.Entry;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mitre.jwt.model.JwtClaims;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Entity
@Table(name="idtokenclaims")
public class IdTokenClaims extends JwtClaims {

	public static final String USER_ID = "user_id";
	public static final String AUTHENTICATION_CONTEXT_CLASS_REFERENCE = "acr";
	public static final String NONCE = "nonce";
	public static final String AUTH_TIME = "auth_time";

	private Long id;
	
	
	
	public IdTokenClaims() {
	    super();
    }

	public IdTokenClaims(JsonObject json) {
	    super(json);
    }

	public IdTokenClaims(String b64) {
	    super(b64);
    }

	/**
     * @return the id
     */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long getId() {
    	return id;
    }
	/**
     * @param id the id to set
     */
    public void setId(Long id) {
    	this.id = id;
    }

    @Transient
	public String getUserId() {
		return getClaimAsString(USER_ID);
	}
	
	public void setUserId(String user_id) {
		setClaim(USER_ID, user_id);
	}
	
	@Transient
	public String getAuthContext() {
		return getClaimAsString(AUTHENTICATION_CONTEXT_CLASS_REFERENCE);		
	}
	
	public void setAuthContext(String acr) {
		setClaim(AUTHENTICATION_CONTEXT_CLASS_REFERENCE, acr);
	}
	
	@Transient
	public String getNonce() {
		return getClaimAsString(NONCE);
	}
	
	public void setNonce(String nonce) {
		setClaim(NONCE, nonce);
	}
	
	@Transient
	public Date getAuthTime() {
		return getClaimAsDate(AUTH_TIME);
	}
	
	public void setAuthTime(Date authTime) {
		setClaim(AUTH_TIME, authTime);
	}
	
	
	/**
	 * Get the seraialized form of this claim set
	 */
	@Basic
    public String getSerializedForm() {
	    // TODO Auto-generated method stub
	    JsonObject o = super.getAsJsonObject();
	    
	    return o.toString();
    }
	
	/**
	 * Set up the claims in this object from the serialized form. This clears all current claims from the object.
	 * @param s a JSON Object string to load into this object
	 * @throws IllegalArgumentException if s is not a valid JSON object string
	 */
	public void setSerializedForm(String s) {
		JsonParser parser = new JsonParser(); 
		JsonElement json = parser.parse(s);
		if (json != null && json.isJsonObject()) {
			loadFromJsonObject(json.getAsJsonObject());
		} else {
			throw new IllegalArgumentException("Could not parse: " + s);
		}
	}
	
	//
	// FIXME: 
	// This doesn't handle loading JsonNull values from the claims set, and this is endemic to the whole claims structure!!!!
	// 
	
	/**
	 * Load this IdToken from a JSON Object
     */
    @Override
    public void loadFromJsonObject(JsonObject json) {
    	JsonObject pass = new JsonObject();
    	
		for (Entry<String, JsonElement> element : json.entrySet()) {
			if (element.getKey().equals(USER_ID)) {
				setUserId(element.getValue().getAsString());
			} else if (element.getKey().equals(AUTHENTICATION_CONTEXT_CLASS_REFERENCE)) {
				setAuthContext(element.getValue().getAsString());
			} else if (element.getKey().equals(NONCE)) {
				setNonce(element.getValue().getAsString());
			} else if (element.getKey().equals(AUTH_TIME)) {
				setAuthTime(new Date(element.getValue().getAsLong() * 1000L));
	        } else {
	        	pass.add(element.getKey(), element.getValue());
	        }
        }    	
    	
	    super.loadFromJsonObject(pass);
    }
}
