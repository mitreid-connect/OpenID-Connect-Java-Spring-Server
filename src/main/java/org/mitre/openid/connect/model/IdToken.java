package org.mitre.openid.connect.model;

import javax.persistence.Entity;

/*
 * TODO: This class needs to be encoded as a JWT
 */
@Entity
public class IdToken {

	private String iss;
	private String user_id;
	private String aud;
	private String exp;
	private String iso29115;
	private String nonce;
	private String auth_time;
	/**
	 * @return the iss
	 */
	public String getIss() {
		return iss;
	}
	/**
	 * @param iss the iss to set
	 */
	public void setIss(String iss) {
		this.iss = iss;
	}
	/**
	 * @return the user_id
	 */
	public String getUser_id() {
		return user_id;
	}
	/**
	 * @param user_id the user_id to set
	 */
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	/**
	 * @return the aud
	 */
	public String getAud() {
		return aud;
	}
	/**
	 * @param aud the aud to set
	 */
	public void setAud(String aud) {
		this.aud = aud;
	}
	/**
	 * @return the exp
	 */
	public String getExp() {
		return exp;
	}
	/**
	 * @param exp the exp to set
	 */
	public void setExp(String exp) {
		this.exp = exp;
	}
	/**
	 * @return the iso29115
	 */
	public String getIso29115() {
		return iso29115;
	}
	/**
	 * @param iso29115 the iso29115 to set
	 */
	public void setIso29115(String iso29115) {
		this.iso29115 = iso29115;
	}
	/**
	 * @return the nonce
	 */
	public String getNonce() {
		return nonce;
	}
	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	/**
	 * @return the auth_time
	 */
	public String getAuth_time() {
		return auth_time;
	}
	/**
	 * @param auth_time the auth_time to set
	 */
	public void setAuth_time(String auth_time) {
		this.auth_time = auth_time;
	}
	
}
