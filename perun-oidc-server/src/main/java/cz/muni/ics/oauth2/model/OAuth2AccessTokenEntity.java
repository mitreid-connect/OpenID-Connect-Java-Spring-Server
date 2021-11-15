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

import com.nimbusds.jwt.JWT;
import cz.muni.ics.oauth2.model.convert.JWTStringConverter;
import cz.muni.ics.openid.connect.model.ApprovedSite;
import cz.muni.ics.uma.model.Permission;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessTokenJackson2Deserializer;
import org.springframework.security.oauth2.common.OAuth2AccessTokenJackson2Serializer;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "access_token")
@NamedQueries({
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_ALL, query = "select a from OAuth2AccessTokenEntity a"),
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_EXPIRED_BY_DATE, query = "select a from OAuth2AccessTokenEntity a where a.expiration <= :" + OAuth2AccessTokenEntity.PARAM_DATE),
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_BY_REFRESH_TOKEN, query = "select a from OAuth2AccessTokenEntity a where a.refreshToken = :" + OAuth2AccessTokenEntity.PARAM_REFERSH_TOKEN),
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_BY_CLIENT, query = "select a from OAuth2AccessTokenEntity a where a.client = :" + OAuth2AccessTokenEntity.PARAM_CLIENT),
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_BY_TOKEN_VALUE, query = "select a from OAuth2AccessTokenEntity a where a.jwt = :" + OAuth2AccessTokenEntity.PARAM_TOKEN_VALUE),
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_BY_APPROVED_SITE, query = "select a from OAuth2AccessTokenEntity a where a.approvedSite = :" + OAuth2AccessTokenEntity.PARAM_APPROVED_SITE),
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_BY_RESOURCE_SET, query = "select a from OAuth2AccessTokenEntity a join a.permissions p where p.resourceSet.id = :" + OAuth2AccessTokenEntity.PARAM_RESOURCE_SET_ID),
	@NamedQuery(name = OAuth2AccessTokenEntity.QUERY_BY_NAME, query = "select r from OAuth2AccessTokenEntity r where r.authenticationHolder.userAuth.name = :" + OAuth2AccessTokenEntity.PARAM_NAME)
})
@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = OAuth2AccessTokenJackson2Serializer.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = OAuth2AccessTokenJackson2Deserializer.class)
public class OAuth2AccessTokenEntity implements OAuth2AccessToken {

	public static final String QUERY_BY_APPROVED_SITE = "OAuth2AccessTokenEntity.getByApprovedSite";
	public static final String QUERY_BY_TOKEN_VALUE = "OAuth2AccessTokenEntity.getByTokenValue";
	public static final String QUERY_BY_CLIENT = "OAuth2AccessTokenEntity.getByClient";
	public static final String QUERY_BY_REFRESH_TOKEN = "OAuth2AccessTokenEntity.getByRefreshToken";
	public static final String QUERY_EXPIRED_BY_DATE = "OAuth2AccessTokenEntity.getAllExpiredByDate";
	public static final String QUERY_ALL = "OAuth2AccessTokenEntity.getAll";
	public static final String QUERY_BY_RESOURCE_SET = "OAuth2AccessTokenEntity.getByResourceSet";
	public static final String QUERY_BY_NAME = "OAuth2AccessTokenEntity.getByName";

	public static final String PARAM_TOKEN_VALUE = "tokenValue";
	public static final String PARAM_CLIENT = "client";
	public static final String PARAM_REFERSH_TOKEN = "refreshToken";
	public static final String PARAM_DATE = "date";
	public static final String PARAM_RESOURCE_SET_ID = "rsid";
	public static final String PARAM_APPROVED_SITE = "approvedSite";
	public static final String PARAM_NAME = "name";

	public static final String ID_TOKEN_FIELD_NAME = "id_token";

	private Long id;
	private ClientDetailsEntity client;
	private AuthenticationHolderEntity authenticationHolder;
	private JWT jwtValue;
	private Date expiration;
	private String tokenType = OAuth2AccessToken.BEARER_TYPE;
	private OAuth2RefreshTokenEntity refreshToken;
	private Set<String> scope;
	private Set<Permission> permissions;
	private ApprovedSite approvedSite;
	private Map<String, Object> additionalInformation = new HashMap<>();

	public OAuth2AccessTokenEntity() { }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		return additionalInformation;
	}

	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
	public AuthenticationHolderEntity getAuthenticationHolder() {
		return authenticationHolder;
	}

	public void setAuthenticationHolder(AuthenticationHolderEntity authenticationHolder) {
		this.authenticationHolder = authenticationHolder;
	}

	@ManyToOne
	@JoinColumn(name = "client_id")
	public ClientDetailsEntity getClient() {
		return client;
	}

	public void setClient(ClientDetailsEntity client) {
		this.client = client;
	}

	@Override
	@Transient
	public String getValue() {
		return jwtValue.serialize();
	}

	@Override
	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	@Override
	@Basic
	@Column(name="token_type")
	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	@ManyToOne
	@JoinColumn(name="refresh_token_id")
	public OAuth2RefreshTokenEntity getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setRefreshToken(OAuth2RefreshToken refreshToken) {
		if (!(refreshToken instanceof OAuth2RefreshTokenEntity)) {
			throw new IllegalArgumentException("Not a storable refresh token entity!");
		}
		setRefreshToken((OAuth2RefreshTokenEntity)refreshToken);
	}

	@Override
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns=@JoinColumn(name="owner_id"), name="token_scope")
	public Set<String> getScope() {
		return scope;
	}

	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	@Override
	@Transient
	public boolean isExpired() {
		return getExpiration() != null && System.currentTimeMillis() > getExpiration().getTime();
	}

	@Basic
	@Column(name="token_value")
	@Convert(converter = JWTStringConverter.class)
	public JWT getJwt() {
		return jwtValue;
	}

	public void setJwt(JWT jwt) {
		this.jwtValue = jwt;
	}

	@Override
	@Transient
	public int getExpiresIn() {
		if (getExpiration() == null) {
			return -1; // no expiration time
		} else {
			if (isExpired()) {
				return 0; // has an expiration time and expired
			} else { // has an expiration time and not expired
				return (int) ((getExpiration().getTime() - System.currentTimeMillis()) / 1000);
			}
		}
	}

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "access_token_permissions", joinColumns = @JoinColumn(name = "access_token_id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id"))
	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	@ManyToOne
	@JoinColumn(name="approved_site_id")
	public ApprovedSite getApprovedSite() {
		return approvedSite;
	}

	public void setApprovedSite(ApprovedSite approvedSite) {
		this.approvedSite = approvedSite;
	}

	@Transient
	public void setIdToken(JWT idToken) {
		if (idToken != null) {
			additionalInformation.put(ID_TOKEN_FIELD_NAME, idToken.serialize());
		}
	}

}
