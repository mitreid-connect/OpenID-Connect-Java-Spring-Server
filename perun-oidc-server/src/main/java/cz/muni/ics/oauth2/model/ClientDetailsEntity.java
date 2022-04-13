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

import static cz.muni.ics.oauth2.model.ClientDetailsEntity.PARAM_CLIENT_ID;
import static cz.muni.ics.oauth2.model.ClientDetailsEntity.QUERY_ALL;
import static cz.muni.ics.oauth2.model.ClientDetailsEntity.QUERY_BY_CLIENT_ID;

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
import cz.muni.ics.oauth2.model.enums.AppType;
import cz.muni.ics.oauth2.model.enums.AuthMethod;
import cz.muni.ics.oauth2.model.enums.SubjectType;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * @author jricher
 *
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name = "client_details")
@NamedQueries({
	@NamedQuery(name = QUERY_ALL,
				query = "SELECT c FROM ClientDetailsEntity c"),
	@NamedQuery(name = QUERY_BY_CLIENT_ID,
				query = "SELECT c FROM ClientDetailsEntity c " +
						"WHERE c.clientId = :" + PARAM_CLIENT_ID)
})
public class ClientDetailsEntity implements ClientDetails {

	public static final String QUERY_BY_CLIENT_ID = "ClientDetailsEntity.getByClientId";
	public static final String QUERY_ALL = "ClientDetailsEntity.findAll";

	public static final String PARAM_CLIENT_ID = "clientId";

	private static final int DEFAULT_ID_TOKEN_VALIDITY_SECONDS = 600;

	private static final long serialVersionUID = -1617727085733786296L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "client_name")
	private String clientName;

	@Column(name = "client_description")
	private String clientDescription = "";

	@Column(name = "client_id")
	private String clientId = null;

	@Column(name = "client_secret")
	private String clientSecret = null;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_redirect_uri", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "redirect_uri")
	@CascadeOnDelete
	private Set<String> redirectUris = new HashSet<>();

	@Column(name = "client_uri")
	private String clientUri;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_contact", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "contact")
	@CascadeOnDelete
	private Set<String> contacts = new HashSet<>();

	@Column(name = "tos_uri")
	private String tosUri;

	@Enumerated(EnumType.STRING)
	@Column(name = "token_endpoint_auth_method")
	private AuthMethod tokenEndpointAuthMethod = AuthMethod.SECRET_BASIC;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_scope", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "scope")
	@CascadeOnDelete
	private Set<String> scope = new HashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_grant_type", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "grant_type")
	@CascadeOnDelete
	private Set<String> grantTypes = new HashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_response_type", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "response_type")
	@CascadeOnDelete
	private Set<String> responseTypes = new HashSet<>();

	@Column(name = "policy_uri")
	private String policyUri;

	@Column(name = "jwks_uri")
	private String jwksUri;

	@Column(name = "jwks")
	@Convert(converter = JWKSetStringConverter.class)
	private JWKSet jwks;

	@Column(name = "software_id")
	private String softwareId;

	@Column(name = "software_version")
	private String softwareVersion;

	@Enumerated(EnumType.STRING)
	@Column(name = "application_type")
	private AppType applicationType;

	@Column(name = "sector_identifier_uri")
	private String sectorIdentifierUri;

	@Enumerated(EnumType.STRING)
	@Column(name = "subject_type")
	private SubjectType subjectType;

	@Column(name = "request_object_signing_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	private JWSAlgorithm requestObjectSigningAlg = null;

	@Column(name = "user_info_signed_response_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	private JWSAlgorithm userInfoSignedResponseAlg = null;

	@Column(name = "user_info_encrypted_response_alg")
	@Convert(converter = JWEAlgorithmStringConverter.class)
	private JWEAlgorithm userInfoEncryptedResponseAlg = null;

	@Column(name = "user_info_encrypted_response_enc")
	@Convert(converter = JWEEncryptionMethodStringConverter.class)
	private EncryptionMethod userInfoEncryptedResponseEnc = null;

	@Column(name = "id_token_signed_response_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	private JWSAlgorithm idTokenSignedResponseAlg = null;

	@Column(name = "id_token_encrypted_response_alg")
	@Convert(converter = JWEAlgorithmStringConverter.class)
	private JWEAlgorithm idTokenEncryptedResponseAlg = null;

	@Column(name = "id_token_encrypted_response_enc")
	@Convert(converter = JWEEncryptionMethodStringConverter.class)
	private EncryptionMethod idTokenEncryptedResponseEnc = null;

	@Column(name = "token_endpoint_auth_signing_alg")
	@Convert(converter = JWSAlgorithmStringConverter.class)
	private JWSAlgorithm tokenEndpointAuthSigningAlg = null;

	@Column(name = "default_max_age")
	private Integer defaultMaxAge;

	@Column(name = "require_auth_time")
	private Boolean requireAuthTime;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_default_acr_value", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "default_acr_value")
	@CascadeOnDelete
	private Set<String> defaultACRvalues;

	@Column(name = "initiate_login_uri")
	private String initiateLoginUri;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_post_logout_redirect_uri", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "post_logout_redirect_uri")
	@CascadeOnDelete
	private Set<String> postLogoutRedirectUris = new HashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_request_uri", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "request_uri")
	@CascadeOnDelete
	private Set<String> requestUris = new HashSet<>();;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_authority", joinColumns = @JoinColumn(name = "owner_id"))
	@Convert(converter = SimpleGrantedAuthorityStringConverter.class)
	@Column(name = "authority")
	@CascadeOnDelete
	private Set<GrantedAuthority> authorities = new HashSet<>();

	@Column(name = "access_token_validity_seconds")
	private Integer accessTokenValiditySeconds = 0;

	@Column(name = "refresh_token_validity_seconds")
	private Integer refreshTokenValiditySeconds = 0;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_resource", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "resource_id")
	@CascadeOnDelete
	private Set<String> resourceIds = new HashSet<>();

	@Column(name = "reuse_refresh_tokens")
	private boolean reuseRefreshToken = true;

	@Column(name = "dynamically_registered")
	private boolean dynamicallyRegistered = false;

	@Column(name = "allow_introspection")
	private boolean allowIntrospection = false;

	@Column(name = "id_token_validity_seconds")
	private Integer idTokenValiditySeconds = DEFAULT_ID_TOKEN_VALIDITY_SECONDS;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "clear_access_tokens_on_refresh")
	private boolean clearAccessTokensOnRefresh = true;

	@Column(name = "device_code_validity_seconds")
	private Integer deviceCodeValiditySeconds = 0;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "client_claims_redirect_uri", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "redirect_uri")
	@CascadeOnDelete
	private Set<String> claimsRedirectUris = new HashSet<>();

	@Column(name = "software_statement")
	@Convert(converter = JWTStringConverter.class)
	private JWT softwareStatement;

	@Column(name = "code_challenge_method")
	@Convert(converter = PKCEAlgorithmStringConverter.class)
	private PKCEAlgorithm codeChallengeMethod;

	@Transient
	private Map<String, Object> additionalInformation = new HashMap<>();

	@PrePersist
	@PreUpdate
	private void prePersist() {
		if (getIdTokenValiditySeconds() == null) {
			setIdTokenValiditySeconds(DEFAULT_ID_TOKEN_VALIDITY_SECONDS);
		}
	}

	@Column(name = "accepted_tos")
	private boolean acceptedTos;

	@Column(name = "jurisdiction")
	private String jurisdiction;

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	@Override
	public Set<String> getScope() {
		return scope;
	}

	@Override
	public Set<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	@Override
	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	@Override
	public Set<String> getResourceIds() {
		return resourceIds;
	}

	@Override
	public boolean isAutoApprove(String scope) {
		return false;
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

	@Override
	@Transient
	public Set<String> getAuthorizedGrantTypes() {
		return getGrantTypes();
	}

	@Override
	@Transient
	public Set<String> getRegisteredRedirectUri() {
		return getRedirectUris();
	}

	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		return this.additionalInformation;
	}

	@Transient
	public boolean isAllowRefresh() {
		if (grantTypes != null) {
			return getAuthorizedGrantTypes().contains("refresh_token");
		} else {
			return false; // if there are no grants, we can't be refreshing them, can we?
		}
	}

}
