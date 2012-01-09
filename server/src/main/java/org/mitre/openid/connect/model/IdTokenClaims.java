package org.mitre.openid.connect.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;

/*
 * TODO: This class needs to be encoded as a JWT
 */
@Entity
public class IdTokenClaims extends JwtClaims {

	public static final String USER_ID = "user_id";
	public static final String AUTHENTICATION_CONTEXT_CLASS_REFERENCE = "acr";
	public static final String NONCE = "nonce";
	public static final String AUTH_TIME = "auth_time";

	private Long id;
	
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


	public String getUserId() {
		return getClaimAsString(USER_ID);
	}
	
	public void setUserId(String user_id) {
		setClaim(USER_ID, user_id);
	}
	

	public String getAuthContext() {
		return getClaimAsString(AUTHENTICATION_CONTEXT_CLASS_REFERENCE);		
	}
	
	public void setAuthContext(String acr) {
		setClaim(AUTHENTICATION_CONTEXT_CLASS_REFERENCE, acr);
	}
	
	
	public String getNonce() {
		return getClaimAsString(NONCE);
	}
	
	public void setNonce(String nonce) {
		setClaim(NONCE, nonce);
	}
	
	
	public Date getAuthTime() {
		return getClaimAsDate(AUTH_TIME);
	}
	
	public void setAuthTime(Date authTime) {
		setClaim(AUTH_TIME, authTime);
	}
	
}
