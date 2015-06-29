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
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * A set of claims required to fulfill a given permission.
 * 
 * @author jricher
 *
 */
@Entity
@Table(name = "policy")
public class Policy {

	private Long id;
	private String name;
	private Collection<Claim> claimsRequired;
	private Set<String> scopes;
	
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
	 * @return the name
	 */
	@Basic
	@Column(name = "name")
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the claimsRequired
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(
			name = "claim_to_policy",
			joinColumns = @JoinColumn(name = "policy_id"),
			inverseJoinColumns = @JoinColumn(name = "claim_id")
	)
	public Collection<Claim> getClaimsRequired() {
		return claimsRequired;
	}

	/**
	 * @param claimsRequired the claimsRequired to set
	 */
	public void setClaimsRequired(Collection<Claim> claimsRequired) {
		this.claimsRequired = claimsRequired;
	}
	
	/**
	 * @return the scopes
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "scope")
	@CollectionTable(
		name = "policy_scope",
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
	
}
