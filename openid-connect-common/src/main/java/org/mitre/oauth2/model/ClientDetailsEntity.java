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
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mitre.jwt.encryption.JweAlgorithms;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * @author jricher
 * 
 */
@Entity
@Table(name="client_details")
@NamedQueries({
	@NamedQuery(name = "ClientDetailsEntity.findAll", query = "SELECT c FROM ClientDetailsEntity c"),
	@NamedQuery(name = "ClientDetailsEntity.getByClientId", query = "select c from ClientDetailsEntity c where c.clientId = :clientId")
})
public class ClientDetailsEntity implements ClientDetails {

	private Long id;
	
	/** Our own fields **/
	private String clientDescription = "";//this is ours
	private Boolean allowRefresh = false; // do we allow refresh tokens for this client?
	private Boolean allowMultipleAccessTokens; // do we allow multiple access tokens, or not?
	private Boolean reuseRefreshToken; // do we let someone reuse a refresh token?
	
	/** Fields from ClientDetails interface **/
    private String clientId = "";
    private String clientSecret = "";
    private Set<String> scope = new HashSet<String>();
    private Set<String> authorizedGrantTypes = new HashSet<String>();
	private Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
    private Integer accessTokenValiditySeconds = 0; // in seconds 
    private Integer refreshTokenValiditySeconds = 0; // in seconds 
    private Set<String> registeredRedirectUri = new HashSet<String>();
    private Set<String> resourceIds = new HashSet<String>();
	private Map<String, Object> additionalInformation = new HashMap<String, Object>();

    /** Fields from Client Registration Specification **/
	private AppType applicationType;
	private String applicationName;
	private AuthType tokenEndpointAuthType = AuthType.SECRET_BASIC;
	private String userIdType;
	
	private Set<String> contacts; 	
	
	private String logoUrl;
	private String policyUrl;
	private String jwkUrl;
	private String jwkEncryptionUrl;
	private String x509Url;
	private String x509EncryptionUrl;
	private String sectorIdentifierUrl;
	
	private JwsAlgorithm requireSignedRequestObject;
	
	private JwsAlgorithm userInfoSignedResponseAlg;
	private JweAlgorithms userInfoEncryptedResponseAlg;
	private JweAlgorithms userInfoEncryptedResponseEnc;
	private JweAlgorithms userInfoEncryptedResponseInt;
	
	private JwsAlgorithm idTokenSignedResponseAlg;
	private JweAlgorithms idTokenEncryptedResponseAlg;
	private JweAlgorithms idTokenEncryptedReponseEnc;
	private JweAlgorithms idTokenEncryptedResponseInt;
	
	private Integer defaultMaxAge;
	private Boolean requireAuthTime;
	private String defaultACR;
	
	
	public enum AuthType {
		SECRET_POST("client_secret_post"), 
		SECRET_BASIC("client_secret_basic"), 
		SECRET_JWT("client_secret_jwt"), 
		PRIVATE_KEY("private_key_jwt");
		
		private final String value;
		
		AuthType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public enum AppType {
		WEB("web"), NATIVE("native");
		
		private final String value;
		
		AppType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	/**
	 * Create a blank ClientDetailsEntity
	 */
	public ClientDetailsEntity() {
		
	}

	public static ClientDetailsEntityBuilder makeBuilder() {
    	return new ClientDetailsEntityBuilder();
    }

	//TODO or FIXME: This builder is currently unused. If we want to keep it, it needs
	//to be updated with the current fieldset.
	public static class ClientDetailsEntityBuilder {
		private ClientDetailsEntity instance;
		
		private ClientDetailsEntityBuilder() {
			instance = new ClientDetailsEntity();
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
         * @see org.mitre.oauth2.model.ClientDetailsEntity#setAllowRefresh(Boolean)
         */
        public ClientDetailsEntityBuilder setAllowRefresh(Boolean allowRefresh) {
	        instance.setAllowRefresh(allowRefresh);
			return this;
        }
        
        /**
         * @param allow
         * @see 
         */
        public ClientDetailsEntityBuilder setAllowMultipleAccessTokens(Boolean allow) {
        	instance.setAllowMultipleAccessTokens(allow);
        	return this;
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
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	/**
	 * 
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
     * @return the clientDescription
     */
	@Basic
	@Column(name="client_description")
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
	@Column(name="allow_refresh")
    public Boolean isAllowRefresh() {
    	return allowRefresh;
    }

	/**
     * @param allowRefresh Whether to allow for issuance of refresh tokens or not (defaults to false)
     */
    public void setAllowRefresh(Boolean allowRefresh) {
    	this.allowRefresh = allowRefresh;
    }
	
    @Basic
    @Column(name="allow_multiple_access_tokens")
    public Boolean isAllowMultipleAccessTokens() {
		return allowMultipleAccessTokens;
	}

	public void setAllowMultipleAccessTokens(Boolean allowMultipleAccessTokens) {
		this.allowMultipleAccessTokens = allowMultipleAccessTokens;
	}

	@Basic
	@Column(name="reuse_refresh_tokens")
	public Boolean isReuseRefreshToken() {
		return reuseRefreshToken;
	}

	public void setReuseRefreshToken(Boolean reuseRefreshToken) {
		this.reuseRefreshToken = reuseRefreshToken;
	}
	
	
	
	
    
	/**
	 * If the clientSecret is not null, then it is always required.
     */
    @Override
    @Transient
    public boolean isSecretRequired() {
	    return getClientSecret() != null;
    }

	/**
	 * If the scope list is not null or empty, then this client has been scoped.
     */
    @Override
    @Transient
    public boolean isScoped() {
    	return getScope() != null && !getScope().isEmpty();
    }
	
	/**
     * @return the clientId
     */
	@Basic
	@Override
	@Column(name="client_id")
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
	@Override
	@Column(name="client_secret")
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
	@Override
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
			name="authorized_grant_type",
			joinColumns=@JoinColumn(name="owner_id")
	)
	@Override
	@Column(name="authorized_grant_type")
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
			name="authority",
			joinColumns=@JoinColumn(name="owner_id")
	)
	@Override
	@Column(name="authority")
    public Set<GrantedAuthority> getAuthorities() {
    	return authorities;
    }

	/**
     * @param authorities the Spring Security authorities this client is given
     */
    public void setAuthorities(Set<GrantedAuthority> authorities) {
    	this.authorities = authorities;
    }

	@Override
	@Basic
	@Column(name="access_token_validity_seconds")
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
	@Column(name="refresh_token_validity_seconds")
	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}
	
	/**
     * @param refreshTokenTimeout Lifetime of refresh tokens, in seconds (optional - leave null for no timeout)
     */
    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
    	this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }
	
    /**
     * @return the registeredRedirectUri
     */
    @ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="redirect_uri",
			joinColumns=@JoinColumn(name="owner_id")
	)
    @Column(name="redirect_uri")
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
	@Column(name="resource_id")
    public Set<String> getResourceIds() {
    	return resourceIds;
    }

	/**
     * @param resourceIds the resourceIds to set
     */
    public void setResourceIds(Set<String> resourceIds) {
    	this.resourceIds = resourceIds;
    }

    
	/**
	 * This library does not make use of this field, so it is not
	 * stored using our persistence layer.
	 * 
	 * @return an empty map
	 */
	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		return this.additionalInformation;
	}

	


	@Basic
	@Column(name="application_type")
	public AppType getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(AppType applicationType) {
		this.applicationType = applicationType;
	}

	@Basic
	@Column(name="application_name")
	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	@Basic
	@Column(name="token_endpoint_auth_type")
	public AuthType getTokenEndpointAuthType() {
		return tokenEndpointAuthType;
	}

	public void setTokenEndpointAuthType(AuthType tokenEndpointAuthType) {
		this.tokenEndpointAuthType = tokenEndpointAuthType;
	}

	@Basic
	@Column(name="user_id_type")
	public String getUserIdType() {
		return userIdType;
	}

	public void setUserIdType(String userIdType) {
		this.userIdType = userIdType;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="contact",
			joinColumns=@JoinColumn(name="owner_id")
	)
	@Column(name="contact")
	public Set<String> getContacts() {
		return contacts;
	}

	public void setContacts(Set<String> contacts) {
		this.contacts = contacts;
	}

	@Basic
	@Column(name="logo_url")
	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	
	@Basic
	@Column(name="policy_url")
	public String getPolicyUrl() {
		return policyUrl;
	}

	public void setPolicyUrl(String policyUrl) {
		this.policyUrl = policyUrl;
	}

	@Basic
	@Column(name="jwk_url")
	public String getJwkUrl() {
		return jwkUrl;
	}

	public void setJwkUrl(String jwkUrl) {
		this.jwkUrl = jwkUrl;
	}

	@Basic
	@Column(name="jwk_encryption_url")
	public String getJwkEncryptionUrl() {
		return jwkEncryptionUrl;
	}

	public void setJwkEncryptionUrl(String jwkEncryptionUrl) {
		this.jwkEncryptionUrl = jwkEncryptionUrl;
	}

	@Basic
	@Column(name="x509_url")
	public String getX509Url() {
		return x509Url;
	}

	public void setX509Url(String x509Url) {
		this.x509Url = x509Url;
	}
	
	@Basic
	@Column(name="x509_encryption_url")
	public String getX509EncryptionUrl() {
		return x509EncryptionUrl;
	}

	public void setX509EncryptionUrl(String x509EncryptionUrl) {
		this.x509EncryptionUrl = x509EncryptionUrl;
	}

	@Basic
	@Column(name="sector_identifier_url")
	public String getSectorIdentifierUrl() {
		return sectorIdentifierUrl;
	}

	public void setSectorIdentifierUrl(String sectorIdentifierUrl) {
		this.sectorIdentifierUrl = sectorIdentifierUrl;
	}

	@Basic
	@Column(name="requre_signed_request_object")
	public JwsAlgorithm getRequireSignedRequestObject() {
		return requireSignedRequestObject;
	}

	public void setRequireSignedRequestObject(
			JwsAlgorithm requireSignedRequestObject) {
		this.requireSignedRequestObject = requireSignedRequestObject;
	}

	@Basic
	@Column(name="user_info_signed_response_alg")
	public JwsAlgorithm getUserInfoSignedResponseAlg() {
		return userInfoSignedResponseAlg;
	}

	public void setUserInfoSignedResponseAlg(JwsAlgorithm userInfoSignedResponseAlg) {
		this.userInfoSignedResponseAlg = userInfoSignedResponseAlg;
	}

	@Basic
	@Column(name="user_info_encrypted_response_alg")
	public JweAlgorithms getUserInfoEncryptedResponseAlg() {
		return userInfoEncryptedResponseAlg;
	}

	public void setUserInfoEncryptedResponseAlg(
			JweAlgorithms userInfoEncryptedResponseAlg) {
		this.userInfoEncryptedResponseAlg = userInfoEncryptedResponseAlg;
	}

	@Basic
	@Column(name="user_info_encrypted_response_enc")
	public JweAlgorithms getUserInfoEncryptedResponseEnc() {
		return userInfoEncryptedResponseEnc;
	}

	public void setUserInfoEncryptedResponseEnc(
			JweAlgorithms userInfoEncryptedResponseEnc) {
		this.userInfoEncryptedResponseEnc = userInfoEncryptedResponseEnc;
	}

	@Basic
	@Column(name="user_info_encrypted_response_int")
	public JweAlgorithms getUserInfoEncryptedResponseInt() {
		return userInfoEncryptedResponseInt;
	}

	public void setUserInfoEncryptedResponseInt(
			JweAlgorithms userInfoEncryptedResponseInt) {
		this.userInfoEncryptedResponseInt = userInfoEncryptedResponseInt;
	}

	@Basic
	@Column(name="id_token_signed_response_alg")
	public JwsAlgorithm getIdTokenSignedResponseAlg() {
		return idTokenSignedResponseAlg;
	}

	public void setIdTokenSignedResponseAlg(JwsAlgorithm idTokenSignedResponseAlg) {
		this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
	}

	@Basic
	@Column(name="id_token_encrypted_response_alg")
	public JweAlgorithms getIdTokenEncryptedResponseAlg() {
		return idTokenEncryptedResponseAlg;
	}

	public void setIdTokenEncryptedResponseAlg(
			JweAlgorithms idTokenEncryptedResponseAlg) {
		this.idTokenEncryptedResponseAlg = idTokenEncryptedResponseAlg;
	}

	@Basic
	@Column(name="id_token_encrypted_response_enc")
	public JweAlgorithms getIdTokenEncryptedReponseEnc() {
		return idTokenEncryptedReponseEnc;
	}

	public void setIdTokenEncryptedReponseEnc(
			JweAlgorithms idTokenEncryptedReponseEnc) {
		this.idTokenEncryptedReponseEnc = idTokenEncryptedReponseEnc;
	}

	@Basic
	@Column(name="id_token_encrypted_response_int")
	public JweAlgorithms getIdTokenEncryptedResponseInt() {
		return idTokenEncryptedResponseInt;
	}

	public void setIdTokenEncryptedResponseInt(
			JweAlgorithms idTokenEncryptedResponseInt) {
		this.idTokenEncryptedResponseInt = idTokenEncryptedResponseInt;
	}

	@Basic
	@Column(name="default_max_age")
	public Integer getDefaultMaxAge() {
		return defaultMaxAge;
	}

	public void setDefaultMaxAge(Integer defaultMaxAge) {
		this.defaultMaxAge = defaultMaxAge;
	}

	@Basic
	@Column(name="require_auth_time")
	public Boolean getRequireAuthTime() {
		return requireAuthTime;
	}

	public void setRequireAuthTime(Boolean requireAuthTime) {
		this.requireAuthTime = requireAuthTime;
	}

	@Basic
	@Column(name="default_acr")
	public String getDefaultACR() {
		return defaultACR;
	}

	public void setDefaultACR(String defaultACR) {
		this.defaultACR = defaultACR;
	}
	
    @Override
	public String toString() {
		return "ClientDetailsEntity ["
				+ (id != null ? "id=" + id + ", " : "")
				+ (clientDescription != null ? "clientDescription="
						+ clientDescription + ", " : "")
				+ "allowRefresh="
				+ allowRefresh
				+ ", allowMultipleAccessTokens="
				+ allowMultipleAccessTokens
				+ ", reuseRefreshToken="
				+ reuseRefreshToken
				+ ", "
				+ (clientId != null ? "clientId=" + clientId + ", " : "")
				+ (clientSecret != null ? "clientSecret=" + clientSecret + ", "
						: "")
				+ (scope != null ? "scope=" + scope + ", " : "")
				+ (authorizedGrantTypes != null ? "authorizedGrantTypes="
						+ authorizedGrantTypes + ", " : "")
				+ (authorities != null ? "authorities=" + authorities + ", "
						: "")
				+ (accessTokenValiditySeconds != null ? "accessTokenValiditySeconds="
						+ accessTokenValiditySeconds + ", "
						: "")
				+ (refreshTokenValiditySeconds != null ? "refreshTokenValiditySeconds="
						+ refreshTokenValiditySeconds + ", "
						: "")
				+ (registeredRedirectUri != null ? "registeredRedirectUri="
						+ registeredRedirectUri + ", " : "")
				+ (resourceIds != null ? "resourceIds=" + resourceIds + ", "
						: "")
				+ (additionalInformation != null ? "additionalInformation="
						+ additionalInformation + ", " : "")
				+ (applicationType != null ? "applicationType="
						+ applicationType + ", " : "")
				+ (applicationName != null ? "applicationName="
						+ applicationName + ", " : "")
				+ (tokenEndpointAuthType != null ? "tokenEndpointAuthType="
						+ tokenEndpointAuthType + ", " : "")
				+ (userIdType != null ? "userIdType=" + userIdType + ", " : "")
				+ (contacts != null ? "contacts=" + contacts + ", " : "")
				+ (logoUrl != null ? "logoUrl=" + logoUrl + ", " : "")
				+ (policyUrl != null ? "policyUrl=" + policyUrl + ", " : "")
				+ (jwkUrl != null ? "jwkUrl=" + jwkUrl + ", " : "")
				+ (jwkEncryptionUrl != null ? "jwkEncryptionUrl="
						+ jwkEncryptionUrl + ", " : "")
				+ (x509Url != null ? "x509Url=" + x509Url + ", " : "")
				+ (x509EncryptionUrl != null ? "x509EncryptionUrl="
						+ x509EncryptionUrl + ", " : "")
				+ (sectorIdentifierUrl != null ? "sectorIdentifierUrl="
						+ sectorIdentifierUrl + ", " : "")
				+ (requireSignedRequestObject != null ? "requireSignedRequestObject="
						+ requireSignedRequestObject + ", "
						: "")
				+ (userInfoSignedResponseAlg != null ? "userInfoSignedResponseAlg="
						+ userInfoSignedResponseAlg + ", "
						: "")
				+ (userInfoEncryptedResponseAlg != null ? "userInfoEncryptedResponseAlg="
						+ userInfoEncryptedResponseAlg + ", "
						: "")
				+ (userInfoEncryptedResponseEnc != null ? "userInfoEncryptedResponseEnc="
						+ userInfoEncryptedResponseEnc + ", "
						: "")
				+ (userInfoEncryptedResponseInt != null ? "userInfoEncryptedResponseInt="
						+ userInfoEncryptedResponseInt + ", "
						: "")
				+ (idTokenSignedResponseAlg != null ? "idTokenSignedResponseAlg="
						+ idTokenSignedResponseAlg + ", "
						: "")
				+ (idTokenEncryptedResponseAlg != null ? "idTokenEncryptedResponseAlg="
						+ idTokenEncryptedResponseAlg + ", "
						: "")
				+ (idTokenEncryptedReponseEnc != null ? "idTokenEncryptedReponseEnc="
						+ idTokenEncryptedReponseEnc + ", "
						: "")
				+ (idTokenEncryptedResponseInt != null ? "idTokenEncryptedResponseInt="
						+ idTokenEncryptedResponseInt + ", "
						: "")
				+ (defaultMaxAge != null ? "defaultMaxAge=" + defaultMaxAge
						+ ", " : "")
				+ (requireAuthTime != null ? "requireAuthTime="
						+ requireAuthTime + ", " : "")
				+ (defaultACR != null ? "defaultACR=" + defaultACR : "") + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((accessTokenValiditySeconds == null) ? 0
						: accessTokenValiditySeconds.hashCode());
		result = prime
				* result
				+ ((additionalInformation == null) ? 0 : additionalInformation
						.hashCode());
		result = prime * result + (allowMultipleAccessTokens ? 1231 : 1237);
		result = prime * result + (allowRefresh ? 1231 : 1237);
		result = prime * result
				+ ((applicationName == null) ? 0 : applicationName.hashCode());
		result = prime * result
				+ ((applicationType == null) ? 0 : applicationType.hashCode());
		result = prime * result
				+ ((authorities == null) ? 0 : authorities.hashCode());
		result = prime
				* result
				+ ((authorizedGrantTypes == null) ? 0 : authorizedGrantTypes
						.hashCode());
		result = prime
				* result
				+ ((clientDescription == null) ? 0 : clientDescription
						.hashCode());
		result = prime * result
				+ ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result
				+ ((clientSecret == null) ? 0 : clientSecret.hashCode());
		result = prime * result
				+ ((contacts == null) ? 0 : contacts.hashCode());
		result = prime * result
				+ ((defaultACR == null) ? 0 : defaultACR.hashCode());
		result = prime * result
				+ ((defaultMaxAge == null) ? 0 : defaultMaxAge.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime
				* result
				+ ((idTokenEncryptedReponseEnc == null) ? 0
						: idTokenEncryptedReponseEnc.hashCode());
		result = prime
				* result
				+ ((idTokenEncryptedResponseAlg == null) ? 0
						: idTokenEncryptedResponseAlg.hashCode());
		result = prime
				* result
				+ ((idTokenEncryptedResponseInt == null) ? 0
						: idTokenEncryptedResponseInt.hashCode());
		result = prime
				* result
				+ ((idTokenSignedResponseAlg == null) ? 0
						: idTokenSignedResponseAlg.hashCode());
		result = prime
				* result
				+ ((jwkEncryptionUrl == null) ? 0 : jwkEncryptionUrl.hashCode());
		result = prime * result + ((jwkUrl == null) ? 0 : jwkUrl.hashCode());
		result = prime * result + ((logoUrl == null) ? 0 : logoUrl.hashCode());
		result = prime * result
				+ ((policyUrl == null) ? 0 : policyUrl.hashCode());
		result = prime
				* result
				+ ((refreshTokenValiditySeconds == null) ? 0
						: refreshTokenValiditySeconds.hashCode());
		result = prime
				* result
				+ ((registeredRedirectUri == null) ? 0 : registeredRedirectUri
						.hashCode());
		result = prime * result
				+ ((requireAuthTime == null) ? 0 : requireAuthTime.hashCode());
		result = prime
				* result
				+ ((requireSignedRequestObject == null) ? 0
						: requireSignedRequestObject.hashCode());
		result = prime * result
				+ ((resourceIds == null) ? 0 : resourceIds.hashCode());
		result = prime * result + (reuseRefreshToken ? 1231 : 1237);
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime
				* result
				+ ((sectorIdentifierUrl == null) ? 0 : sectorIdentifierUrl
						.hashCode());
		result = prime
				* result
				+ ((tokenEndpointAuthType == null) ? 0 : tokenEndpointAuthType
						.hashCode());
		result = prime * result
				+ ((userIdType == null) ? 0 : userIdType.hashCode());
		result = prime
				* result
				+ ((userInfoEncryptedResponseAlg == null) ? 0
						: userInfoEncryptedResponseAlg.hashCode());
		result = prime
				* result
				+ ((userInfoEncryptedResponseEnc == null) ? 0
						: userInfoEncryptedResponseEnc.hashCode());
		result = prime
				* result
				+ ((userInfoEncryptedResponseInt == null) ? 0
						: userInfoEncryptedResponseInt.hashCode());
		result = prime
				* result
				+ ((userInfoSignedResponseAlg == null) ? 0
						: userInfoSignedResponseAlg.hashCode());
		result = prime
				* result
				+ ((x509EncryptionUrl == null) ? 0 : x509EncryptionUrl
						.hashCode());
		result = prime * result + ((x509Url == null) ? 0 : x509Url.hashCode());
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
		if (accessTokenValiditySeconds == null) {
			if (other.accessTokenValiditySeconds != null) {
				return false;
			}
		} else if (!accessTokenValiditySeconds
				.equals(other.accessTokenValiditySeconds)) {
			return false;
		}
		if (additionalInformation == null) {
			if (other.additionalInformation != null) {
				return false;
			}
		} else if (!additionalInformation.equals(other.additionalInformation)) {
			return false;
		}
		if (allowMultipleAccessTokens != other.allowMultipleAccessTokens) {
			return false;
		}
		if (allowRefresh != other.allowRefresh) {
			return false;
		}
		if (applicationName == null) {
			if (other.applicationName != null) {
				return false;
			}
		} else if (!applicationName.equals(other.applicationName)) {
			return false;
		}
		if (applicationType != other.applicationType) {
			return false;
		}
		if (authorities == null) {
			if (other.authorities != null) {
				return false;
			}
		} else if (!authorities.equals(other.authorities)) {
			return false;
		}
		if (authorizedGrantTypes == null) {
			if (other.authorizedGrantTypes != null) {
				return false;
			}
		} else if (!authorizedGrantTypes.equals(other.authorizedGrantTypes)) {
			return false;
		}
		if (clientDescription == null) {
			if (other.clientDescription != null) {
				return false;
			}
		} else if (!clientDescription.equals(other.clientDescription)) {
			return false;
		}
		if (clientId == null) {
			if (other.clientId != null) {
				return false;
			}
		} else if (!clientId.equals(other.clientId)) {
			return false;
		}
		if (clientSecret == null) {
			if (other.clientSecret != null) {
				return false;
			}
		} else if (!clientSecret.equals(other.clientSecret)) {
			return false;
		}
		if (contacts == null) {
			if (other.contacts != null) {
				return false;
			}
		} else if (!contacts.equals(other.contacts)) {
			return false;
		}
		if (defaultACR == null) {
			if (other.defaultACR != null) {
				return false;
			}
		} else if (!defaultACR.equals(other.defaultACR)) {
			return false;
		}
		if (defaultMaxAge == null) {
			if (other.defaultMaxAge != null) {
				return false;
			}
		} else if (!defaultMaxAge.equals(other.defaultMaxAge)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (idTokenEncryptedReponseEnc != other.idTokenEncryptedReponseEnc) {
			return false;
		}
		if (idTokenEncryptedResponseAlg != other.idTokenEncryptedResponseAlg) {
			return false;
		}
		if (idTokenEncryptedResponseInt != other.idTokenEncryptedResponseInt) {
			return false;
		}
		if (idTokenSignedResponseAlg != other.idTokenSignedResponseAlg) {
			return false;
		}
		if (jwkEncryptionUrl == null) {
			if (other.jwkEncryptionUrl != null) {
				return false;
			}
		} else if (!jwkEncryptionUrl.equals(other.jwkEncryptionUrl)) {
			return false;
		}
		if (jwkUrl == null) {
			if (other.jwkUrl != null) {
				return false;
			}
		} else if (!jwkUrl.equals(other.jwkUrl)) {
			return false;
		}
		if (logoUrl == null) {
			if (other.logoUrl != null) {
				return false;
			}
		} else if (!logoUrl.equals(other.logoUrl)) {
			return false;
		}
		if (policyUrl == null) {
			if (other.policyUrl != null) {
				return false;
			}
		} else if (!policyUrl.equals(other.policyUrl)) {
			return false;
		}
		if (refreshTokenValiditySeconds == null) {
			if (other.refreshTokenValiditySeconds != null) {
				return false;
			}
		} else if (!refreshTokenValiditySeconds
				.equals(other.refreshTokenValiditySeconds)) {
			return false;
		}
		if (registeredRedirectUri == null) {
			if (other.registeredRedirectUri != null) {
				return false;
			}
		} else if (!registeredRedirectUri.equals(other.registeredRedirectUri)) {
			return false;
		}
		if (requireAuthTime == null) {
			if (other.requireAuthTime != null) {
				return false;
			}
		} else if (!requireAuthTime.equals(other.requireAuthTime)) {
			return false;
		}
		if (requireSignedRequestObject != other.requireSignedRequestObject) {
			return false;
		}
		if (resourceIds == null) {
			if (other.resourceIds != null) {
				return false;
			}
		} else if (!resourceIds.equals(other.resourceIds)) {
			return false;
		}
		if (reuseRefreshToken != other.reuseRefreshToken) {
			return false;
		}
		if (scope == null) {
			if (other.scope != null) {
				return false;
			}
		} else if (!scope.equals(other.scope)) {
			return false;
		}
		if (sectorIdentifierUrl == null) {
			if (other.sectorIdentifierUrl != null) {
				return false;
			}
		} else if (!sectorIdentifierUrl.equals(other.sectorIdentifierUrl)) {
			return false;
		}
		if (tokenEndpointAuthType != other.tokenEndpointAuthType) {
			return false;
		}
		if (userIdType == null) {
			if (other.userIdType != null) {
				return false;
			}
		} else if (!userIdType.equals(other.userIdType)) {
			return false;
		}
		if (userInfoEncryptedResponseAlg != other.userInfoEncryptedResponseAlg) {
			return false;
		}
		if (userInfoEncryptedResponseEnc != other.userInfoEncryptedResponseEnc) {
			return false;
		}
		if (userInfoEncryptedResponseInt != other.userInfoEncryptedResponseInt) {
			return false;
		}
		if (userInfoSignedResponseAlg != other.userInfoSignedResponseAlg) {
			return false;
		}
		if (x509EncryptionUrl == null) {
			if (other.x509EncryptionUrl != null) {
				return false;
			}
		} else if (!x509EncryptionUrl.equals(other.x509EncryptionUrl)) {
			return false;
		}
		if (x509Url == null) {
			if (other.x509Url != null) {
				return false;
			}
		} else if (!x509Url.equals(other.x509Url)) {
			return false;
		}
		return true;
	}
	
}
