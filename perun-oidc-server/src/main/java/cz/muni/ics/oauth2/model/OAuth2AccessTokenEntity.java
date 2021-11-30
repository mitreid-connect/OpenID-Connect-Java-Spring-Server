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

import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.PARAM_APPROVED_SITE;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.PARAM_CLIENT;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.PARAM_DATE;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.PARAM_NAME;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.PARAM_REFRESH_TOKEN;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.PARAM_TOKEN_VALUE;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.QUERY_ALL;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.QUERY_BY_APPROVED_SITE;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.QUERY_BY_CLIENT;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.QUERY_BY_NAME;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.QUERY_BY_REFRESH_TOKEN;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.QUERY_BY_TOKEN_VALUE;
import static cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity.QUERY_EXPIRED_BY_DATE;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nimbusds.jwt.JWT;
import cz.muni.ics.oauth2.model.convert.JWTStringConverter;
import cz.muni.ics.openid.connect.model.ApprovedSite;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessTokenJackson2Deserializer;
import org.springframework.security.oauth2.common.OAuth2AccessTokenJackson2Serializer;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

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
@Table(name = "access_token")
@NamedQueries({
	@NamedQuery(name = QUERY_ALL,
				query = "SELECT a FROM OAuth2AccessTokenEntity a"),
	@NamedQuery(name = QUERY_EXPIRED_BY_DATE,
				query = "SELECT a FROM OAuth2AccessTokenEntity a " +
						"WHERE a.expiration <= :" + PARAM_DATE),
	@NamedQuery(name = QUERY_BY_REFRESH_TOKEN,
				query = "SELECT a FROM OAuth2AccessTokenEntity a " +
						"WHERE a.refreshToken = :" + PARAM_REFRESH_TOKEN),
	@NamedQuery(name = QUERY_BY_CLIENT,
				query = "SELECT a FROM OAuth2AccessTokenEntity a " +
						"WHERE a.client = :" + PARAM_CLIENT),
	@NamedQuery(name = QUERY_BY_TOKEN_VALUE,
				query = "SELECT a FROM OAuth2AccessTokenEntity a " +
						"WHERE a.jwtValue = :" + PARAM_TOKEN_VALUE),
	@NamedQuery(name = QUERY_BY_APPROVED_SITE,
				query = "SELECT a FROM OAuth2AccessTokenEntity a " +
						"WHERE a.approvedSite = :" + PARAM_APPROVED_SITE),
	@NamedQuery(name = QUERY_BY_NAME,
				query = "SELECT r FROM OAuth2AccessTokenEntity r " +
						"WHERE r.authenticationHolder.userAuth.name = :" + PARAM_NAME)
})
@JsonSerialize(using = OAuth2AccessTokenJackson2Serializer.class)
@JsonDeserialize(using = OAuth2AccessTokenJackson2Deserializer.class)
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
	public static final String PARAM_REFRESH_TOKEN = "refreshToken";
	public static final String PARAM_DATE = "date";
	public static final String PARAM_RESOURCE_SET_ID = "rsid";
	public static final String PARAM_APPROVED_SITE = "approvedSite";
	public static final String PARAM_NAME = "name";

	public static final String ID_TOKEN_FIELD_NAME = "id_token";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "client_id")
	private ClientDetailsEntity client;

	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
	@CascadeOnDelete
	private AuthenticationHolderEntity authenticationHolder;

	@Column(name = "token_value")
	@Convert(converter = JWTStringConverter.class)
	private JWT jwtValue;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	private Date expiration;

	@Column(name = "token_type")
	private String tokenType = OAuth2AccessToken.BEARER_TYPE;

	@ManyToOne
	@JoinColumn(name = "refresh_token_id")
	private OAuth2RefreshTokenEntity refreshToken;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "token_scope", joinColumns = @JoinColumn(name = "owner_id"))
	@CascadeOnDelete
	private Set<String> scope;

	@ManyToOne
	@JoinColumn(name = "approved_site_id")
	private ApprovedSite approvedSite;

	@Transient
	private Map<String, Object> additionalInformation = new HashMap<>();

	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		return additionalInformation;
	}

	@Override
	@Transient
	public String getValue() {
		return jwtValue.serialize();
	}

	@Override
	public Date getExpiration() {
		return expiration;
	}

	@Override
	public String getTokenType() {
		return tokenType;
	}

	@Override
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
	public Set<String> getScope() {
		return scope;
	}

	@Override
	@Transient
	public boolean isExpired() {
		return getExpiration() != null && System.currentTimeMillis() > getExpiration().getTime();
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

	@Transient
	public void setIdToken(JWT idToken) {
		if (idToken != null) {
			additionalInformation.put(ID_TOKEN_FIELD_NAME, idToken.serialize());
		}
	}

}
