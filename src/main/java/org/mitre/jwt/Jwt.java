package org.mitre.jwt;

import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Jwt {

	private JwtHeader header = new JwtHeader();
	
	private JwtClaims claims = new JwtClaims();
	
	private String signature;

	
	

	/**
     * @return the header
     */
    public JwtHeader getHeader() {
    	return header;
    }



	/**
     * @param header the header to set
     */
    public void setHeader(JwtHeader header) {
    	this.header = header;
    }



	/**
     * @return the claims
     */
    public JwtClaims getClaims() {
    	return claims;
    }



	/**
     * @param claims the claims to set
     */
    public void setClaims(JwtClaims claims) {
    	this.claims = claims;
    }



	/**
     * @return the signature
     */
    public String getSignature() {
    	return signature;
    }



	/**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
    	this.signature = signature;
    }
	
	/**
	 * Return the canonical encoded string of this JWT
	 */
	public String toString() {
		JsonObject h = header.getAsJsonObject();
		JsonObject o = claims.getAsJsonObject();

		String h64 = new String(Base64.encodeBase64URLSafe(h.toString().getBytes()));
		String o64 = new String(Base64.encodeBase64(o.toString().getBytes()));
		
		return h64 + "." + o64 + "." + Strings.nullToEmpty(this.signature);		
	}


	/**
	 * Parse a wire-encoded JWT
	 */
	public static Jwt parse(String s) {
		
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(s));
		
		if (parts.size() != 3) {
			throw new IllegalArgumentException("Invalid JWT format.");
		}
		
		String h64 = parts.get(0);
		String o64 = parts.get(1);
		String s64 = parts.get(2);
		
		JsonParser parser = new JsonParser();
		
		
		
		// shuttle for return value
		Jwt jwt = new Jwt();
		
		return jwt;
		
	}
	
}
