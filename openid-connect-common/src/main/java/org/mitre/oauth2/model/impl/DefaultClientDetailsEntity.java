/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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

package org.mitre.oauth2.model.impl;

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
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.springframework.security.core.GrantedAuthority;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

/**
 * @author jricher
 * 
 */
@Entity
@Table(name = "client_details")
@NamedQueries({
	@NamedQuery(name = "DefaultClientDetailsEntity.findAll", query = "SELECT c FROM DefaultClientDetailsEntity c"),
	@NamedQuery(name = "DefaultClientDetailsEntity.getByClientId", query = "select c from DefaultClientDetailsEntity c where c.clientId = :clientId")
})
public class DefaultClientDetailsEntity implements ClientDetailsEntity {

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

	private JWSAlgorithmEmbed tokenEndpointAuthSigningAlg = null; // token_endpoint_auth_signing_alg

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
	private boolean allowIntrospection = false; // do we let this client call the introspection endpoint?
	private Integer idTokenValiditySeconds; //timeout for id tokens
	private Date createdAt; // time the client was created
	
	/**
	 * Create a blank DefaultClientDetailsEntity
	 */
	DefaultClientDetailsEntity() {

	}

	@PrePersist
	@PreUpdate
	private void prePersist() {
		// make sure that ID tokens always time out, default to 5 minutes
		if (getIdTokenValiditySeconds() == null) {
			setIdTokenValiditySeconds(DEFAULT_ID_TOKEN_VALIDITY_SECONDS);
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getId()
	 */
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setId(java.lang.Long)
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getClientDescription()
	 */
	@Override
	@Basic
	@Column(name="client_description")
	public String getClientDescription() {
		return clientDescription;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setClientDescription(java.lang.String)
	 */
	@Override
	public void setClientDescription(String clientDescription) {
		this.clientDescription = clientDescription;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#isAllowRefresh()
	 */
	@Override
	@Transient
	public boolean isAllowRefresh() {
		if (grantTypes != null) {
			return getAuthorizedGrantTypes().contains("refresh_token");
		} else {
			return false; // if there are no grants, we can't be refreshing them, can we?
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#isReuseRefreshToken()
	 */
	@Override
	@Basic
	@Column(name="reuse_refresh_tokens")
	public boolean isReuseRefreshToken() {
		return reuseRefreshToken;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setReuseRefreshToken(boolean)
	 */
	@Override
	public void setReuseRefreshToken(boolean reuseRefreshToken) {
		this.reuseRefreshToken = reuseRefreshToken;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getIdTokenValiditySeconds()
	 */
	@Override
	@Basic
	@Column(name="id_token_validity_seconds")
	public Integer getIdTokenValiditySeconds() {
		return idTokenValiditySeconds;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setIdTokenValiditySeconds(java.lang.Integer)
	 */
	@Override
	public void setIdTokenValiditySeconds(Integer idTokenValiditySeconds) {
		this.idTokenValiditySeconds = idTokenValiditySeconds;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#isDynamicallyRegistered()
	 */
	@Override
	@Basic
	@Column(name="dynamically_registered")
	public boolean isDynamicallyRegistered() {
		return dynamicallyRegistered;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setDynamicallyRegistered(boolean)
	 */
	@Override
	public void setDynamicallyRegistered(boolean dynamicallyRegistered) {
		this.dynamicallyRegistered = dynamicallyRegistered;
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#isAllowIntrospection()
	 */
	@Override
	@Basic
	@Column(name="allow_introspection")
	public boolean isAllowIntrospection() {
		return allowIntrospection;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setAllowIntrospection(boolean)
	 */
	@Override
	public void setAllowIntrospection(boolean allowIntrospection) {
		this.allowIntrospection = allowIntrospection;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#isSecretRequired()
	 */
	@Override
	@Transient
	public boolean isSecretRequired() {
		if (getTokenEndpointAuthMethod() != null &&
				(getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_BASIC) ||
				 getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_POST) ||
				 getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT))) {
			return true;
		} else {
			return false;
		}

	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#isScoped()
	 */
	@Override
	@Transient
	public boolean isScoped() {
		return getScope() != null && !getScope().isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getClientId()
	 */
	@Override
	@Basic
	@Column(name="client_id")
	public String getClientId() {
		return clientId;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setClientId(java.lang.String)
	 */
	@Override
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getClientSecret()
	 */
	@Override
	@Basic
	@Column(name="client_secret")
	public String getClientSecret() {
		return clientSecret;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setClientSecret(java.lang.String)
	 */
	@Override
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getScope()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_scope",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="scope")
	public Set<String> getScope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setScope(java.util.Set)
	 */
	@Override
	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getGrantTypes()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_grant_type",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="grant_type")
	public Set<String> getGrantTypes() {
		return grantTypes;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setGrantTypes(java.util.Set)
	 */
	@Override
	public void setGrantTypes(Set<String> grantTypes) {
		this.grantTypes = grantTypes;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getAuthorizedGrantTypes()
	 */
	@Override
	public Set<String> getAuthorizedGrantTypes() {
		return getGrantTypes();
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getAuthorities()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_authority",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="authority")
	public Set<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setAuthorities(java.util.Set)
	 */
	@Override
	public void setAuthorities(Set<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getAccessTokenValiditySeconds()
	 */
	@Override
	@Basic
	@Column(name="access_token_validity_seconds")
	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setAccessTokenValiditySeconds(java.lang.Integer)
	 */
	@Override
	public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getRefreshTokenValiditySeconds()
	 */
	@Override
	@Basic
	@Column(name="refresh_token_validity_seconds")
	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setRefreshTokenValiditySeconds(java.lang.Integer)
	 */
	@Override
	public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getRedirectUris()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_redirect_uri",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="redirect_uri")
	public Set<String> getRedirectUris() {
		return redirectUris;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setRedirectUris(java.util.Set)
	 */
	@Override
	public void setRedirectUris(Set<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getRegisteredRedirectUri()
	 */
	@Override
	@Transient
	public Set<String> getRegisteredRedirectUri() {
		return getRedirectUris();
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getResourceIds()
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

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setResourceIds(java.util.Set)
	 */
	@Override
	public void setResourceIds(Set<String> resourceIds) {
		this.resourceIds = resourceIds;
	}


	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getAdditionalInformation()
	 */
	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		return this.additionalInformation;
	}




	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getApplicationType()
	 */
	@Override
	@Enumerated(EnumType.STRING)
	@Column(name="application_type")
	public AppType getApplicationType() {
		return applicationType;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setApplicationType(org.mitre.oauth2.model.impl.DefaultClientDetailsEntity.AppType)
	 */
	@Override
	public void setApplicationType(AppType applicationType) {
		this.applicationType = applicationType;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getClientName()
	 */
	@Override
	@Basic
	@Column(name="client_name")
	public String getClientName() {
		return clientName;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setClientName(java.lang.String)
	 */
	@Override
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getTokenEndpointAuthMethod()
	 */
	@Override
	@Enumerated(EnumType.STRING)
	@Column(name="token_endpoint_auth_method")
	public AuthMethod getTokenEndpointAuthMethod() {
		return tokenEndpointAuthMethod;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setTokenEndpointAuthMethod(org.mitre.oauth2.model.impl.DefaultClientDetailsEntity.AuthMethod)
	 */
	@Override
	public void setTokenEndpointAuthMethod(AuthMethod tokenEndpointAuthMethod) {
		this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getSubjectType()
	 */
	@Override
	@Enumerated(EnumType.STRING)
	@Column(name="subject_type")
	public SubjectType getSubjectType() {
		return subjectType;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setSubjectType(org.mitre.oauth2.model.impl.DefaultClientDetailsEntity.SubjectType)
	 */
	@Override
	public void setSubjectType(SubjectType subjectType) {
		this.subjectType = subjectType;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getContacts()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_contact",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="contact")
	public Set<String> getContacts() {
		return contacts;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setContacts(java.util.Set)
	 */
	@Override
	public void setContacts(Set<String> contacts) {
		this.contacts = contacts;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getLogoUri()
	 */
	@Override
	@Basic
	@Column(name="logo_uri")
	public String getLogoUri() {
		return logoUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setLogoUri(java.lang.String)
	 */
	@Override
	public void setLogoUri(String logoUri) {
		this.logoUri = logoUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getPolicyUri()
	 */
	@Override
	@Basic
	@Column(name="policy_uri")
	public String getPolicyUri() {
		return policyUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setPolicyUri(java.lang.String)
	 */
	@Override
	public void setPolicyUri(String policyUri) {
		this.policyUri = policyUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getClientUri()
	 */
	@Override
	@Basic
	@Column(name="client_uri")
	public String getClientUri() {
		return clientUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setClientUri(java.lang.String)
	 */
	@Override
	public void setClientUri(String clientUri) {
		this.clientUri = clientUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getTosUri()
	 */
	@Override
	@Basic
	@Column(name="tos_uri")
	public String getTosUri() {
		return tosUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setTosUri(java.lang.String)
	 */
	@Override
	public void setTosUri(String tosUri) {
		this.tosUri = tosUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getJwksUri()
	 */
	@Override
	@Basic
	@Column(name="jwks_uri")
	public String getJwksUri() {
		return jwksUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setJwksUri(java.lang.String)
	 */
	@Override
	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getSectorIdentifierUri()
	 */
	@Override
	@Basic
	@Column(name="sector_identifier_uri")
	public String getSectorIdentifierUri() {
		return sectorIdentifierUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setSectorIdentifierUri(java.lang.String)
	 */
	@Override
	public void setSectorIdentifierUri(String sectorIdentifierUri) {
		this.sectorIdentifierUri = sectorIdentifierUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getRequestObjectSigningAlgEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="request_object_signing_alg"))
	})
	public JWSAlgorithmEmbed getRequestObjectSigningAlgEmbed() {
		return requestObjectSigningAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setRequestObjectSigningAlgEmbed(org.mitre.jose.JWSAlgorithmEmbed)
	 */
	@Override
	public void setRequestObjectSigningAlgEmbed(JWSAlgorithmEmbed requestObjectSigningAlg) {
		this.requestObjectSigningAlg = requestObjectSigningAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getUserInfoSignedResponseAlgEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="user_info_signed_response_alg"))
	})
	public JWSAlgorithmEmbed getUserInfoSignedResponseAlgEmbed() {
		return userInfoSignedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setUserInfoSignedResponseAlgEmbed(org.mitre.jose.JWSAlgorithmEmbed)
	 */
	@Override
	public void setUserInfoSignedResponseAlgEmbed(JWSAlgorithmEmbed userInfoSignedResponseAlg) {
		this.userInfoSignedResponseAlg = userInfoSignedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getUserInfoEncryptedResponseAlgEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="user_info_encrypted_response_alg"))
	})
	public JWEAlgorithmEmbed getUserInfoEncryptedResponseAlgEmbed() {
		return userInfoEncryptedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setUserInfoEncryptedResponseAlgEmbed(org.mitre.jose.JWEAlgorithmEmbed)
	 */
	@Override
	public void setUserInfoEncryptedResponseAlgEmbed(JWEAlgorithmEmbed userInfoEncryptedResponseAlg) {
		this.userInfoEncryptedResponseAlg = userInfoEncryptedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getUserInfoEncryptedResponseEncEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="user_info_encrypted_response_enc"))
	})
	public JWEEncryptionMethodEmbed getUserInfoEncryptedResponseEncEmbed() {
		return userInfoEncryptedResponseEnc;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setUserInfoEncryptedResponseEncEmbed(org.mitre.jose.JWEEncryptionMethodEmbed)
	 */
	@Override
	public void setUserInfoEncryptedResponseEncEmbed(JWEEncryptionMethodEmbed userInfoEncryptedResponseEnc) {
		this.userInfoEncryptedResponseEnc = userInfoEncryptedResponseEnc;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getIdTokenSignedResponseAlgEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="id_token_signed_response_alg"))
	})
	public JWSAlgorithmEmbed getIdTokenSignedResponseAlgEmbed() {
		return idTokenSignedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setIdTokenSignedResponseAlgEmbed(org.mitre.jose.JWSAlgorithmEmbed)
	 */
	@Override
	public void setIdTokenSignedResponseAlgEmbed(JWSAlgorithmEmbed idTokenSignedResponseAlg) {
		this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getIdTokenEncryptedResponseAlgEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="id_token_encrypted_response_alg"))
	})
	public JWEAlgorithmEmbed getIdTokenEncryptedResponseAlgEmbed() {
		return idTokenEncryptedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setIdTokenEncryptedResponseAlgEmbed(org.mitre.jose.JWEAlgorithmEmbed)
	 */
	@Override
	public void setIdTokenEncryptedResponseAlgEmbed(JWEAlgorithmEmbed idTokenEncryptedResponseAlg) {
		this.idTokenEncryptedResponseAlg = idTokenEncryptedResponseAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getIdTokenEncryptedResponseEncEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="id_token_encrypted_response_enc"))
	})
	public JWEEncryptionMethodEmbed getIdTokenEncryptedResponseEncEmbed() {
		return idTokenEncryptedResponseEnc;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setIdTokenEncryptedResponseEncEmbed(org.mitre.jose.JWEEncryptionMethodEmbed)
	 */
	@Override
	public void setIdTokenEncryptedResponseEncEmbed(JWEEncryptionMethodEmbed idTokenEncryptedResponseEnc) {
		this.idTokenEncryptedResponseEnc = idTokenEncryptedResponseEnc;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getTokenEndpointAuthSigningAlgEmbed()
	 */
	@Override
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "algorithmName", column=@Column(name="token_endpoint_auth_signing_alg"))
	})
	public JWSAlgorithmEmbed getTokenEndpointAuthSigningAlgEmbed() {
		return tokenEndpointAuthSigningAlg;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setTokenEndpointAuthSigningAlgEmbed(org.mitre.jose.JWSAlgorithmEmbed)
	 */
	@Override
	public void setTokenEndpointAuthSigningAlgEmbed(JWSAlgorithmEmbed tokenEndpointAuthSigningAlgEmbed) {
		this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlgEmbed;
	}

	//
	// Transient passthrough methods for JOSE elements
	//

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getRequestObjectSigningAlg()
	 */
	@Override
	@Transient
	public JWSAlgorithm getRequestObjectSigningAlg() {
		if (requestObjectSigningAlg != null) {
			return requestObjectSigningAlg.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setRequestObjectSigningAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	@Override
	public void setRequestObjectSigningAlg(JWSAlgorithm requestObjectSigningAlg) {
		this.requestObjectSigningAlg = new JWSAlgorithmEmbed(requestObjectSigningAlg);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getUserInfoSignedResponseAlg()
	 */
	@Override
	@Transient
	public JWSAlgorithm getUserInfoSignedResponseAlg() {
		if (userInfoSignedResponseAlg != null) {
			return userInfoSignedResponseAlg.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setUserInfoSignedResponseAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	@Override
	public void setUserInfoSignedResponseAlg(JWSAlgorithm userInfoSignedResponseAlg) {
		this.userInfoSignedResponseAlg = new JWSAlgorithmEmbed(userInfoSignedResponseAlg);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getUserInfoEncryptedResponseAlg()
	 */
	@Override
	@Transient
	public JWEAlgorithm getUserInfoEncryptedResponseAlg() {
		if (userInfoEncryptedResponseAlg != null) {
			return userInfoEncryptedResponseAlg.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setUserInfoEncryptedResponseAlg(com.nimbusds.jose.JWEAlgorithm)
	 */
	@Override
	public void setUserInfoEncryptedResponseAlg(JWEAlgorithm userInfoEncryptedResponseAlg) {
		this.userInfoEncryptedResponseAlg = new JWEAlgorithmEmbed(userInfoEncryptedResponseAlg);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getUserInfoEncryptedResponseEnc()
	 */
	@Override
	@Transient
	public EncryptionMethod getUserInfoEncryptedResponseEnc() {
		if (userInfoEncryptedResponseEnc != null) {
			return userInfoEncryptedResponseEnc.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setUserInfoEncryptedResponseEnc(com.nimbusds.jose.EncryptionMethod)
	 */
	@Override
	public void setUserInfoEncryptedResponseEnc(EncryptionMethod userInfoEncryptedResponseEnc) {
		this.userInfoEncryptedResponseEnc = new JWEEncryptionMethodEmbed(userInfoEncryptedResponseEnc);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getIdTokenSignedResponseAlg()
	 */
	@Override
	@Transient
	public JWSAlgorithm getIdTokenSignedResponseAlg() {
		if (idTokenSignedResponseAlg != null) {
			return idTokenSignedResponseAlg.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setIdTokenSignedResponseAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	@Override
	public void setIdTokenSignedResponseAlg(JWSAlgorithm idTokenSignedResponseAlg) {
		this.idTokenSignedResponseAlg = new JWSAlgorithmEmbed(idTokenSignedResponseAlg);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getIdTokenEncryptedResponseAlg()
	 */
	@Override
	@Transient
	public JWEAlgorithm getIdTokenEncryptedResponseAlg() {
		if (idTokenEncryptedResponseAlg != null) {
			return idTokenEncryptedResponseAlg.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setIdTokenEncryptedResponseAlg(com.nimbusds.jose.JWEAlgorithm)
	 */
	@Override
	public void setIdTokenEncryptedResponseAlg(JWEAlgorithm idTokenEncryptedResponseAlg) {
		this.idTokenEncryptedResponseAlg = new JWEAlgorithmEmbed(idTokenEncryptedResponseAlg);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getIdTokenEncryptedResponseEnc()
	 */
	@Override
	@Transient
	public EncryptionMethod getIdTokenEncryptedResponseEnc() {
		if (idTokenEncryptedResponseEnc != null) {
			return idTokenEncryptedResponseEnc.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setIdTokenEncryptedResponseEnc(com.nimbusds.jose.EncryptionMethod)
	 */
	@Override
	public void setIdTokenEncryptedResponseEnc(EncryptionMethod idTokenEncryptedResponseEnc) {
		this.idTokenEncryptedResponseEnc = new JWEEncryptionMethodEmbed(idTokenEncryptedResponseEnc);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getTokenEndpointAuthSigningAlg()
	 */
	@Override
	@Transient
	public JWSAlgorithm getTokenEndpointAuthSigningAlg() {
		if (tokenEndpointAuthSigningAlg != null) {
			return tokenEndpointAuthSigningAlg.getAlgorithm();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setTokenEndpointAuthSigningAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	@Override
	public void setTokenEndpointAuthSigningAlg(JWSAlgorithm tokenEndpointAuthSigningAlg) {
		this.tokenEndpointAuthSigningAlg = new JWSAlgorithmEmbed(tokenEndpointAuthSigningAlg);
	}

	// END Transient JOSE methods

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getDefaultMaxAge()
	 */
	@Override
	@Basic
	@Column(name="default_max_age")
	public Integer getDefaultMaxAge() {
		return defaultMaxAge;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setDefaultMaxAge(java.lang.Integer)
	 */
	@Override
	public void setDefaultMaxAge(Integer defaultMaxAge) {
		this.defaultMaxAge = defaultMaxAge;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getRequireAuthTime()
	 */
	@Override
	@Basic
	@Column(name="require_auth_time")
	public Boolean getRequireAuthTime() {
		return requireAuthTime;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setRequireAuthTime(java.lang.Boolean)
	 */
	@Override
	public void setRequireAuthTime(Boolean requireAuthTime) {
		this.requireAuthTime = requireAuthTime;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getResponseTypes()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_response_type",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="response_type")
	public Set<String> getResponseTypes() {
		return responseTypes;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setResponseTypes(java.util.Set)
	 */
	@Override
	public void setResponseTypes(Set<String> responseTypes) {
		this.responseTypes = responseTypes;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getDefaultACRvalues()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_default_acr_value",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="default_acr_value")
	public Set<String> getDefaultACRvalues() {
		return defaultACRvalues;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setDefaultACRvalues(java.util.Set)
	 */
	@Override
	public void setDefaultACRvalues(Set<String> defaultACRvalues) {
		this.defaultACRvalues = defaultACRvalues;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getInitiateLoginUri()
	 */
	@Override
	@Basic
	@Column(name="initiate_login_uri")
	public String getInitiateLoginUri() {
		return initiateLoginUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setInitiateLoginUri(java.lang.String)
	 */
	@Override
	public void setInitiateLoginUri(String initiateLoginUri) {
		this.initiateLoginUri = initiateLoginUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getPostLogoutRedirectUri()
	 */
	@Override
	@Basic
	@Column(name="post_logout_redirect_uri")
	public String getPostLogoutRedirectUri() {
		return postLogoutRedirectUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setPostLogoutRedirectUri(java.lang.String)
	 */
	@Override
	public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
		this.postLogoutRedirectUri = postLogoutRedirectUri;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getRequestUris()
	 */
	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="client_request_uri",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="request_uri")
	public Set<String> getRequestUris() {
		return requestUris;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setRequestUris(java.util.Set)
	 */
	@Override
	public void setRequestUris(Set<String> requestUris) {
		this.requestUris = requestUris;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#getCreatedAt()
	 */
	@Override
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_at")
	public Date getCreatedAt() {
		return createdAt;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#setCreatedAt(java.util.Date)
	 */
	@Override
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.model.impl.MyInt#isAutoApprove(java.lang.String)
	 */
	@Override
	public boolean isAutoApprove(String scope) {
		return false;
	}

}
