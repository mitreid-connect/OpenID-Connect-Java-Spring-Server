/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.oauth2.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
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
import javax.persistence.Transient;

import org.mitre.oauth2.model.convert.JWTStringConverter;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import com.nimbusds.jwt.JWT;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "refresh_token")
@NamedQueries({
	@NamedQuery(name = OAuth2RefreshTokenEntity.QUERY_ALL, query = "select r from OAuth2RefreshTokenEntity r"),
	@NamedQuery(name = OAuth2RefreshTokenEntity.QUERY_EXPIRED_BY_DATE, query = "select r from OAuth2RefreshTokenEntity r where r.expiration <= :" + OAuth2RefreshTokenEntity.PARAM_DATE),
	@NamedQuery(name = OAuth2RefreshTokenEntity.QUERY_BY_CLIENT, query = "select r from OAuth2RefreshTokenEntity r where r.client = :" + OAuth2RefreshTokenEntity.PARAM_CLIENT),
	@NamedQuery(name = OAuth2RefreshTokenEntity.QUERY_BY_TOKEN_VALUE, query = "select r from OAuth2RefreshTokenEntity r where r.jwt = :" + OAuth2RefreshTokenEntity.PARAM_TOKEN_VALUE)
})
public class OAuth2RefreshTokenEntity implements OAuth2RefreshToken {

	public static final String QUERY_BY_TOKEN_VALUE = "OAuth2RefreshTokenEntity.getByTokenValue";
	public static final String QUERY_BY_CLIENT = "OAuth2RefreshTokenEntity.getByClient";
	public static final String QUERY_EXPIRED_BY_DATE = "OAuth2RefreshTokenEntity.getAllExpiredByDate";
	public static final String QUERY_ALL = "OAuth2RefreshTokenEntity.getAll";

	public static final String PARAM_TOKEN_VALUE = "tokenValue";
	public static final String PARAM_CLIENT = "client";
	public static final String PARAM_DATE = "date";

	private Long id;

	private AuthenticationHolderEntity authenticationHolder;

	private ClientDetailsEntity client;

	//JWT-encoded representation of this access token entity
	private JWT jwt;

	// our refresh tokens might expire
	private Date expiration;

	/**
	 *
	 */
	public OAuth2RefreshTokenEntity() {

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
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * The authentication in place when the original access token was
	 * created
	 *
	 * @return the authentication
	 */
	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
	public AuthenticationHolderEntity getAuthenticationHolder() {
		return authenticationHolder;
	}

	/**
	 * @param authentication the authentication to set
	 */
	public void setAuthenticationHolder(AuthenticationHolderEntity authenticationHolder) {
		this.authenticationHolder = authenticationHolder;
	}

	/**
	 * Get the JWT-encoded value of this token
	 */
	@Override
	@Transient
	public String getValue() {
		return jwt.serialize();
	}

	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	public Date getExpiration() {
		return expiration;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken#setExpiration(java.util.Date)
	 */

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	/**
	 * Has this token expired?
	 * @return true if it has a timeout set and the timeout has passed
	 */
	@Transient
	public boolean isExpired() {
		return getExpiration() == null ? false : System.currentTimeMillis() > getExpiration().getTime();
	}

	/**
	 * @return the client
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "client_id")
	public ClientDetailsEntity getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(ClientDetailsEntity client) {
		this.client = client;
	}

	/**
	 * Get the JWT object directly
	 * @return the jwt
	 */
	@Basic
	@Column(name="token_value")
	@Convert(converter = JWTStringConverter.class)
	public JWT getJwt() {
		return jwt;
	}

	/**
	 * @param jwt the jwt to set
	 */
	public void setJwt(JWT jwt) {
		this.jwt = jwt;
	}

}
