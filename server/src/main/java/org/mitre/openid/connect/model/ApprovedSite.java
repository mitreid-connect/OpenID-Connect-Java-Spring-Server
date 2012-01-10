package org.mitre.openid.connect.model;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.security.oauth2.provider.ClientDetails;

@Entity
@Table(name="approvedsite")
public class ApprovedSite {

    // unique id
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    // which user made the approval
	@ManyToOne
	@JoinColumn(name="userinfo_id")
	private UserInfo userInfo;
	
	// which OAuth2 client is this tied to
	@ManyToOne
	@JoinColumn(name="clientdetails_id")
	private ClientDetails clientDetails;
	
	// when was this first approved?
	@Temporal(TemporalType.DATE)
	private Date creationDate;
	
	// when was this last accessed?
	@Temporal(TemporalType.DATE)
	private Date accessDate;
	
	// if this is a time-limited access, when does it run out?
	@Temporal(TemporalType.DATE)
	private Date timeoutDate;
	
	// what scopes have been allowed
	// this should include all information for what data to access
	@OneToMany(mappedBy = "approvedsite")
	private Collection<String> allowedScopes;
	
	// TODO: should we store the OAuth2 tokens and IdTokens here?
	
	/**
	 * Empty constructor
	 */
	public ApprovedSite() {

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
     * @return the userInfo
     */
    @ManyToOne
    public UserInfo getUserInfo() {
    	return userInfo;
    }

	/**
     * @param userInfo the userInfo to set
     */
    public void setUserInfo(UserInfo userInfo) {
    	this.userInfo = userInfo;
    }

	/**
     * @return the clientDetails
     */
    public ClientDetails getClientDetails() {
    	return clientDetails;
    }

	/**
     * @param clientDetails the clientDetails to set
     */
    public void setClientDetails(ClientDetails clientDetails) {
    	this.clientDetails = clientDetails;
    }

	/**
     * @return the creationDate
     */
    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getCreationDate() {
    	return creationDate;
    }

	/**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
    	this.creationDate = creationDate;
    }

	/**
     * @return the accessDate
     */
    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getAccessDate() {
    	return accessDate;
    }

	/**
     * @param accessDate the accessDate to set
     */
    public void setAccessDate(Date accessDate) {
    	this.accessDate = accessDate;
    }

	/**
     * @return the allowedScopes
     */
    @OneToMany
    public Collection<String> getAllowedScopes() {
    	return allowedScopes;
    }

	/**
     * @param allowedScopes the allowedScopes to set
     */
    public void setAllowedScopes(Collection<String> allowedScopes) {
    	this.allowedScopes = allowedScopes;
    }

	/**
     * @return the timeoutDate
     */
    @Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getTimeoutDate() {
    	return timeoutDate;
    }

	/**
     * @param timeoutDate the timeoutDate to set
     */
    public void setTimeoutDate(Date timeoutDate) {
    	this.timeoutDate = timeoutDate;
    }
	
	
	
}
