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
package org.mitre.openid.connect.config;



/**
 * 
 * Container class for a client's view of a server's configuration
 * 
 * @author nemonik, jricher
 * 
 */
public class ServerConfiguration {

	private String authorizationEndpointUri;

	private String tokenEndpointUri;

	private String issuer;
	
	private String jwksUri;
	
	private String userInfoUri;

	/**
	 * @return the authorizationEndpointUri
	 */
	public String getAuthorizationEndpointUri() {
		return authorizationEndpointUri;
	}

	/**
	 * @param authorizationEndpointUri the authorizationEndpointUri to set
	 */
	public void setAuthorizationEndpointUri(String authorizationEndpointUri) {
		this.authorizationEndpointUri = authorizationEndpointUri;
	}

	/**
	 * @return the tokenEndpointUri
	 */
	public String getTokenEndpointUri() {
		return tokenEndpointUri;
	}

	/**
	 * @param tokenEndpointUri the tokenEndpointUri to set
	 */
	public void setTokenEndpointUri(String tokenEndpointUri) {
		this.tokenEndpointUri = tokenEndpointUri;
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
	 * @return the jwksUri
	 */
	public String getJwksUri() {
		return jwksUri;
	}

	/**
	 * @param jwksUri the jwksUri to set
	 */
	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
	}

	/**
	 * @return the userInfoUri
	 */
	public String getUserInfoUri() {
		return userInfoUri;
	}

	/**
	 * @param userInfoUri the userInfoUri to set
	 */
	public void setUserInfoUri(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}
	
}