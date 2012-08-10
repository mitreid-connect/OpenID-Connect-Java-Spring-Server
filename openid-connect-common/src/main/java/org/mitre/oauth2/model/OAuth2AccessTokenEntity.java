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

import java.util.Date;
import java.util.HashMap;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.mitre.jwt.model.Jwt;
import org.mitre.openid.connect.model.IdToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

/**
 * @author jricher
 *
 */
@Entity
@Table(name="access_token")
@NamedQueries({
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByRefreshToken", query = "select a from OAuth2AccessTokenEntity a where a.refreshToken = :refreshToken"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByClient", query = "select a from OAuth2AccessTokenEntity a where a.client = :client"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getExpired", query = "select a from OAuth2AccessTokenEntity a where a.expiration is not null and a.expiration < current_timestamp"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByAuthentication", query = "select a from OAuth2AccessTokenEntity a where a.authenticationHolder.authentication = :authentication"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByTokenValue", query = "select a from OAuth2AccessTokenEntity a where a.value = :tokenValue")
})
//@JsonSerialize(using = OAuth2AccessTokenSerializer.class)
//@JsonDeserialize(using = OAuth2AccessTokenDeserializer.class)
public class OAuth2AccessTokenEntity implements OAuth2AccessToken {

	public static String ID_TOKEN = "id_token";
	
	private Long id;
	
	private ClientDetailsEntity client;
	
	private AuthenticationHolder authenticationHolder; // the authentication that made this access
	
	private Jwt jwtValue; // JWT-encoded access token value
	
	//TODO should not need this
	private String value;

	private IdToken idToken; // JWT-encoded OpenID Connect IdToken
	
	private Date expiration;

	private String tokenType = OAuth2AccessToken.BEARER_TYPE;

	private OAuth2RefreshTokenEntity refreshToken;

	private Set<String> scope;
	
	/**
	 * Create a new, blank access token
	 */
	public OAuth2AccessTokenEntity() {
		setJwt(new Jwt()); // give us a blank jwt to work with at least
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
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Get all additional information to be sent to the serializer. Inserts a copy of the IdToken (in JWT String form). 
	 */
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		Map<String, Object> map = new HashMap<String, Object>(); //super.getAdditionalInformation();
		map.put(ID_TOKEN, getIdTokenString());
		return map;
	}
	
	/**
	 * The authentication in place when this token was created.
     * @return the authentication
     */
	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
    public AuthenticationHolder getAuthenticationHolder() {
    	return authenticationHolder;
    }

	/**
     * @param authentication the authentication to set
     */
    public void setAuthenticationHolder(AuthenticationHolder authenticationHolder) {
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
    @Basic
    @Column(name="token_value")
    public String getValue() {
    	this.value = jwtValue.toString();
	    return value;
    }

    /**
     * Set the "value" of this Access Token
     * 
     * @param value the JWT string
     * @throws IllegalArgumentException if "value" is not a properly formatted JWT string
     */
    public void setValue(String value) {
    	this.value = value;
    	setJwt(Jwt.parse(value));
    }

    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getExpiration() {
	    return expiration;
    }

    public void setExpiration(Date expiration) {
	    this.expiration = expiration;
    }

    @Basic
    @Column(name="token_type")
    public String getTokenType() {
	    return tokenType;
    }

    public void setTokenType(String tokenType) {
	   this.tokenType = tokenType;
    }

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
    		// TODO: make a copy constructor instead....
    		throw new IllegalArgumentException("Not a storable refresh token entity!");
    	}
    	// force a pass through to the entity version
    	setRefreshToken((OAuth2RefreshTokenEntity)refreshToken);
    }
	
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

    @Transient
	public boolean isExpired() {
		return getExpiration() == null ? false : System.currentTimeMillis() > getExpiration().getTime();
	}
    
	/**
	 * This is transient b/c the IdToken is not serializable. Instead,
	 * the toString of the IdToken is persisted in idTokenString 
	 * @return the idToken
	 */
    @Transient
	public IdToken getIdToken() {
		return idToken;
	}

	/**
	 * @param idToken the idToken to set
	 */
	public void setIdToken(IdToken idToken) {
		this.idToken = idToken;
	}
	
	/**
	 * @return the idTokenString
	 */
	@Basic
	@Column(name="id_token_string")
	public String getIdTokenString() {
		if (idToken != null) {
			return idToken.toString();
		} else {
			return null;
		}
	}

	/**
	 * @param idTokenString the idTokenString to set
     * @throws IllegalArgumentException if "value" is not a properly formatted JWT string
	 */
	public void setIdTokenString(String idTokenString) {
		this.idToken = IdToken.parse(idTokenString);
	}

	/**
	 * @return the jwtValue
	 */
	@Transient
	public Jwt getJwt() {
		return jwtValue;
	}

	/**
	 * @param jwtValue the jwtValue to set
	 */
	public void setJwt(Jwt jwt) {
		this.jwtValue = jwt;
		this.value = jwt.toString();
	}

	@Override
	public int getExpiresIn() {
		// TODO Auto-generated method stub
		return 0;
	}
}
