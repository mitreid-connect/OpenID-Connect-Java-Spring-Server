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
package org.mitre.openid.connect.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
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

import org.mitre.oauth2.model.ClientDetailsEntity;

@Entity
@Table(name="approvedsite")
@NamedQueries({
	@NamedQuery(name = "ApprovedSite.getAll", query = "select a from ApprovedSite a"),
	@NamedQuery(name = "ApprovedSite.getByUserInfo", query = "select a from ApprovedSite a where a.userInfo = :approvedSiteUserInfo"),
	@NamedQuery(name = "ApprovedSite.getByClientDetails", query = "select a from ApprovedSite a where a.clientDetails = :approvedSiteClientDetails")
})
public class ApprovedSite {

    // unique id
    private Long id;
    
    // which user made the approval
	private UserInfo userInfo;
	
	// which OAuth2 client is this tied to
	private ClientDetailsEntity clientDetails;
	
	// when was this first approved?
	private Date creationDate;
	
	// when was this last accessed?
	private Date accessDate;
	
	// if this is a time-limited access, when does it run out?
	private Date timeoutDate;
	
	// what scopes have been allowed
	// this should include all information for what data to access
	private Set<String> allowedScopes;
	
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
	@JoinColumn(name="userinfo_id")
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
    @ManyToOne
	@JoinColumn(name="clientdetails_id")
    public ClientDetailsEntity getClientDetails() {
    	return clientDetails;
    }

	/**
     * @param clientDetails the clientDetails to set
     */
    public void setClientDetails(ClientDetailsEntity clientDetails) {
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
    @ElementCollection(fetch = FetchType.EAGER)
    public Set<String> getAllowedScopes() {
    	return allowedScopes;
    }

	/**
     * @param allowedScopes the allowedScopes to set
     */
    public void setAllowedScopes(Set<String> allowedScopes) {
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
