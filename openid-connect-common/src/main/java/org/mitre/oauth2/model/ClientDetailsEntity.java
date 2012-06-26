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
/**
 * 
 */
package org.mitre.oauth2.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * @author jricher
 * 
 */
@Entity
@Table(name="clientdetails")
@NamedQueries({
	@NamedQuery(name = "ClientDetailsEntity.findAll", query = "SELECT c FROM ClientDetailsEntity c")
})
public class ClientDetailsEntity implements ClientDetails {

	/**
	 * Create a blank ClientDetailsEntity
	 */
	public ClientDetailsEntity() {
		
	}

	public enum AuthType {
		client_secret_post, client_secret_basic, client_secret_jwt, private_key_jwt
	}

    private String clientId = "";
    private String clientSecret = "";
    private Set<String> scope = new HashSet<String>();
    private Set<String> authorizedGrantTypes = new HashSet<String>();
    private Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
    private String clientName = "";
    private String clientDescription = "";
    private boolean allowRefresh = false; // do we allow refresh tokens for this client?
    private Integer accessTokenValiditySeconds = 0; // in seconds
    private Integer refreshTokenValiditySeconds = 0; // in seconds
    private String owner = ""; // userid of who registered it
    private Set<String> registeredRedirectUri = new HashSet<String>();
    private Set<String> resourceIds = new HashSet<String>();
	private Map<String, Object> additionalInformation = new HashMap<String, Object>();

    //Additional properties added by OpenID Connect Dynamic Client Registration spec
	//http://openid.net/specs/openid-connect-registration-1_0.html
	
	/**
	 * List of email addresses for people allowed to administer the information for
	 * this Client. This is used by some providers to enable a web UI to modify the
	 * Client information.
	 */
//	private Set<String> contacts; 	
//	
//	private String applicationType;//native or web
//	private String applicationName;
//	private String logo_url;
//	private Set<String> redirectUris; //Connect allows clients to have more than one redirectUri registered
//	private AuthType tokenEndpointAuthType = AuthType.client_secret_basic;
//	private String policyUrl;
//	private String jwk_url;
//	private String jwk_encryption_url;
//	private String x509Url;
//	private String x509EncryptionUrl;
//	private String sectorIdentifierUrl;
//	private String userIdType;
	
	/** 
	 * OPTIONAL. The JWS [JWS] signature algorithm that MUST be required 
	 * by the Authorization Server. All OpenID Request Objects from 
	 * this client_id MUST be rejected if not signed by this algorithm.
	 */
//	private String requireSignedRequestObject;
//	
//	private String userInfoSignedResponseAlg;
//	private Set<String> userInfoEncryptedResponseAlgs;
//	private String idTokenSignedResponseAlg;
//	private Set<String> idTokenEncryptedResponseAlgs;
	
	//Maximum age for any authentications
//	private Integer defaultMaxAge;
//	
//	private Boolean requireAuthTime;
//	
//	private String defaultACR;
	
	// TODO:
	/*
	private boolean allowMultipleAccessTokens; // do we allow multiple access tokens, or not?
	private boolean reuseRefreshToken; // do we let someone reuse a refresh token?
	*/
	
	/**
     * @return the clientId
     */
	@Id @GeneratedValue
    public String getClientId() {
    	return clientId;
    }

	/**
     * @param clientId The OAuth2 client_id, must be unique to this client 
     */
    public void setClientId(String clientId) {
    	this.clientId = clientId;
    }

	/**
     * @return the clientSecret
     */
	@Basic
	public String getClientSecret() {
    	return clientSecret;
    }

	/**
     * @param clientSecret the OAuth2 client_secret (optional)
     */
    public void setClientSecret(String clientSecret) {
    	this.clientSecret = clientSecret;
    }

	/**
     * @return the scope
     */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="scope",
			joinColumns=@JoinColumn(name="owner_id")
	)
	public Set<String> getScope() {
    	return scope;
    }

	/**
     * @param scope the set of scopes allowed to be issued to this client
     */
    public void setScope(Set<String> scope) {
    	this.scope = scope;
    }

	/**
     * @return the authorizedGrantTypes
     */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="authorizedgranttypes",
			joinColumns=@JoinColumn(name="owner_id")
	)
	public Set<String> getAuthorizedGrantTypes() {
    	return authorizedGrantTypes;
    }

	/**
     * @param authorizedGrantTypes the OAuth2 grant types that this client is allowed to use  
     */
    public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
    	this.authorizedGrantTypes = authorizedGrantTypes;
    }

	/**
     * @return the authorities
     */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="authorities",
			joinColumns=@JoinColumn(name="owner_id")
	)
    public Set<GrantedAuthority> getAuthorities() {
    	return authorities;
    }

	/**
     * @param authorities the Spring Security authorities this client is given
     */
    public void setAuthorities(Set<GrantedAuthority> authorities) {
    	this.authorities = authorities;
    }

	/**
	 * If the clientSecret is not null, then it is always required.
     */
    @Override
    public boolean isSecretRequired() {
	    return getClientSecret() != null;
    }

	/**
	 * If the scope list is not null or empty, then this client has been scoped.
     */
    @Override
    public boolean isScoped() {
    	return getScope() != null && !getScope().isEmpty();
    }

	/**
     * @return the clientName
     */
	@Basic
    public String getClientName() {
    	return clientName;
    }

	/**
     * @param clientName Human-readable name of the client (optional)
     */
    public void setClientName(String clientName) {
    	this.clientName = clientName;
    }

	/**
     * @return the clientDescription
     */
	@Basic
    public String getClientDescription() {
    	return clientDescription;
    }

	/**
     * @param clientDescription Human-readable long description of the client (optional)
     */
    public void setClientDescription(String clientDescription) {
    	this.clientDescription = clientDescription;
    }

	/**
     * @return the allowRefresh
     */
	@Basic
    public boolean isAllowRefresh() {
    	return allowRefresh;
    }

	/**
     * @param allowRefresh Whether to allow for issuance of refresh tokens or not (defaults to false)
     */
    public void setAllowRefresh(boolean allowRefresh) {
    	this.allowRefresh = allowRefresh;
    }

	@Override
	@Basic
	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}
	
	/**
     * @param accessTokenTimeout the accessTokenTimeout to set
     */
    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
    	this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }


	@Override
	@Basic
	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}
	
	/**
     * @param refreshTokenTimeout Lifetime of refresh tokens, in seconds (optional - leave null for no timeout)
     */
    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
    	this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }
	
	//TODO: implement fully with db table or get removed from interface
	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		return this.additionalInformation;
	}
	
	public void setAdditionalInformation(Map<String, Object> map) {
		this.additionalInformation = map;
	}

	/**
     * @return the owner
     */
	@Basic
    public String getOwner() {
    	return owner;
    }

	/**
     * @param owner User ID of the person who registered this client (optional)
     */
    public void setOwner(String owner) {
    	this.owner = owner;
    }

	/**
     * @return the registeredRedirectUri
     */
	//@Basic
    @ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="redirect_uris",
			joinColumns=@JoinColumn(name="owner_id")
	)
    public Set<String> getRegisteredRedirectUri() {
    	return registeredRedirectUri;
    }

	/**
     * @param registeredRedirectUri the registeredRedirectUri to set
     */
    public void setRegisteredRedirectUri(Set<String> registeredRedirectUri) {
    	this.registeredRedirectUri = registeredRedirectUri;
    }

	/**
     * @return the resourceIds
     */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="resource_ids",
			joinColumns=@JoinColumn(name="owner_id")
	)
    public Set<String> getResourceIds() {
    	return resourceIds;
    }

	/**
     * @param resourceIds the resourceIds to set
     */
    public void setResourceIds(Set<String> resourceIds) {
    	this.resourceIds = resourceIds;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return "ClientDetailsEntity [" + (clientId != null ? "clientId=" + clientId + ", " : "") + (scope != null ? "scope=" + scope + ", " : "") + (clientName != null ? "clientName=" + clientName + ", " : "") + (owner != null ? "owner=" + owner : "") + "]";
    }

	/* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
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
	    if (getClass() != obj.getClass()) {
		    return false;
	    }
	    ClientDetailsEntity other = (ClientDetailsEntity) obj;
	    if (clientId == null) {
		    if (other.clientId != null) {
			    return false;
		    }
	    } else if (!clientId.equals(other.clientId)) {
		    return false;
	    }
	    return true;
    }

    public static ClientDetailsEntityBuilder makeBuilder() {
    	return new ClientDetailsEntityBuilder();
    }
    
	public static class ClientDetailsEntityBuilder {
		private ClientDetailsEntity instance;
		
		private ClientDetailsEntityBuilder() {
			instance = new ClientDetailsEntity();
		}

		/**
         * @param clientId
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientId(java.lang.String)
         */
        public ClientDetailsEntityBuilder setClientId(String clientId) {
	        instance.setClientId(clientId);
			return this;
        }

		/**
         * @param clientSecret
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientSecret(java.lang.String)
         */
        public ClientDetailsEntityBuilder setClientSecret(String clientSecret) {
	        instance.setClientSecret(clientSecret);
			return this;
        }

		/**
         * @param scope
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setScope(java.util.List)
         */
        public ClientDetailsEntityBuilder setScope(Set<String> scope) {
	        instance.setScope(scope);
			return this;
        }

		/**
         * @param authorizedGrantTypes
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setAuthorizedGrantTypes(java.util.List)
         */
        public ClientDetailsEntityBuilder setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
	        instance.setAuthorizedGrantTypes(authorizedGrantTypes);
			return this;
        }

		/**
         * @param authorities
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setAuthorities(java.util.List)
         */
        public ClientDetailsEntityBuilder setAuthorities(Set<GrantedAuthority> authorities) {
	        instance.setAuthorities(authorities);
			return this;
        }

		/**
         * @param clientName
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientName(java.lang.String)
         */
        public ClientDetailsEntityBuilder setClientName(String clientName) {
	        instance.setClientName(clientName);
			return this;
        }

		/**
         * @param clientDescription
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientDescription(java.lang.String)
         */
        public ClientDetailsEntityBuilder setClientDescription(String clientDescription) {
	        instance.setClientDescription(clientDescription);
			return this;
        }

		/**
         * @param allowRefresh
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setAllowRefresh(boolean)
         */
        public ClientDetailsEntityBuilder setAllowRefresh(boolean allowRefresh) {
	        instance.setAllowRefresh(allowRefresh);
			return this;
        }

		/**
         * @param accessTokenTimeout
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setAccessTokenTimeout(java.lang.Long)
         */
        public ClientDetailsEntityBuilder setAccessValiditySeconds(int accessTokenValiditySeconds) {
	        instance.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
			return this;
        }

		/**
         * @param refreshTokenTimeout
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setRefreshTokenTimeout(java.lang.Long)
         */
        public ClientDetailsEntityBuilder setRefreshTokenValiditySeconds(int refreshTokenValiditySeconds) {
	        instance.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
			return this;
        }

		/**
         * @param owner
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setOwner(java.lang.String)
         */
        public ClientDetailsEntityBuilder setOwner(String owner) {
	        instance.setOwner(owner);
			return this;
        }
        
        /**
         * Complete the builder
         * @return
         */
		public ClientDetailsEntity finish() {
			return instance;
		}

		/**
         * @param registeredRedirectUri
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setRegisteredRedirectUri(java.lang.String)
         */
        public ClientDetailsEntityBuilder setRegisteredRedirectUri(Set<String> registeredRedirectUri) {
	        instance.setRegisteredRedirectUri(registeredRedirectUri);
	        return this;
        }

		/**
         * @param resourceIds
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setResourceIds(java.util.List)
         */
        public ClientDetailsEntityBuilder setResourceIds(Set<String> resourceIds) {
	        instance.setResourceIds(resourceIds);
	        return this;
        }
		
	}

/*	*//**
	 * @return the contacts
	 *//*
	public Set<String> getContacts() {
		return contacts;
	}

	*//**
	 * @param contacts the contacts to set
	 *//*
	public void setContacts(Set<String> contacts) {
		this.contacts = contacts;
	}

	*//**
	 * @return the applicationType
	 *//*
	public String getApplicationType() {
		return applicationType;
	}

	*//**
	 * @param applicationType the applicationType to set
	 *//*
	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	*//**
	 * @return the applicationName
	 *//*
	public String getApplicationName() {
		return applicationName;
	}

	*//**
	 * @param applicationName the applicationName to set
	 *//*
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	*//**
	 * @return the logo_url
	 *//*
	public String getLogo_url() {
		return logo_url;
	}

	*//**
	 * @param logo_url the logo_url to set
	 *//*
	public void setLogo_url(String logo_url) {
		this.logo_url = logo_url;
	}

	*//**
	 * @return the redirectUris
	 *//*
	public Set<String> getRedirectUris() {
		return redirectUris;
	}

	*//**
	 * @param redirectUris the redirectUris to set
	 *//*
	public void setRedirectUris(Set<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

	*//**
	 * @return the tokenEndpointAuthType
	 *//*
	public AuthType getTokenEndpointAuthType() {
		return tokenEndpointAuthType;
	}

	*//**
	 * @param tokenEndpointAuthType the tokenEndpointAuthType to set
	 *//*
	public void setTokenEndpointAuthType(AuthType tokenEndpointAuthType) {
		this.tokenEndpointAuthType = tokenEndpointAuthType;
	}

	*//**
	 * @return the policyUrl
	 *//*
	public String getPolicyUrl() {
		return policyUrl;
	}

	*//**
	 * @param policyUrl the policyUrl to set
	 *//*
	public void setPolicyUrl(String policyUrl) {
		this.policyUrl = policyUrl;
	}

	*//**
	 * @return the jwk_url
	 *//*
	public String getJwk_url() {
		return jwk_url;
	}

	*//**
	 * @param jwk_url the jwk_url to set
	 *//*
	public void setJwk_url(String jwk_url) {
		this.jwk_url = jwk_url;
	}

	*//**
	 * @return the jwk_encryption_url
	 *//*
	public String getJwk_encryption_url() {
		return jwk_encryption_url;
	}

	*//**
	 * @param jwk_encryption_url the jwk_encryption_url to set
	 *//*
	public void setJwk_encryption_url(String jwk_encryption_url) {
		this.jwk_encryption_url = jwk_encryption_url;
	}

	*//**
	 * @return the x509Url
	 *//*
	public String getX509Url() {
		return x509Url;
	}

	*//**
	 * @param x509Url the x509Url to set
	 *//*
	public void setX509Url(String x509Url) {
		this.x509Url = x509Url;
	}

	*//**
	 * @return the x509EncryptionUrl
	 *//*
	public String getX509EncryptionUrl() {
		return x509EncryptionUrl;
	}

	*//**
	 * @param x509EncryptionUrl the x509EncryptionUrl to set
	 *//*
	public void setX509EncryptionUrl(String x509EncryptionUrl) {
		this.x509EncryptionUrl = x509EncryptionUrl;
	}

	*//**
	 * @return the sectorIdentifierUrl
	 *//*
	public String getSectorIdentifierUrl() {
		return sectorIdentifierUrl;
	}

	*//**
	 * @param sectorIdentifierUrl the sectorIdentifierUrl to set
	 *//*
	public void setSectorIdentifierUrl(String sectorIdentifierUrl) {
		this.sectorIdentifierUrl = sectorIdentifierUrl;
	}

	*//**
	 * @return the userIdType
	 *//*
	public String getUserIdType() {
		return userIdType;
	}

	*//**
	 * @param userIdType the userIdType to set
	 *//*
	public void setUserIdType(String userIdType) {
		this.userIdType = userIdType;
	}

	*//**
	 * @return the requireSignedRequestObject
	 *//*
	public String getRequireSignedRequestObject() {
		return requireSignedRequestObject;
	}

	*//**
	 * @param requireSignedRequestObject the requireSignedRequestObject to set
	 *//*
	public void setRequireSignedRequestObject(String requireSignedRequestObject) {
		this.requireSignedRequestObject = requireSignedRequestObject;
	}

	*//**
	 * @return the userInfoSignedResponseAlg
	 *//*
	public String getUserInfoSignedResponseAlg() {
		return userInfoSignedResponseAlg;
	}

	*//**
	 * @param userInfoSignedResponseAlg the userInfoSignedResponseAlg to set
	 *//*
	public void setUserInfoSignedResponseAlg(String userInfoSignedResponseAlg) {
		this.userInfoSignedResponseAlg = userInfoSignedResponseAlg;
	}

	*//**
	 * @return the userInfoEncryptedResponseAlgs
	 *//*
	public Set<String> getUserInfoEncryptedResponseAlgs() {
		return userInfoEncryptedResponseAlgs;
	}

	*//**
	 * @param userInfoEncryptedResponseAlgs the userInfoEncryptedResponseAlgs to set
	 *//*
	public void setUserInfoEncryptedResponseAlgs(
			Set<String> userInfoEncryptedResponseAlgs) {
		this.userInfoEncryptedResponseAlgs = userInfoEncryptedResponseAlgs;
	}

	*//**
	 * @return the idTokenEncryptedResponseAlgs
	 *//*
	public Set<String> getIdTokenEncryptedResponseAlgs() {
		return idTokenEncryptedResponseAlgs;
	}

	*//**
	 * @param idTokenEncryptedResponseAlgs the idTokenEncryptedResponseAlgs to set
	 *//*
	public void setIdTokenEncryptedResponseAlgs(
			Set<String> idTokenEncryptedResponseAlgs) {
		this.idTokenEncryptedResponseAlgs = idTokenEncryptedResponseAlgs;
	}

	*//**
	 * @return the idTokenSignedResponseAlg
	 *//*
	public String getIdTokenSignedResponseAlg() {
		return idTokenSignedResponseAlg;
	}

	*//**
	 * @param idTokenSignedResponseAlg the idTokenSignedResponseAlg to set
	 *//*
	public void setIdTokenSignedResponseAlg(String idTokenSignedResponseAlg) {
		this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
	}

	*//**
	 * @return the defaultMaxAge
	 *//*
	public Integer getDefaultMaxAge() {
		return defaultMaxAge;
	}

	*//**
	 * @param defaultMaxAge the defaultMaxAge to set
	 *//*
	public void setDefaultMaxAge(Integer defaultMaxAge) {
		this.defaultMaxAge = defaultMaxAge;
	}

	*//**
	 * @return the requireAuthTime
	 *//*
	public Boolean getRequireAuthTime() {
		return requireAuthTime;
	}

	*//**
	 * @param requireAuthTime the requireAuthTime to set
	 *//*
	public void setRequireAuthTime(Boolean requireAuthTime) {
		this.requireAuthTime = requireAuthTime;
	}

	*//**
	 * @return the defaultACR
	 *//*
	public String getDefaultACR() {
		return defaultACR;
	}

	*//**
	 * @param defaultACR the defaultACR to set
	 *//*
	public void setDefaultACR(String defaultACR) {
		this.defaultACR = defaultACR;
	}
*/
}
