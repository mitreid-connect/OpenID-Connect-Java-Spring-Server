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
package org.mitre.openid.connect.client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;

import org.mitre.util.Utility;

import com.google.gson.JsonObject;

/**
 * @author nemonik
 * 
 */
public class OIDCServerConfiguration {

	private String authorizationEndpointURI;

	private String tokenEndpointURI;

	private String checkIDEndpointURI;

	private String clientSecret;

	private String clientId;
	
	private String x509EncryptUrl;
	
	private String x509SigningUrl;
	
	private String jwkEncryptUrl;
	
	private String jwkSigningUrl;
	
	private Key encryptKey;
	
	private Key signingKey;

	public String getAuthorizationEndpointURI() {
		return authorizationEndpointURI;
	}

	public String getCheckIDEndpointURI() {
		return checkIDEndpointURI;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getTokenEndpointURI() {
		return tokenEndpointURI;
	}

	public void setAuthorizationEndpointURI(String authorizationEndpointURI) {
		this.authorizationEndpointURI = authorizationEndpointURI;
	}

	public void setCheckIDEndpointURI(String checkIDEndpointURI) {
		this.checkIDEndpointURI = checkIDEndpointURI;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setTokenEndpointURI(String tokenEndpointURI) {
		this.tokenEndpointURI = tokenEndpointURI;
	}
	
	public String getX509EncryptUrl() {
		return x509EncryptUrl;
	}
	
	public String getX509SigningUrl() {
		return x509SigningUrl;
	}
	
	public String getJwkEncryptUrl() {
		return jwkEncryptUrl;
	}
	
	public String getJwkSigningUrl() {
		return jwkSigningUrl;
	}
	
	public void setX509EncryptUrl(String x509EncryptUrl) {
		this.x509EncryptUrl = x509EncryptUrl;
	}
	
	public void setX509SigningUrl(String x509SigningUrl) {
		this.x509SigningUrl = x509SigningUrl;
	}
	
	public void setJwkEncryptUrl(String jwkEncryptUrl) {
		this.jwkEncryptUrl = jwkEncryptUrl;
	}
	
	public void setJwkSigningUrl(String jwkSigningUrl) {
		this.jwkSigningUrl = jwkSigningUrl;
	}
	
	public Key getSigningKey() throws Exception {
		if(signingKey == null){
			if(x509SigningUrl != null){
				File file = new File(x509SigningUrl);
				URL url = file.toURI().toURL();
				signingKey = Utility.retrieveX509Key(url);
			}
			else if (jwkSigningUrl != null){
				File file = new File(jwkSigningUrl);
				URL url = file.toURI().toURL();
				signingKey = Utility.retrieveJwkKey(url);
			}
		}
		return signingKey;
	}
	
	public Key getEncryptionKey() throws Exception {
		if(encryptKey == null){
			if(x509EncryptUrl != null){
				File file = new File(x509EncryptUrl);
				URL url = file.toURI().toURL();
				encryptKey = Utility.retrieveX509Key(url);
			}
			else if (jwkEncryptUrl != null){
				File file = new File(jwkEncryptUrl);
				URL url = file.toURI().toURL();
				encryptKey = Utility.retrieveJwkKey(url);
			}
		}
		return encryptKey;
	}
	
	public void checkKeys() throws Exception {
		encryptKey = null;
		signingKey = null;
		getEncryptionKey();
		getSigningKey();
	}

	@Override
	public String toString() {
		return "OIDCServerConfiguration [authorizationEndpointURI="
				+ authorizationEndpointURI + ", tokenEndpointURI="
				+ tokenEndpointURI + ", checkIDEndpointURI="
				+ checkIDEndpointURI + ", clientSecret=" + clientSecret
				+ ", clientId=" + clientId + ", x509EncryptedUrl=" 
				+ x509EncryptUrl + ", jwkEncryptedUrl=" 
				+ jwkEncryptUrl + ", x509SigningUrl="
				+ x509SigningUrl + ", jwkSigningUrl="
				+ jwkSigningUrl + "]";
	}

}
