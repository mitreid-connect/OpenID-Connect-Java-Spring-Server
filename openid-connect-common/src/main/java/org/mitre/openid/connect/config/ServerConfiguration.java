/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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

	private String registrationEndpointUri;

	private String issuer;

	private String jwksUri;

	private String userInfoUri;

	private String introspectionEndpointUri;

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

	/**
	 * @return the registrationEndpointUri
	 */
	public String getRegistrationEndpointUri() {
		return registrationEndpointUri;
	}

	/**
	 * @param registrationEndpointUri the registrationEndpointUri to set
	 */
	public void setRegistrationEndpointUri(String registrationEndpointUri) {
		this.registrationEndpointUri = registrationEndpointUri;
	}

	/**
	 * @return the introspectionEndpointUri
	 */
	public String getIntrospectionEndpointUri() {
		return introspectionEndpointUri;
	}

	/**
	 * @param introspectionEndpointUri the introspectionEndpointUri to set
	 */
	public void setIntrospectionEndpointUri(String introspectionEndpointUri) {
		this.introspectionEndpointUri = introspectionEndpointUri;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorizationEndpointUri == null) ? 0 : authorizationEndpointUri.hashCode());
		result = prime * result + ((introspectionEndpointUri == null) ? 0 : introspectionEndpointUri.hashCode());
		result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
		result = prime * result + ((jwksUri == null) ? 0 : jwksUri.hashCode());
		result = prime * result + ((registrationEndpointUri == null) ? 0 : registrationEndpointUri.hashCode());
		result = prime * result + ((tokenEndpointUri == null) ? 0 : tokenEndpointUri.hashCode());
		result = prime * result + ((userInfoUri == null) ? 0 : userInfoUri.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ServerConfiguration)) {
			return false;
		}
		ServerConfiguration other = (ServerConfiguration) obj;
		if (authorizationEndpointUri == null) {
			if (other.authorizationEndpointUri != null) {
				return false;
			}
		} else if (!authorizationEndpointUri.equals(other.authorizationEndpointUri)) {
			return false;
		}
		if (introspectionEndpointUri == null) {
			if (other.introspectionEndpointUri != null) {
				return false;
			}
		} else if (!introspectionEndpointUri.equals(other.introspectionEndpointUri)) {
			return false;
		}
		if (issuer == null) {
			if (other.issuer != null) {
				return false;
			}
		} else if (!issuer.equals(other.issuer)) {
			return false;
		}
		if (jwksUri == null) {
			if (other.jwksUri != null) {
				return false;
			}
		} else if (!jwksUri.equals(other.jwksUri)) {
			return false;
		}
		if (registrationEndpointUri == null) {
			if (other.registrationEndpointUri != null) {
				return false;
			}
		} else if (!registrationEndpointUri.equals(other.registrationEndpointUri)) {
			return false;
		}
		if (tokenEndpointUri == null) {
			if (other.tokenEndpointUri != null) {
				return false;
			}
		} else if (!tokenEndpointUri.equals(other.tokenEndpointUri)) {
			return false;
		}
		if (userInfoUri == null) {
			if (other.userInfoUri != null) {
				return false;
			}
		} else if (!userInfoUri.equals(other.userInfoUri)) {
			return false;
		}
		return true;
	}


}
