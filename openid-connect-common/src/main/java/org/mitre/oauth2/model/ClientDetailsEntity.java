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
/**
 * 
 */
package org.mitre.oauth2.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;
import org.mitre.jose.JWSAlgorithmEmbed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * @author jricher
 * 
 */
@Entity
@Table(name = "client_details")
@NamedQueries({
	@NamedQuery(name = "ClientDetailsEntity.findAll", query = "SELECT c FROM ClientDetailsEntity c"),
	@NamedQuery(name = "ClientDetailsEntity.getByClientId", query = "select c from ClientDetailsEntity c where c.clientId = :clientId")
})
public class ClientDetailsEntity implements ClientDetails {

	/**
	 * 
	 */
    private static final int DEFAULT_ID_TOKEN_VALIDITY_SECONDS = 600;

	private static final long serialVersionUID = -1617727085733786296L;

	private Long id;

	/** Fields from the OAuth2 Dynamic Registration Specification */
	private String clientId = null; // client_id
	private String clientSecret = null; // client_secret
	private Set<String> redirectUris = new HashSet<String>(); // redirect_uris
	private String clientName; // client_name
	private String clientUri; // client_uri
	private String logoUri; // logo_uri
	private Set<String> contacts; // contacts
	private String tosUri; // tos_uri
	private AuthMethod tokenEndpointAuthMethod = AuthMethod.SECRET_BASIC; // token_endpoint_auth_method
	private Set<String> scope = new HashSet<String>(); // scope
	private Set<String> grantTypes = new HashSet<String>(); // grant_types
	private Set<String> responseTypes = new HashSet<String>(); // response_types
	private String policyUri;
	private String jwksUri;

	/** Fields from OIDC Client Registration Specification **/
	private AppType applicationType; // application_type
	private String sectorIdentifierUri; // sector_identifier_uri
	private SubjectType subjectType; // subject_type

	private JWSAlgorithmEmbed requestObjectSigningAlg = null; // request_object_signing_alg

	private JWSAlgorithmEmbed userInfoSignedResponseAlg = null; // user_info_signed_response_alg
	private JWEAlgorithmEmbed userInfoEncryptedResponseAlg = null; // user_info_encrypted_response_alg
	private JWEEncryptionMethodEmbed userInfoEncryptedResponseEnc = null; // user_info_encrypted_response_enc

	private JWSAlgorithmEmbed idTokenSignedResponseAlg = null; // id_token_signed_response_alg
	private JWEAlgorithmEmbed idTokenEncryptedResponseAlg = null; // id_token_encrypted_response_alg
	private JWEEncryptionMethodEmbed idTokenEncryptedResponseEnc = null; // id_token_encrypted_response_enc

	private Integer defaultMaxAge; // default_max_age
	private Boolean requireAuthTime; // require_auth_time
	private Set<String> defaultACRvalues; // default_acr_values

	private String initiateLoginUri; // initiate_login_uri
	private String postLogoutRedirectUri; // post_logout_redirect_uri

	private Set<String> requestUris; // request_uris

	/** Fields to support the ClientDetails interface **/
	private Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
	private Integer accessTokenValiditySeconds = 0; // in seconds
	private Integer refreshTokenValiditySeconds = 0; // in seconds
	private Set<String> resourceIds = new HashSet<String>();
	private Map<String, Object> additionalInformation = new HashMap<String, Object>();

	/** Our own fields **/
	private String clientDescription = ""; // human-readable description
	private boolean reuseRefreshToken = true; // do we let someone reuse a refresh token?
	private boolean dynamicallyRegistered = false; // was this client dynamically registered?
	private boolean trustedRegistration = false; // was this client registered by a trusted process?
	private boolean allowIntrospection = false; // do we let this client call the introspection endpoint?
	private Integer idTokenValiditySeconds; //timeout for id tokens
	private Date createdAt; // time the client was created

	public enum AuthMethod {
		SECRET_POST("client_secret_post"),
		SECRET_BASIC("client_secret_basic"),
		SECRET_JWT("client_secret_jwt"),
		PRIVATE_KEY("private_key_jwt"),
		NONE("none");

		private final String value;

		// map to aid reverse lookup
		private static final Map<String, AuthMethod> lookup = new HashMap<String, AuthMethod>();
		static {
			for (AuthMethod a : AuthMethod.values()) {
				lookup.put(a.getValue(), a);
			}
		}

		AuthMethod(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static AuthMethod getByValue(String value) {
			return lookup.get(value);
		}
	}

	public enum AppType {
		WEB("web"), NATIVE("native");

		private final String value;

		// map to aid reverse lookup
		private static final Map<String, AppType> lookup = new HashMap<String, AppType>();
		static {
			for (AppType a : AppType.values()) {
				lookup.put(a.getValue(), a);
			}
		}

		AppType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static AppType getByValue(String value) {
			return lookup.get(value);
		}
	}

	public enum SubjectType {
		PAIRWISE("pairwise"), PUBLIC("public");

		private final String value;

		// map to aid reverse lookup
		private static final Map<String, SubjectType> lookup = new HashMap<String, SubjectType>();
		static {
			for (SubjectType u : SubjectType.values()) {
				lookup.put(u.getValue(), u);
			}
		}

		SubjectType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static SubjectType getByValue(String value) {
			return lookup.get(value);
		}
	}

	/**
	 * Create a blank ClientDetailsEntity
	 */
	public ClientDetailsEntity() {

	}

	@PrePersist
	@PreUpdate
	private void prePersist() {
		// make sure that ID tokens always time out, default to 5 minutes
		if (getIdTokenValiditySeconds() == null) {
			setIdTokenValiditySeconds(DEFAULT_ID_TOKEN_VALIDITY_SECONDS);
		}
	}	
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
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
	@Transient
	public boolean isAllowRefresh() {
		if (grantTypes != null) {
			return getAuthorizedGrantTypes().contains("refresh_token");
		} else {
			return false; // if there are no grants, we can't be refreshing them, can we?
		}
	}

	@Basic
	@Column(name="reuse_refresh_tokens")
	public boolean isReuseRefreshToken() {
		return reuseRefreshToken;
	}

	public void setReuseRefreshToken(boolean reuseRefreshToken) {
		this.reuseRefreshToken = reuseRefreshToken;
	}

	/**
	 * Number of seconds ID token is valid for. MUST be a positive integer, can not be null.
	 * 
	 * @return the idTokenValiditySeconds
	 */
	@Basic
	@Column(name="id_token_validity_seconds")
	public Integer getIdTokenValiditySeconds() {
		return idTokenValiditySeconds;
	}

	/**
	 * @param idTokenValiditySeconds the idTokenValiditySeconds to set
	 */
	public void setIdTokenValiditySeconds(Integer idTokenValiditySeconds) {
		this.idTokenValiditySeconds = idTokenValiditySeconds;
	}

	/**
	 * @return the dynamicallyRegistered
	 */
	@Basic
	@Column(name="dynamically_registered")
	public boolean isDynamicallyRegistered() {
		return dynamicallyRegistered;
	}

	/**
	 * @param dynamicallyRegistered the dynamicallyRegistered to set
	 */
	public void setDynamicallyRegistered(boolean dynamicallyRegistered) {
		this.dynamicallyRegistered = dynamicallyRegistered;
	}
	
	/**
	 * @return the trustedRegistration
	 */
	@Basic
	@Column(name="trusted_registration")
	public boolean isTrustedRegistration() {
		return trustedRegistration;
	}

	/**
	 * @param trustedRegistration the trustedRegistration to set
	 */
	public void setTrustedRegistration(boolean trustedRegistration) {
		this.trustedRegistration = trustedRegistration;
	}



	/**
	 * @return the allowIntrospection
	 */
	@Basic
	@Column(name="allow_introspection")
	public boolean isAllowIntrospection() {
		return allowIntrospection;
	}

	/**
	 * @param allowIntrospection the allowIntrospection to set
	 */
	public void setAllowIntrospection(boolean allowIntrospection) {
		this.allowIntrospection = allowIntrospection;
	}

	/**
	 * 
	 */
	@Override
	@Transient
	public boolean isSecretRequired() {
		// TODO: this should check the auth method field instead
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
			name="client_scope",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Override
	@Column(name="scope")
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
			name="client_grant_type",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="grant_type")
	public Set<String> getGrantTypes() {
		return grantTypes;
	}

	/**
	 * @param authorizedGrantTypes the OAuth2 grant types that this client is allowed to use
	 */
	public void setGrantTypes(Set<String> grantTypes) {
		this.grantTypes = grantTypes;
	}

	/**
	 * passthrough for SECOAUTH api
	 */
	@Override
	public Set<String> getAuthorizedGrantTypes() {
		return getGrantTypes();
	}

	/**
	 * @return the authorities
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_authority",
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
			name="client_redirect_uri",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="redirect_uri")
	public Set<String> getRedirectUris() {
		return redirectUris;
	}

	/**
	 * @param registeredRedirectUri the registeredRedirectUri to set
	 */
	public void setRedirectUris(Set<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

	/**
	 * Pass-through method to fulfill the ClientDetails interface with a bad name
	 */
	@Override
	@Transient
	public Set<String> getRegisteredRedirectUri() {
		return getRedirectUris();
	}

	/**
	 * @return the resourceIds
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_resource",
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
	 * However, it's somehow required by SECOUATH.
	 * 
	 * @return an empty map
	 */
	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		return this.additionalInformation;
	}




	@Enumerated(EnumType.STRING)
	@Column(name="application_type")
	public AppType getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(AppType applicationType) {
		this.applicationType = applicationType;
	}

	@Basic
	@Column(name="client_name")
	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="token_endpoint_auth_method")
	public AuthMethod getTokenEndpointAuthMethod() {
		return tokenEndpointAuthMethod;
	}

	public void setTokenEndpointAuthMethod(AuthMethod tokenEndpointAuthMethod) {
		this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="subject_type")
	public SubjectType getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(SubjectType subjectType) {
		this.subjectType = subjectType;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_contact",
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
	@Column(name="logo_uri")
	public String getLogoUri() {
		return logoUri;
	}

	public void setLogoUri(String logoUri) {
		this.logoUri = logoUri;
	}

	@Basic
	@Column(name="policy_uri")
	public String getPolicyUri() {
		return policyUri;
	}

	public void setPolicyUri(String policyUri) {
		this.policyUri = policyUri;
	}

	/**
	 * @return the clientUrl
	 */
	@Basic
	@Column(name="client_uri")
	public String getClientUri() {
		return clientUri;
	}

	/**
	 * @param clientUrl the clientUrl to set
	 */
	public void setClientUri(String clientUri) {
		this.clientUri = clientUri;
	}

	/**
	 * @return the tosUrl
	 */
	@Basic
	@Column(name="tos_uri")
	public String getTosUri() {
		return tosUri;
	}

	/**
	 * @param tosUrl the tosUrl to set
	 */
	public void setTosUri(String tosUri) {
		this.tosUri = tosUri;
	}

	@Basic
	@Column(name="jwks_uri")
	public String getJwksUri() {
		return jwksUri;
	}

	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
	}

	@Basic
	@Column(name="sector_identifier_uri")
	public String getSectorIdentifierUri() {
		return sectorIdentifierUri;
	}

	public void setSectorIdentifierUri(String sectorIdentifierUri) {
		this.sectorIdentifierUri = sectorIdentifierUri;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="request_object_signing_alg"))
	})
	public JWSAlgorithmEmbed getRequestObjectSigningAlg() {
		return requestObjectSigningAlg;
	}

	public void setRequestObjectSigningAlg(JWSAlgorithmEmbed requestObjectSigningAlg) {
		this.requestObjectSigningAlg = requestObjectSigningAlg;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="user_info_signed_response_alg"))
	})
	public JWSAlgorithmEmbed getUserInfoSignedResponseAlg() {
		return userInfoSignedResponseAlg;
	}

	public void setUserInfoSignedResponseAlg(JWSAlgorithmEmbed userInfoSignedResponseAlg) {
		this.userInfoSignedResponseAlg = userInfoSignedResponseAlg;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="user_info_encrypted_response_alg"))
	})
	public JWEAlgorithmEmbed getUserInfoEncryptedResponseAlg() {
		return userInfoEncryptedResponseAlg;
	}

	public void setUserInfoEncryptedResponseAlg(JWEAlgorithmEmbed userInfoEncryptedResponseAlg) {
		this.userInfoEncryptedResponseAlg = userInfoEncryptedResponseAlg;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="user_info_encrypted_response_enc"))
	})
	public JWEEncryptionMethodEmbed getUserInfoEncryptedResponseEnc() {
		return userInfoEncryptedResponseEnc;
	}

	public void setUserInfoEncryptedResponseEnc(JWEEncryptionMethodEmbed userInfoEncryptedResponseEnc) {
		this.userInfoEncryptedResponseEnc = userInfoEncryptedResponseEnc;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="id_token_signed_response_alg"))
	})
	public JWSAlgorithmEmbed getIdTokenSignedResponseAlg() {
		return idTokenSignedResponseAlg;
	}

	public void setIdTokenSignedResponseAlg(JWSAlgorithmEmbed idTokenSignedResponseAlg) {
		this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="id_token_encrypted_response_alg"))
	})
	public JWEAlgorithmEmbed getIdTokenEncryptedResponseAlg() {
		return idTokenEncryptedResponseAlg;
	}

	public void setIdTokenEncryptedResponseAlg(JWEAlgorithmEmbed idTokenEncryptedResponseAlg) {
		this.idTokenEncryptedResponseAlg = idTokenEncryptedResponseAlg;
	}

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="id_token_encrypted_response_enc"))
	})
	public JWEEncryptionMethodEmbed getIdTokenEncryptedResponseEnc() {
		return idTokenEncryptedResponseEnc;
	}

	public void setIdTokenEncryptedResponseEnc(JWEEncryptionMethodEmbed idTokenEncryptedResponseEnc) {
		this.idTokenEncryptedResponseEnc = idTokenEncryptedResponseEnc;
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

	/**
	 * @return the responseTypes
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_response_type",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="response_type")
	public Set<String> getResponseTypes() {
		return responseTypes;
	}

	/**
	 * @param responseTypes the responseTypes to set
	 */
	public void setResponseTypes(Set<String> responseTypes) {
		this.responseTypes = responseTypes;
	}

	/**
	 * @return the defaultACRvalues
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_default_acr_value",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="default_acr_value")
	public Set<String> getDefaultACRvalues() {
		return defaultACRvalues;
	}

	/**
	 * @param defaultACRvalues the defaultACRvalues to set
	 */
	public void setDefaultACRvalues(Set<String> defaultACRvalues) {
		this.defaultACRvalues = defaultACRvalues;
	}

	/**
	 * @return the initiateLoginUri
	 */
	@Basic
	@Column(name="initiate_login_uri")
	public String getInitiateLoginUri() {
		return initiateLoginUri;
	}

	/**
	 * @param initiateLoginUri the initiateLoginUri to set
	 */
	public void setInitiateLoginUri(String initiateLoginUri) {
		this.initiateLoginUri = initiateLoginUri;
	}

	/**
	 * @return the postLogoutRedirectUri
	 */
	@Basic
	@Column(name="post_logout_redirect_uri")
	public String getPostLogoutRedirectUri() {
		return postLogoutRedirectUri;
	}

	/**
	 * @param postLogoutRedirectUri the postLogoutRedirectUri to set
	 */
	public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
		this.postLogoutRedirectUri = postLogoutRedirectUri;
	}

	/**
	 * @return the requestUris
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_request_uri",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="request_uri")
	public Set<String> getRequestUris() {
		return requestUris;
	}

	/**
	 * @param requestUris the requestUris to set
	 */
	public void setRequestUris(Set<String> requestUris) {
		this.requestUris = requestUris;
	}

	/**
	 * @return the createdAt
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_at")
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
