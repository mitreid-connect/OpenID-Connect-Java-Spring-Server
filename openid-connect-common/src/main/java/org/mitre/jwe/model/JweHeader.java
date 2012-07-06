package org.mitre.jwe.model;

import java.util.Map.Entry;

import org.mitre.jwt.model.JwtHeader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JweHeader extends JwtHeader{
	
	public static final String INTEGRITY = "int";
	public static final String INITIALIZATION_VECTOR = "iv";
	public static final String EPHEMERAL_PUBLIC_KEY = "epk";
	public static final String COMPRESSION_ALGORITHM = "zip";
	public static final String JSON_SET_URL = "jku";
	public static final String JSON_WEB_KEY = "jwk";
	public static final String X509_URL = "x5u";
	public static final String X509_CERTIFICATE_THUMBPRINT = "x5t";
	public static final String X509_CERTIFICATE_CHAIN = "x5c";
	public static final String KEY_ID = "kid";
	public static final String KEY_DERIVATION_FUNCTION = "kdf";
	public static final String CONTENT_TYPE = "cty";

	public JweHeader(){
		super();
	}
	
	public JweHeader(JsonObject object){
		super(object);
	}
	
	public JweHeader(String b64) {
		super(b64);
    }
	
	/**
	 * Load all claims from the given json object into this object
     */
    @Override
    public void loadFromJsonObject(JsonObject json) {
    	
    	JsonObject pass = new JsonObject();
    	
		for (Entry<String, JsonElement> element : json.entrySet()) {
			if (element.getValue().isJsonNull()) {
				pass.add(element.getKey(), element.getValue());
			}  else if (element.getKey().equals(INTEGRITY)) {
	        	this.setIntegrity(json.get(INTEGRITY).getAsString());
	        } else if (element.getKey().equals(INITIALIZATION_VECTOR)) {	        	
	        	this.setIv(json.get(INITIALIZATION_VECTOR).getAsString());
	        } else if (element.getKey().equals(EPHEMERAL_PUBLIC_KEY)) {
	        	this.setEphemeralPublicKey(json.get(EPHEMERAL_PUBLIC_KEY).getAsString());
	        } else if (element.getKey().equals(COMPRESSION_ALGORITHM)) {
	        	this.setCompressionAlgorithm(json.get(COMPRESSION_ALGORITHM).getAsString());
	        } else if (element.getKey().equals(JSON_SET_URL)) {
	        	this.setJku(json.get(JSON_SET_URL).getAsString());
	        } else if (element.getKey().equals(JSON_WEB_KEY)) {
	        	this.setJsonWebKey(json.get(JSON_WEB_KEY).getAsString());
	        } else if (element.getKey().equals(X509_URL)) {
	        	this.setX509Url(json.get(X509_URL).getAsString());
	        } else if (element.getKey().equals(X509_CERTIFICATE_THUMBPRINT)) {
	        	this.setX509CertThumbprint(json.get(X509_CERTIFICATE_THUMBPRINT).getAsString());
	        } else if (element.getKey().equals(X509_CERTIFICATE_CHAIN)) {
	        	this.setX509CertChain(json.get(X509_CERTIFICATE_CHAIN).getAsString());
	        } else if (element.getKey().equals(KEY_ID)) {
	        	this.setKeyId(json.get(KEY_ID).getAsString());
	        } else {
	        	pass.add(element.getKey(), element.getValue());
	        }
        }
		super.loadFromJsonObject(pass);
	}
	
	public String getIntegrity() {
		return INTEGRITY;
	}

	public String getInitializationVector() {
		return INITIALIZATION_VECTOR;
	}

	public String getEphemeralPublicKey() {
		return EPHEMERAL_PUBLIC_KEY;
	}

	public String getCompressionAlgorithm() {
		return COMPRESSION_ALGORITHM;
	}

	public String getJsonSetUrl() {
		return JSON_SET_URL;
	}

	public String getJsonWebKey() {
		return JSON_WEB_KEY;
	}

	public String getX509Url() {
		return X509_URL;
	}

	public String getX509CertificateThumbprint() {
		return X509_CERTIFICATE_THUMBPRINT;
	}

	public String getX509CertificateChain() {
		return X509_CERTIFICATE_CHAIN;
	}

	public String getKeyId() {
		return KEY_ID;
	}
	
	public static String getKeyDerivationFunction() {
		return KEY_DERIVATION_FUNCTION;
	}

	public static String getContentType() {
		return CONTENT_TYPE;
	}

	public void setIv(String iv) {
		setClaim(INITIALIZATION_VECTOR, iv);
	}
	
	public void setJku(String jku) {
		setClaim(JSON_SET_URL, jku);
	}
	
	public void setIntegrity(String integrity) {
		setClaim(INTEGRITY, integrity);
	}
	
	public void setEphemeralPublicKey(String epk) {
		setClaim(EPHEMERAL_PUBLIC_KEY, epk);
	}
	
	public void setCompressionAlgorithm(String zip) {
		setClaim(COMPRESSION_ALGORITHM, zip);
	}
	
	public void setJsonWebKey(String jwk) {
		setClaim(JSON_WEB_KEY, jwk);
	}
	
	public void setX509Url(String x5u) {
		setClaim(X509_URL, x5u);
	}
	
	public void setX509CertThumbprint(String x5t) {
		setClaim(X509_CERTIFICATE_THUMBPRINT, x5t);
	}
	
	public void setX509CertChain(String x5c) {
		setClaim(X509_CERTIFICATE_CHAIN, x5c);
	}
	
	public void setKeyId(String kid) {
		setClaim(KEY_ID, kid);
	}
	
	public void setKeyDerivationFunction(String kdf) {
		setClaim(KEY_DERIVATION_FUNCTION, kdf);
	}
	
	public void setContentType(String cty) {
		setClaim(CONTENT_TYPE, cty);
	}
}
