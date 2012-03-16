/**
 * 
 */
package org.mitre.oauth2.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.mitre.jwt.model.Jwt;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

/**
 * @author jricher
 *
 */
@Entity
@Table(name="refreshtoken")
@NamedQueries({
	@NamedQuery(name = "OAuth2RefreshTokenEntity.getByClient", query = "select r from OAuth2RefreshTokenEntity r where r.client = :client"),
	@NamedQuery(name = "OAuth2RefreshTokenEntity.getExpired", query = "select r from OAuth2RefreshTokenEntity r where r.expiration is not null and r.expiration < current_timestamp")
})
public class OAuth2RefreshTokenEntity extends OAuth2RefreshToken {

	private ClientDetailsEntity client;

	//JWT-encoded representation of this access token entity
	private Jwt jwt;
	
	// our refresh tokens might expire
	private Date expiration;

	private  Set<String> scope; // we save the scope issued to the refresh token so that we can reissue a new access token	
	
	/**
	 * 
	 */
	public OAuth2RefreshTokenEntity() {
		// we ignore the superclass's Value field
		super(null);
		setJwt(new Jwt()); // start with a blank JWT value
	}

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2RefreshToken#getValue()
     */
	/**
	 * Get the JWT-encoded value of this token
	 */
    @Override
    @Id
    @Column(name="id")
    public String getValue() {
	    // TODO Auto-generated method stub
	    return jwt.toString();
    }

    /**
     * Set the value of this token as a string. Parses the string into a JWT.
     * @param value
     * @throws IllegalArgumentException if the value is not a valid JWT string
     */
    public void setValue(String value) {
	    // TODO Auto-generated method stub
	    setJwt(Jwt.parse(value));
    }

    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
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
     * @return the scope
     */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			joinColumns=@JoinColumn(name="owner_id"),
			name="scope"
	)
    public Set<String> getScope() {
    	return scope;
    }

	/**
     * @param scope the scope to set
     */
    public void setScope(Set<String> scope) {
    	this.scope = scope;
    }
    
    /**
     * Get the JWT object directly
     * @return the jwt
     */
    @Transient
    public Jwt getJwt() {
    	return jwt;
    }
    
    /**
     * @param jwt the jwt to set
     */
    public void setJwt(Jwt jwt) {
    	this.jwt = jwt;
    }
    
}
