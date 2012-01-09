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

import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;

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
public class OAuth2RefreshTokenEntity extends ExpiringOAuth2RefreshToken {

	private ClientDetailsEntity client;

	private  Set<String> scope; // we save the scope issued to the refresh token so that we can reissue a new access token	
	
	/**
	 * 
	 */
	public OAuth2RefreshTokenEntity() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2RefreshToken#getValue()
     */
    @Override
    @Id
    @Column(name="id")
    public String getValue() {
	    // TODO Auto-generated method stub
	    return super.getValue();
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.OAuth2RefreshToken#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
	    // TODO Auto-generated method stub
	    super.setValue(value);
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken#getExpiration()
     */
    @Override
    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getExpiration() {
	    // TODO Auto-generated method stub
	    return super.getExpiration();
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken#setExpiration(java.util.Date)
     */
    @Override
    public void setExpiration(Date expiration) {
	    // TODO Auto-generated method stub
	    super.setExpiration(expiration);
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
    

    
}
