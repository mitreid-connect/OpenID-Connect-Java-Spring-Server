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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
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
public class OAuth2AccessTokenEntity extends OAuth2AccessToken {

	private ClientDetailsEntity client;
	
	private OAuth2Authentication authentication; // the authentication that made this access
	
	/**
	 * 
	 */
	public OAuth2AccessTokenEntity() {
		super(null);
	}
	
	
	/**
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
    @Override
    @Id
    @Column(name="id")
    public String getValue() {
	    // TODO Auto-generated method stub
	    return super.getValue();
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2AccessToken#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
	    // TODO Auto-generated method stub
	    super.setValue(value);
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


}
