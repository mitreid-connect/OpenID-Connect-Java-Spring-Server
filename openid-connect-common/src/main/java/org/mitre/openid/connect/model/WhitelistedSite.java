/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.openid.connect.model;

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

/**
 * Indicator that login to a site should be automatically granted
 * without user interaction.
 * @author jricher, aanganes
 *
 */
@Entity
@Table(name="whitelisted_site")
@NamedQueries({
	@NamedQuery(name = WhitelistedSite.QUERY_ALL, query = "select w from WhitelistedSite w"),
	@NamedQuery(name = WhitelistedSite.QUERY_BY_CLIENT_ID, query = "select w from WhitelistedSite w where w.clientId = :" + WhitelistedSite.PARAM_CLIENT_ID),
	@NamedQuery(name = WhitelistedSite.QUERY_BY_CREATOR, query = "select w from WhitelistedSite w where w.creatorUserId = :" + WhitelistedSite.PARAM_USER_ID)
})
public class WhitelistedSite {

	public static final String QUERY_BY_CREATOR = "WhitelistedSite.getByCreatoruserId";
	public static final String QUERY_BY_CLIENT_ID = "WhitelistedSite.getByClientId";
	public static final String QUERY_ALL = "WhitelistedSite.getAll";

	public static final String PARAM_USER_ID = "userId";
	public static final String PARAM_CLIENT_ID = "clientId";

	// unique id
	private Long id;

	// Reference to the admin user who created this entry
	private String creatorUserId;

	// which OAuth2 client is this tied to
	private String clientId;

	// what scopes be allowed by default
	// this should include all information for what data to access
	private Set<String> allowedScopes;

	/**
	 * Empty constructor
	 */
	public WhitelistedSite() {

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
	 * @return the allowedScopes
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="whitelisted_site_scope",
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

	@Basic
	@Column(name="creator_user_id")
	public String getCreatorUserId() {
		return creatorUserId;
	}

	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}
}
