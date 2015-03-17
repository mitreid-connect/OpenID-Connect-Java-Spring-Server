/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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

package org.mitre.uma.model;

import java.util.Collection;
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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 
 * An UMA permission, used in the protection API.
 * 
 * @author jricher
 *
 */
@Entity
@Table(name = "permission")
@NamedQueries({
	@NamedQuery(name = Permission.QUERY_TICKET, query = "select p from Permission p where p.ticket = :" + Permission.PARAM_TICKET)
})
public class Permission {

	public static final String QUERY_TICKET = "Permission.queryByTicket";
	public static final String PARAM_TICKET = "ticket";
	
	
	private Long id;
	private ResourceSet resourceSet;
	private Set<String> scopes;
	private String ticket;
	private Date expiration;
	private Collection<Claim> claimsSupplied;
	
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
	 * @return the resourceSet
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "resource_set_id")
	public ResourceSet getResourceSet() {
		return resourceSet;
	}
	
	/**
	 * @param resourceSet the resourceSet to set
	 */
	public void setResourceSet(ResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}
	
	/**
	 * @return the scopes
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "scope")
	@CollectionTable(
		name = "permission_scope",
		joinColumns = @JoinColumn(name = "owner_id")
	)
	public Set<String> getScopes() {
		return scopes;
	}
	
	/**
	 * @param scopes the scopes to set
	 */
	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}
	
	/**
	 * @return the ticket
	 */
	@Basic
	@Column(name = "ticket")
	public String getTicket() {
		return ticket;
	}
	
	/**
	 * @param ticket the ticket to set
	 */
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	/**
	 * @return the expiration
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	public Date getExpiration() {
		return expiration;
	}

	/**
	 * @param expiration the expiration to set
	 */
	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	/**
	 * @return the claimsSupplied
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "permission_id")
	public Collection<Claim> getClaimsSupplied() {
		return claimsSupplied;
	}

	/**
	 * @param claimsSupplied the claimsSupplied to set
	 */
	public void setClaimsSupplied(Collection<Claim> claimsSupplied) {
		this.claimsSupplied = claimsSupplied;
	}
	
	
}
