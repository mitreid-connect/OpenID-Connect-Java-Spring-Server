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

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "access_token")
@NamedQueries({
	@NamedQuery(name = "OAuth2AccessTokenEntity.getAll", query = "select a from OAuth2AccessTokenEntity a"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByRefreshToken", query = "select a from OAuth2AccessTokenEntity a where a.refreshToken = :refreshToken"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByClient", query = "select a from OAuth2AccessTokenEntity a where a.client = :client"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByAuthentication", query = "select a from OAuth2AccessTokenEntity a where a.authenticationHolder.authentication = :authentication"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByIdToken", query = "select a from OAuth2AccessTokenEntity a where a.idToken = :idToken"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByTokenValue", query = "select a from OAuth2AccessTokenEntity a where a.value = :tokenValue")
})
//@JsonSerialize(using = OAuth2AccessTokenSerializer.class)
//@JsonDeserialize(using = OAuth2AccessTokenDeserializer.class)
public class OAuth2AccessTokenEntity implements OAuth2AccessToken {

	public static final String ID_TOKEN_SCOPE = "id-token";
	public static final String REGISTRATION_TOKEN_SCOPE = "registration-token";

	public static String ID_TOKEN = "id_token";

	private Long id;

	private ClientDetailsEntity client;

	private AuthenticationHolderEntity authenticationHolder; // the authentication that made this access

	private JWT jwtValue; // JWT-encoded access token value

	private OAuth2AccessTokenEntity idToken; // JWT-encoded OpenID Connect IdToken

	private Date expiration;

	private String tokenType = OAuth2AccessToken.BEARER_TYPE;

	private OAuth2RefreshTokenEntity refreshToken;

	private Set<String> scope;

	/**
	 * Create a new, blank access token
	 */
	public OAuth2AccessTokenEntity() {

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
	 * Get all additional information to be sent to the serializer. Inserts a copy of the IdToken (in JWT String form).
	 */
	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		Map<String, Object> map = new HashMap<String, Object>(); //super.getAdditionalInformation();
		if (getIdToken() != null) {
			map.put(ID_TOKEN, getIdTokenString());
		}
		return map;
	}

	/**
	 * The authentication in place when this token was created.
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
	 * @return the client
	 */
	@ManyToOne
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
	 * Get the string-encoded value of this access token.
	 */
	@Override
	@Basic
	@Column(name="token_value")
	public String getValue() {
		return jwtValue.serialize();
	}

	/**
	 * Set the "value" of this Access Token
	 * 
	 * @param value the JWT string
	 * @throws ParseException if "value" is not a properly formatted JWT string
	 */
	public void setValue(String value) throws ParseException {
		setJwt(JWTParser.parse(value));
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
		// force a pass through to the entity version
		setRefreshToken((OAuth2RefreshTokenEntity)refreshToken);
	}

	@Override
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(
			joinColumns=@JoinColumn(name="owner_id"),
			name="token_scope"
			)
	public Set<String> getScope() {
		return scope;
	}

	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	@Override
	@Transient
	public boolean isExpired() {
		return getExpiration() == null ? false : System.currentTimeMillis() > getExpiration().getTime();
	}

	/**
	 * @return the idToken
	 */
	@OneToOne(cascade=CascadeType.ALL) // one-to-one mapping for now
	@JoinColumn(name = "id_token_id")
	public OAuth2AccessTokenEntity getIdToken() {
		return idToken;
	}

	/**
	 * @param idToken the idToken to set
	 */
	public void setIdToken(OAuth2AccessTokenEntity idToken) {
		this.idToken = idToken;
	}

	/**
	 * @return the idTokenString
	 */
	@Transient
	public String getIdTokenString() {
		if (idToken != null) {
			return idToken.getValue(); // get the JWT string value of the id token entity
		} else {
			return null;
		}
	}

	/**
	 * @return the jwtValue
	 */
	@Transient
	public JWT getJwt() {
		return jwtValue;
	}

	/**
	 * @param jwtValue the jwtValue to set
	 */
	public void setJwt(JWT jwt) {
		this.jwtValue = jwt;
	}

	@Override
	@Transient
	public int getExpiresIn() {

		if (getExpiration() == null) {
			return -1; // no expiration time
		} else {
			int secondsRemaining = (int) ((getExpiration().getTime() - System.currentTimeMillis()) / 1000);
			if (isExpired()) {
				return 0; // has an expiration time and expired
			} else { // has an expiration time and not expired
				return secondsRemaining;
			}
		}
	}

}
