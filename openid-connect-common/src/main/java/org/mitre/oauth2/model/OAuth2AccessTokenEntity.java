/**
 * 
 */
package org.mitre.oauth2.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.mitre.jwt.model.Jwt;
import org.mitre.openid.connect.model.IdToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessTokenDeserializer;
import org.springframework.security.oauth2.common.OAuth2AccessTokenSerializer;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * @author jricher
 *
 */
@Entity
@Table(name="accesstoken")
@NamedQueries({
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByRefreshToken", query = "select a from OAuth2AccessTokenEntity a where a.refreshToken = :refreshToken"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getByClient", query = "select a from OAuth2AccessTokenEntity a where a.client = :client"),
	@NamedQuery(name = "OAuth2AccessTokenEntity.getExpired", query = "select a from OAuth2AccessTokenEntity a where a.expiration is not null and a.expiration < current_timestamp")
})
@JsonSerialize(using = OAuth2AccessTokenSerializer.class)
@JsonDeserialize(using = OAuth2AccessTokenDeserializer.class)
public class OAuth2AccessTokenEntity extends OAuth2AccessToken {

	public static String ID_TOKEN = "id_token";
	
	private ClientDetailsEntity client;
	
	private OAuth2Authentication authentication; // the authentication that made this access
	
	// JWT-encoded access token value
	private Jwt jwtValue;

	// JWT-encoded OpenID Connect IdToken
	private IdToken idToken;
	
	/**
	 * Create a new, blank access token
	 */
	public OAuth2AccessTokenEntity() {
		// we ignore the "value" field in the superclass because we can't cleanly override it
		super(null);
		setJwt(new Jwt()); // give us a blank jwt to work with at least
		setIdToken(new IdToken()); // and a blank IdToken
	}
	
	/**
	 * Get all additional information to be sent to the serializer. Inserts a copy of the IdToken (in JWT String form). 
	 */
	@Override
	@Transient
	public Map<String, Object> getAdditionalInformation() {
		Map<String, Object> map = super.getAdditionalInformation();
		map.put(ID_TOKEN, getIdTokenString());
		return map;
	}
	
	
	
	/**
	 * The authentication in place when this token was created.
     * @return the authentication
     */
	@Lob
	@Basic
    public OAuth2Authentication getAuthentication() {
    	return authentication;
    }


	/**
     * @param authentication the authentication to set
     */
    public void setAuthentication(OAuth2Authentication authentication) {
    	this.authentication = authentication;
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
	
	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#getValue()
     */
    /**
     * Get the string-encoded value of this access token. 
     */
    @Override
    @Id
    @Column(name="id")
    public String getValue() {
	    return jwtValue.toString();
    }

    /**
     * Set the "value" of this Access Token
     * 
     * @param value the JWT string
     * @throws IllegalArgumentException if "value" is not a properly formatted JWT string
     */
    public void setValue(String value) {
    	setJwt(Jwt.parse(value));
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#getExpiration()
     */
    @Override
    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getExpiration() {
	    // TODO Auto-generated method stub
	    return super.getExpiration();
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#setExpiration(java.util.Date)
     */
    @Override
    public void setExpiration(Date expiration) {
	    // TODO Auto-generated method stub
	    super.setExpiration(expiration);
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#getTokenType()
     */
    @Override
    @Basic
    public String getTokenType() {
	    // TODO Auto-generated method stub
	    return super.getTokenType();
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#setTokenType(java.lang.String)
     */
    @Override
    public void setTokenType(String tokenType) {
	    // TODO Auto-generated method stub
	    super.setTokenType(tokenType);
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#getRefreshToken()
     */
    @Override
    @ManyToOne
    @JoinColumn(name="refresh_token_id")
    public OAuth2RefreshTokenEntity getRefreshToken() {
	    // TODO Auto-generated method stub
	    return (OAuth2RefreshTokenEntity) super.getRefreshToken();
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#setRefreshToken(org.springframework.security.oauth2.common.OAuth2RefreshToken)
     */
    public void setRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
	    // TODO Auto-generated method stub
	    super.setRefreshToken(refreshToken);
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#setRefreshToken(org.springframework.security.oauth2.common.OAuth2RefreshToken)
     */
    @Override
    public void setRefreshToken(OAuth2RefreshToken refreshToken) {
    	if (!(refreshToken instanceof OAuth2RefreshTokenEntity)) {
    		// TODO: make a copy constructor instead....
    		throw new IllegalArgumentException("Not a storable refresh token entity!");
    	}
    	// force a pass through to the entity version
    	setRefreshToken((OAuth2RefreshTokenEntity)refreshToken);
    }
	
	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#getScope()
     */
    @Override
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(
    		joinColumns=@JoinColumn(name="owner_id"),
    		name="scope"
    )
    public Set<String> getScope() {
	    // TODO Auto-generated method stub
	    return super.getScope();
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#setScope(java.util.Set)
     */
    @Override
    public void setScope(Set<String> scope) {
	    // TODO Auto-generated method stub
	    super.setScope(scope);
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
	public String getIdTokenString() {
		return idToken.toString();
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
	}
}
