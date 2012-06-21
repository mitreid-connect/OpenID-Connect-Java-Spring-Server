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

/**
 * @author nemonik
 * 
 */
public class OIDCServerConfiguration {

	private String authorizationEndpointURI;

	private String tokenEndpointURI;

	private String clientSecret;

	private String clientId;
	
	private String issuer;
	
	private String x509EncryptUrl;
	
	private String x509SigningUrl;
	
	private String jwkEncryptUrl;
	
	private String jwkSigningUrl;

	public String getAuthorizationEndpointURI() {
		return authorizationEndpointURI;
	}

	public String getClientId() {
		return clientId;
	}
	
	public String getIssuer() {
		return issuer;
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
 
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
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

	@Override
	public String toString() {
		return "OIDCServerConfiguration [authorizationEndpointURI="
				+ authorizationEndpointURI + ", tokenEndpointURI="
				+ tokenEndpointURI + ", clientSecret=" + clientSecret
				+ ", clientId=" + clientId + ", issuer=" + issuer 
				+", x509EncryptedUrl=" 
				+ x509EncryptUrl + ", jwkEncryptedUrl=" 
				+ jwkEncryptUrl + ", x509SigningUrl="
				+ x509SigningUrl + ", jwkSigningUrl="
				+ jwkSigningUrl + "]";
	}

}