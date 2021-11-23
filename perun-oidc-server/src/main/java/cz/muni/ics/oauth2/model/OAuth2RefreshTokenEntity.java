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

import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.PARAM_CLIENT;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.PARAM_DATE;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.PARAM_NAME;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.PARAM_TOKEN_VALUE;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.QUERY_ALL;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.QUERY_BY_CLIENT;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.QUERY_BY_NAME;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.QUERY_BY_TOKEN_VALUE;
import static cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity.QUERY_EXPIRED_BY_DATE;

import com.nimbusds.jwt.JWT;
import cz.muni.ics.oauth2.model.convert.JWTStringConverter;
import java.util.Date;
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
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

/**
 * @author jricher
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name = "refresh_token")
@NamedQueries({
		@NamedQuery(name = QUERY_ALL,
				query = "SELECT r FROM OAuth2RefreshTokenEntity r"),
		@NamedQuery(name = QUERY_EXPIRED_BY_DATE,
				query = "SELECT r FROM OAuth2RefreshTokenEntity r " +
						"WHERE r.expiration <= :" + PARAM_DATE),
		@NamedQuery(name = QUERY_BY_CLIENT,
				query = "SELECT r FROM OAuth2RefreshTokenEntity r " +
						"WHERE r.client = :" + PARAM_CLIENT),
		@NamedQuery(name = QUERY_BY_TOKEN_VALUE,
				query = "SELECT r FROM OAuth2RefreshTokenEntity r " +
						"WHERE r.jwt = :" + PARAM_TOKEN_VALUE),
		@NamedQuery(name = QUERY_BY_NAME,
				query = "SELECT r FROM OAuth2RefreshTokenEntity r " +
						"WHERE r.authenticationHolder.userAuth.name = :" + PARAM_NAME)
})
public class OAuth2RefreshTokenEntity implements OAuth2RefreshToken {

	public static final String QUERY_BY_TOKEN_VALUE = "OAuth2RefreshTokenEntity.getByTokenValue";
	public static final String QUERY_BY_CLIENT = "OAuth2RefreshTokenEntity.getByClient";
	public static final String QUERY_EXPIRED_BY_DATE = "OAuth2RefreshTokenEntity.getAllExpiredByDate";
	public static final String QUERY_ALL = "OAuth2RefreshTokenEntity.getAll";
	public static final String QUERY_BY_NAME = "OAuth2RefreshTokenEntity.getByName";

	public static final String PARAM_TOKEN_VALUE = "tokenValue";
	public static final String PARAM_CLIENT = "client";
	public static final String PARAM_DATE = "date";
	public static final String PARAM_NAME = "name";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
	@CascadeOnDelete
	private AuthenticationHolderEntity authenticationHolder;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "client_id")
	private ClientDetailsEntity client;

	@Column(name = "token_value")
	@Convert(converter = JWTStringConverter.class)
	private JWT jwt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	private Date expiration;

	@Override
	@Transient
	public String getValue() {
		return jwt.serialize();
	}

	@Transient
	public boolean isExpired() {
		return getExpiration() != null && System.currentTimeMillis() > getExpiration().getTime();
	}

}
