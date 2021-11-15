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
package cz.muni.ics.openid.connect.model;

import java.util.Date;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

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

	private Long id;
	private String userId;
	private String clientId;
	private Date creationDate;
	private Date accessDate;
	private Date timeoutDate;
	private Set<String> allowedScopes;

	public ApprovedSite() { }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name="user_id")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Basic
	@Column(name="client_id")
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name="creation_date")
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name="access_date")
	public Date getAccessDate() {
		return accessDate;
	}

	public void setAccessDate(Date accessDate) {
		this.accessDate = accessDate;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="approved_site_scope", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="scope")
	public Set<String> getAllowedScopes() {
		return allowedScopes;
	}

	public void setAllowedScopes(Set<String> allowedScopes) {
		this.allowedScopes = allowedScopes;
	}

	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name="timeout_date")
	public Date getTimeoutDate() {
		return timeoutDate;
	}

	public void setTimeoutDate(Date timeoutDate) {
		this.timeoutDate = timeoutDate;
	}

	@Transient
	public boolean isExpired() {
		if (getTimeoutDate() != null) {
			Date now = new Date();
			return now.after(getTimeoutDate());
		} else {
			return false;
		}
	}

}
