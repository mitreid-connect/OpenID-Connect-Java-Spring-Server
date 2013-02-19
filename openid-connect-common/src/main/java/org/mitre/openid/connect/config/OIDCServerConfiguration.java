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

import javax.persistence.Basic;

import com.nimbusds.jose.JWSAlgorithm;


/**
 * @author nemonik
 * 
 */
public class OIDCServerConfiguration {

	private String authorizationEndpointUrl;

	private String tokenEndpointUrl;

	private String clientSecret;

	private String clientId;
	
	private String issuer;
	
	private String x509EncryptUrl;
	
	private String x509SigningUrl;
	
	private String jwkEncryptUrl;
	
	private String jwkSigningUrl;
	
	private String userInfoUrl;
	
	private JWSAlgorithm signingAlgorithm;

	public String getAuthorizationEndpointUrl() {
		return authorizationEndpointUrl;
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

	public String getTokenEndpointUrl() {
		return tokenEndpointUrl;
	} 

	public void setAuthorizationEndpointUrl(String authorizationEndpointURI) {
		this.authorizationEndpointUrl = authorizationEndpointURI;
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

	public void setTokenEndpointUrl(String tokenEndpointURI) {
		this.tokenEndpointUrl = tokenEndpointURI;
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

	/**
     * @return the userInfoUrl
     */
    public String getUserInfoUrl() {
	    return userInfoUrl;
    }

	/**
     * @param userInfoUrl the userInfoUrl to set
     */
    public void setUserInfoUrl(String userInfoUrl) {
	    this.userInfoUrl = userInfoUrl;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return "OIDCServerConfiguration [authorizationEndpointUrl=" + authorizationEndpointUrl + ", tokenEndpointUrl=" + tokenEndpointUrl + ", clientSecret=" + clientSecret + ", clientId=" + clientId + ", issuer=" + issuer + ", x509EncryptUrl=" + x509EncryptUrl + ", x509SigningUrl="
	            + x509SigningUrl + ", jwkEncryptUrl=" + jwkEncryptUrl + ", jwkSigningUrl=" + jwkSigningUrl + ", userInfoUrl=" + userInfoUrl + "]";
    }

	/**
	 * @return the signingAlgorithm
	 */
	public JWSAlgorithm getSigningAlgorithm() {
		return signingAlgorithm;
	}

	/**
	 * @param signingAlgorithm the signingAlgorithm to set
	 */
	public void setSigningAlgorithm(JWSAlgorithm signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}

	/**
	 * Get the name of this algorithm, return null if no algorithm set.
	 * @return
	 */
	@Basic
	public String getAlgorithmName() {
		if (signingAlgorithm != null) {
			return signingAlgorithm.getName();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the name of this algorithm. 
	 * Calls JWSAlgorithm.parse()
	 * @param algorithmName
	 */
	public void setAlgorithmName(String algorithmName) {
		if (algorithmName != null) {
			signingAlgorithm = JWSAlgorithm.parse(algorithmName);
		} else {
			signingAlgorithm = null;
		}
	}

}