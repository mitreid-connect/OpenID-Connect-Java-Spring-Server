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

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
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

import org.mitre.oauth2.model.ClientDetailsEntity;

/**
 * Indicator that login to a site should be automatically granted 
 * without user interaction.
 * @author jricher, aanganes
 *
 */
@Entity
@Table(name="whitelistedsite")
@NamedQueries({
	@NamedQuery(name = "WhitelistedSite.getAll", query = "select w from WhitelistedSite w"), 
	@NamedQuery(name = "WhitelistedSite.getByClientDetails", query = "select w from WhitelistedSite w where w.clientDetails = :clientDetails"),
	@NamedQuery(name = "WhitelistedSite.getByCreatoruserId", query = "select w from WhitelistedSite w where w.creatorUserId = :userId")
})
public class WhitelistedSite {

    // unique id
    private Long id;
    
    // Reference to the admin user who created this entry
	private String creatorUserId;
    
	// which OAuth2 client is this tied to
	private ClientDetailsEntity clientDetails;
	
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
	 * @return the allowedScopes
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
    		name="allowed_scopes",
    		joinColumns=@JoinColumn(name="owner_id")
    )
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
	public String getCreatorUserId() {
		return creatorUserId;
	}

	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}
}
