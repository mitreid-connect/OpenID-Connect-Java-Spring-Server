/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
 *******************************************************************************/
/**
 *
 */
package cz.muni.ics.oauth2.model;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;
import cz.muni.ics.oauth2.model.convert.JWEAlgorithmStringConverter;
import cz.muni.ics.oauth2.model.convert.JWEEncryptionMethodStringConverter;
import cz.muni.ics.oauth2.model.convert.JWKSetStringConverter;
import cz.muni.ics.oauth2.model.convert.JWSAlgorithmStringConverter;
import cz.muni.ics.oauth2.model.convert.JWTStringConverter;
import cz.muni.ics.oauth2.model.convert.PKCEAlgorithmStringConverter;
import cz.muni.ics.oauth2.model.convert.SimpleGrantedAuthorityStringConverter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "client_details")
@NamedQueries({
	@NamedQuery(name = ClientDetailsEntity.QUERY_ALL, query = "SELECT c FROM ClientDetailsEntity c"),
	@NamedQuery(name = ClientDetailsEntity.QUERY_BY_CLIENT_ID, query = "select c from ClientDetailsEntity c where c.clientId = :" + ClientDetailsEntity.PARAM_CLIENT_ID)
})
public class ClientDetailsEntity implements ClientDetails {

	public static final String QUERY_BY_CLIENT_ID = "ClientDetailsEntity.getByClientId";
	public static final String QUERY_ALL = "ClientDetailsEntity.findAll";

	public static final String PARAM_CLIENT_ID = "clientId";

	private static final int DEFAULT_ID_TOKEN_VALIDITY_SECONDS = 600;

	private static final long serialVersionUID = -1617727085733786296L;

	private Long id;
	private String clientId = null;
	private String clientSecret = null;
	private Set<String> redirectUris = new HashSet<>();
	private String clientName;
	private String clientUri;
	private Set<String> contacts;
	private String tosUri;
	private AuthMethod tokenEndpointAuthMethod = AuthMethod.SECRET_BASIC;
	private Set<String> scope = new HashSet<>();
	private Set<String> grantTypes = new HashSet<>();
	private Set<String> responseTypes = new HashSet<>();
	private String policyUri;
	private String jwksUri;
	private JWKSet jwks;
	private String softwareId;
	private String softwareVersion;
	private AppType applicationType;
	private String sectorIdentifierUri;
	private SubjectType subjectType;
	private JWSAlgorithm requestObjectSigningAlg = null;
	private JWSAlgorithm userInfoSignedResponseAlg = null;
	private JWEAlgorithm userInfoEncryptedResponseAlg = null;
	private EncryptionMethod userInfoEncryptedResponseEnc = null;
	private JWSAlgorithm idTokenSignedResponseAlg = null;
	private JWEAlgorithm idTokenEncryptedResponseAlg = null;
	private EncryptionMethod idTokenEncryptedResponseEnc = null;
	private JWSAlgorithm tokenEndpointAuthSigningAlg = null;
	private Integer defaultMaxAge;
	private Boolean requireAuthTime;
	private Set<String> defaultACRvalues;
	private String initiateLoginUri;
	private Set<String> postLogoutRedirectUris;
	private Set<String> requestUris;
	private Set<GrantedAuthority> authorities = new HashSet<>();
	private Integer accessTokenValiditySeconds = 0;
	private Integer refreshTokenValiditySeconds = 0;
	private Set<String> resourceIds = new HashSet<>();
	private Map<String, Object> additionalInformation = new HashMap<>();
	private String clientDescription = "";
	private boolean reuseRefreshToken = true;
	private boolean dynamicallyRegistered = false;
	private boolean allowIntrospection = false;
	private Integer idTokenValiditySeconds;
	private Date createdAt;
	private boolean clearAccessTokensOnRefresh = true;
	private Integer deviceCodeValiditySeconds;
	private Set<String> claimsRedirectUris;
	private JWT softwareStatement;
	private PKCEAlgorithm codeChallengeMethod;

	public enum AuthMethod {
		SECRET_POST("client_secret_post"),
		SECRET_BASIC("client_secret_basic"),
		SECRET_JWT("client_secret_jwt"),
		PRIVATE_KEY("private_key_jwt"),
		NONE("none");

		private final String value;

		// map to aid reverse lookup
		private static final Map<String, AuthMethod> lookup = new HashMap<>();
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
		private static final Map<String, AppType> lookup = new HashMap<>();
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
		private static final Map<String, SubjectType> lookup = new HashMap<>();
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

	public ClientDetailsEntity() {

	}

	@PrePersist
	@PreUpdate
	private void prePersist() {
		if (getIdTokenValiditySeconds() == null) {
			setIdTokenValiditySeconds(DEFAULT_ID_TOKEN_VALIDITY_SECONDS);
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name="client_description")
	public String getClientDescription() {
		return clientDescription;
	}

	public void setClientDescription(String clientDescription) {
		this.clientDescription = clientDescription;
	}

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

	@Basic
	@Column(name="id_token_validity_seconds")
	public Integer getIdTokenValiditySeconds() {
		return idTokenValiditySeconds;
	}

	public void setIdTokenValiditySeconds(Integer idTokenValiditySeconds) {
		this.idTokenValiditySeconds = idTokenValiditySeconds;
	}

	@Basic
	@Column(name="dynamically_registered")
	public boolean isDynamicallyRegistered() {
		return dynamicallyRegistered;
	}

	public void setDynamicallyRegistered(boolean dynamicallyRegistered) {
		this.dynamicallyRegistered = dynamicallyRegistered;
	}

	@Basic
	@Column(name="allow_introspection")
	public boolean isAllowIntrospection() {
		return allowIntrospection;
	}

	public void setAllowIntrospection(boolean allowIntrospection) {
		this.allowIntrospection = allowIntrospection;
	}

	@Override
	@Transient
	public boolean isSecretRequired() {
		return getTokenEndpointAuthMethod() != null &&
			(getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_BASIC) ||
				getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_POST) ||
				getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT));
	}

	@Override
	@Transient
	public boolean isScoped() {
		return getScope() != null && !getScope().isEmpty();
	}

	@Basic
	@Override
	@Column(name="client_id")
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Basic
	@Override
	@Column(name="client_secret")
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_scope", joinColumns=@JoinColumn(name="owner_id"))
	@Override
	@Column(name="scope")
	public Set<String> getScope() {
		return scope;
	}

	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_grant_type", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="grant_type")
	public Set<String> getGrantTypes() {
		return grantTypes;
	}

	public void setGrantTypes(Set<String> grantTypes) {
		this.grantTypes = grantTypes;
	}

	@Override
	@Transient
	public Set<String> getAuthorizedGrantTypes() {
		return getGrantTypes();
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_authority", joinColumns=@JoinColumn(name="owner_id"))
	@Override
	@Convert(converter = SimpleGrantedAuthorityStringConverter.class)
	@Column(name="authority")
	public Set<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Override
	@Basic
	@Column(name="access_token_validity_seconds")
	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
	}

	@Override
	@Basic
	@Column(name="refresh_token_validity_seconds")
	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_redirect_uri", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="redirect_uri")
	public Set<String> getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(Set<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

	@Override
	@Transient
	public Set<String> getRegisteredRedirectUri() {
		return getRedirectUris();
	}

	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_resource", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="resource_id")
	public Set<String> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(Set<String> resourceIds) {
		this.resourceIds = resourceIds;
	}

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
	@CollectionTable(name="client_contact", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="contact")
	public Set<String> getContacts() {
		return contacts;
	}

	public void setContacts(Set<String> contacts) {
		this.contacts = contacts;
	}

	@Basic
	@Column(name="policy_uri")
	public String getPolicyUri() {
		return policyUri;
	}

	public void setPolicyUri(String policyUri) {
		this.policyUri = policyUri;
	}

	@Basic
	@Column(name="client_uri")
	public String getClientUri() {
		return clientUri;
	}

	public void setClientUri(String clientUri) {
		this.clientUri = clientUri;
	}

	@Basic
	@Column(name="tos_uri")
	public String getTosUri() {
		return tosUri;
	}

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
	@Column(name="jwks")
	@Convert(converter = JWKSetStringConverter.class)
	public JWKSet getJwks() {
		return jwks;
	}

	public void setJwks(JWKSet jwks) {
		this.jwks = jwks;
	}

	@Basic
	@Column(name="sector_identifier_uri")
	public String getSectorIdentifierUri() {
		return sectorIdentifierUri;
	}

	public void setSectorIdentifierUri(String sectorIdentifierUri) {
		this.sectorIdentifierUri = sectorIdentifierUri;
	}

	@Basic
	@Column(name = "request_object_signing_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	public JWSAlgorithm getRequestObjectSigningAlg() {
		return requestObjectSigningAlg;
	}

	public void setRequestObjectSigningAlg(JWSAlgorithm requestObjectSigningAlg) {
		this.requestObjectSigningAlg = requestObjectSigningAlg;
	}

	@Basic
	@Column(name = "user_info_signed_response_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	public JWSAlgorithm getUserInfoSignedResponseAlg() {
		return userInfoSignedResponseAlg;
	}

	public void setUserInfoSignedResponseAlg(JWSAlgorithm userInfoSignedResponseAlg) {
		this.userInfoSignedResponseAlg = userInfoSignedResponseAlg;
	}

	@Basic
	@Column(name = "user_info_encrypted_response_alg")
	@Convert(converter = JWEAlgorithmStringConverter.class)
	public JWEAlgorithm getUserInfoEncryptedResponseAlg() {
		return userInfoEncryptedResponseAlg;
	}

	public void setUserInfoEncryptedResponseAlg(JWEAlgorithm userInfoEncryptedResponseAlg) {
		this.userInfoEncryptedResponseAlg = userInfoEncryptedResponseAlg;
	}

	@Basic
	@Column(name = "user_info_encrypted_response_enc")
	@Convert(converter = JWEEncryptionMethodStringConverter.class)
	public EncryptionMethod getUserInfoEncryptedResponseEnc() {
		return userInfoEncryptedResponseEnc;
	}

	public void setUserInfoEncryptedResponseEnc(EncryptionMethod userInfoEncryptedResponseEnc) {
		this.userInfoEncryptedResponseEnc = userInfoEncryptedResponseEnc;
	}

	@Basic
	@Column(name="id_token_signed_response_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	public JWSAlgorithm getIdTokenSignedResponseAlg() {
		return idTokenSignedResponseAlg;
	}

	public void setIdTokenSignedResponseAlg(JWSAlgorithm idTokenSignedResponseAlg) {
		this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
	}

	@Basic
	@Column(name = "id_token_encrypted_response_alg")
	@Convert(converter = JWEAlgorithmStringConverter.class)
	public JWEAlgorithm getIdTokenEncryptedResponseAlg() {
		return idTokenEncryptedResponseAlg;
	}

	public void setIdTokenEncryptedResponseAlg(JWEAlgorithm idTokenEncryptedResponseAlg) {
		this.idTokenEncryptedResponseAlg = idTokenEncryptedResponseAlg;
	}

	@Basic
	@Column(name = "id_token_encrypted_response_enc")
	@Convert(converter = JWEEncryptionMethodStringConverter.class)
	public EncryptionMethod getIdTokenEncryptedResponseEnc() {
		return idTokenEncryptedResponseEnc;
	}

	public void setIdTokenEncryptedResponseEnc(EncryptionMethod idTokenEncryptedResponseEnc) {
		this.idTokenEncryptedResponseEnc = idTokenEncryptedResponseEnc;
	}

	@Basic
	@Column(name="token_endpoint_auth_signing_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	public JWSAlgorithm getTokenEndpointAuthSigningAlg() {
		return tokenEndpointAuthSigningAlg;
	}

	public void setTokenEndpointAuthSigningAlg(JWSAlgorithm tokenEndpointAuthSigningAlg) {
		this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
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

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_response_type", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="response_type")
	public Set<String> getResponseTypes() {
		return responseTypes;
	}

	public void setResponseTypes(Set<String> responseTypes) {
		this.responseTypes = responseTypes;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_default_acr_value", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="default_acr_value")
	public Set<String> getDefaultACRvalues() {
		return defaultACRvalues;
	}

	public void setDefaultACRvalues(Set<String> defaultACRvalues) {
		this.defaultACRvalues = defaultACRvalues;
	}

	@Basic
	@Column(name="initiate_login_uri")
	public String getInitiateLoginUri() {
		return initiateLoginUri;
	}

	public void setInitiateLoginUri(String initiateLoginUri) {
		this.initiateLoginUri = initiateLoginUri;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_post_logout_redirect_uri", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="post_logout_redirect_uri")
	public Set<String> getPostLogoutRedirectUris() {
		return postLogoutRedirectUris;
	}

	public void setPostLogoutRedirectUris(Set<String> postLogoutRedirectUri) {
		this.postLogoutRedirectUris = postLogoutRedirectUri;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_request_uri", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="request_uri")
	public Set<String> getRequestUris() {
		return requestUris;
	}

	public void setRequestUris(Set<String> requestUris) {
		this.requestUris = requestUris;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_at")
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public boolean isAutoApprove(String scope) {
		return false;
	}

	@Basic
	@Column(name = "clear_access_tokens_on_refresh")
	public boolean isClearAccessTokensOnRefresh() {
		return clearAccessTokensOnRefresh;
	}

	public void setClearAccessTokensOnRefresh(boolean clearAccessTokensOnRefresh) {
		this.clearAccessTokensOnRefresh = clearAccessTokensOnRefresh;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="client_claims_redirect_uri", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="redirect_uri")
	public Set<String> getClaimsRedirectUris() {
		return claimsRedirectUris;
	}

	public void setClaimsRedirectUris(Set<String> claimsRedirectUris) {
		this.claimsRedirectUris = claimsRedirectUris;
	}

	@Basic
	@Column(name = "software_statement")
	@Convert(converter = JWTStringConverter.class)
	public JWT getSoftwareStatement() {
		return softwareStatement;
	}

	public void setSoftwareStatement(JWT softwareStatement) {
		this.softwareStatement = softwareStatement;
	}

	@Basic
	@Column(name = "code_challenge_method")
	@Convert(converter = PKCEAlgorithmStringConverter.class)
	public PKCEAlgorithm getCodeChallengeMethod() {
		return codeChallengeMethod;
	}

	public void setCodeChallengeMethod(PKCEAlgorithm codeChallengeMethod) {
		this.codeChallengeMethod = codeChallengeMethod;
	}

	@Basic
	@Column(name="device_code_validity_seconds")
	public Integer getDeviceCodeValiditySeconds() {
		return deviceCodeValiditySeconds;
	}

	public void setDeviceCodeValiditySeconds(Integer deviceCodeValiditySeconds) {
		this.deviceCodeValiditySeconds = deviceCodeValiditySeconds;
	}

	@Basic
	@Column(name="software_id")
	public String getSoftwareId() {
		return softwareId;
	}

	public void setSoftwareId(String softwareId) {
		this.softwareId = softwareId;
	}

	@Basic
	@Column(name="software_version")
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

}
