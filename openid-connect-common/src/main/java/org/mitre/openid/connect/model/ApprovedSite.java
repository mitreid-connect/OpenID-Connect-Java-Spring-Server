/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
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
package org.mitre.openid.connect.model;

import java.util.Date;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;

import com.google.common.collect.Sets;

@Entity
@Table(name="approved_site")
@NamedQueries({
	@NamedQuery(name = ApprovedSite.QUERY_ALL, query = "select a from ApprovedSite a"),
	@NamedQuery(name = ApprovedSite.QUERY_BY_USER_ID, query = "select a from ApprovedSite a where a.userId = :" + ApprovedSite.PARAM_USER_ID),
	@NamedQuery(name = ApprovedSite.QUERY_BY_CLIENT_ID, query = "select a from ApprovedSite a where a.clientId = :" + ApprovedSite.PARAM_CLIENT_ID),
	@NamedQuery(name = ApprovedSite.QUERY_BY_CLIENT_ID_AND_USER_ID, query = "select a from ApprovedSite a where a.clientId = :" + ApprovedSite.PARAM_CLIENT_ID + " and a.userId = :" + ApprovedSite.PARAM_USER_ID)
})
public class ApprovedSite {

	public static final String QUERY_BY_CLIENT_ID_AND_USER_ID = "ApprovedSite.getByClientIdAndUserId";
	public static final String QUERY_BY_CLIENT_ID = "ApprovedSite.getByClientId";
	public static final String QUERY_BY_USER_ID = "ApprovedSite.getByUserId";
	public static final String QUERY_ALL = "ApprovedSite.getAll";

	public static final String PARAM_CLIENT_ID = "clientId";
	public static final String PARAM_USER_ID = "userId";

	// unique id
	private Long id;

	// which user made the approval
	private String userId;

	// which OAuth2 client is this tied to
	private String clientId;

	// when was this first approved?
	private Date creationDate;

	// when was this last accessed?
	private Date accessDate;

	// if this is a time-limited access, when does it run out?
	private Date timeoutDate;

	// what scopes have been allowed
	// this should include all information for what data to access
	private Set<String> allowedScopes;

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
	 * @return the userInfo
	 */
	@Basic
	@Column(name="user_id")
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userInfo the userInfo to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the clientId
	 */
	@Basic
	@Column(name="client_id")
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the creationDate
	 */
	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name="creation_date")
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
	@Column(name="access_date")
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
	@CollectionTable(
			name="approved_site_scope",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="scope")
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
	@Column(name="timeout_date")
	public Date getTimeoutDate() {
		return timeoutDate;
	}

	/**
	 * @param timeoutDate the timeoutDate to set
	 */
	public void setTimeoutDate(Date timeoutDate) {
		this.timeoutDate = timeoutDate;
	}

	/**
	 * Has this approval expired?
	 * @return
	 */
	@Transient
	public boolean isExpired() {
		if (getTimeoutDate() != null) {
			Date now = new Date();
			if (now.after(getTimeoutDate())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
