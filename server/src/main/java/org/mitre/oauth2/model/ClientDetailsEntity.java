/**
 * 
 */
package org.mitre.oauth2.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

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

	private String clientId;	
	private String clientSecret;
	private Set<String> scope;
	private Set<String> authorizedGrantTypes;
	private Set<GrantedAuthority> authorities = Collections.emptySet();
	private String clientName;
	private String clientDescription;
	private boolean allowRefresh = false; // do we allow refresh tokens for this client?
	private Long accessTokenTimeout; // in seconds
	private Long refreshTokenTimeout; // in seconds
	private String owner; // userid of who registered it
	private String registeredRedirectUri;
	private Set<String> resourceIds;

	// TODO:
	/*
	private boolean allowMultipleAccessTokens; // do we allow multiple access tokens, or not?
	private boolean reuseRefreshToken; // do we let someone reuse a refresh token?
	*/
	
	/**
     * @return the clientId
     */
	@Id
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

	/**
     * @param accessTokenTimeout Lifetime of access tokens, in seconds (optional - leave null for no timeout)
     */
	@Basic
    public Long getAccessTokenTimeout() {
    	return accessTokenTimeout;
    }

	/**
     * @param accessTokenTimeout the accessTokenTimeout to set
     */
    public void setAccessTokenTimeout(Long accessTokenTimeout) {
    	this.accessTokenTimeout = accessTokenTimeout;
    }

	/**
     * @return the refreshTokenTimeout
     */
	@Basic
    public Long getRefreshTokenTimeout() {
    	return refreshTokenTimeout;
    }

	/**
     * @param refreshTokenTimeout Lifetime of refresh tokens, in seconds (optional - leave null for no timeout)
     */
    public void setRefreshTokenTimeout(Long refreshTokenTimeout) {
    	this.refreshTokenTimeout = refreshTokenTimeout;
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
	@Basic
    public String getRegisteredRedirectUri() {
    	return registeredRedirectUri;
    }

	/**
     * @param registeredRedirectUri the registeredRedirectUri to set
     */
    public void setRegisteredRedirectUri(String registeredRedirectUri) {
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
        public ClientDetailsEntityBuilder setAccessTokenTimeout(Long accessTokenTimeout) {
	        instance.setAccessTokenTimeout(accessTokenTimeout);
			return this;
        }

		/**
         * @param refreshTokenTimeout
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setRefreshTokenTimeout(java.lang.Long)
         */
        public ClientDetailsEntityBuilder setRefreshTokenTimeout(Long refreshTokenTimeout) {
	        instance.setRefreshTokenTimeout(refreshTokenTimeout);
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
        public ClientDetailsEntityBuilder setRegisteredRedirectUri(String registeredRedirectUri) {
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

}
