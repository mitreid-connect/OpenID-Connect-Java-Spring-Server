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

	public String getAuthorizationEndpointURI() {
		return authorizationEndpointURI;
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
 
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setTokenEndpointURI(String tokenEndpointURI) {
		this.tokenEndpointURI = tokenEndpointURI;
	}

	@Override
	public String toString() {
		return "OIDCServerConfiguration [authorizationEndpointURI="
				+ authorizationEndpointURI + ", tokenEndpointURI="
				+ tokenEndpointURI + ", clientSecret=" + clientSecret
				+ ", clientId=" + clientId + "]";
	}

}