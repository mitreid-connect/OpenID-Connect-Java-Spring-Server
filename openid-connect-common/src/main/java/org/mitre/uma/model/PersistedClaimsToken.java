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

package org.mitre.uma.model;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "persisted_claims_token")
@NamedQueries ({
	@NamedQuery(name = PersistedClaimsToken.QUERY_BY_VALUE, query = "select p from PersistedClaimsToken p where p.value = :" + PersistedClaimsToken.PARAM_VALUE),
})
public class PersistedClaimsToken {

	public static final String QUERY_BY_VALUE = "PersistedClaimsToken.queryByValue";
	
	public static final String PARAM_VALUE = "value";
	
	private Long id;
	private String clientId;
	private Date expiration;
	private Collection<Claim> claimsSupplied;
	private String value;
	
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
	@Column(name = "client_id")
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
	@JoinTable(
			name = "claim_to_permission_ticket",
			joinColumns = @JoinColumn(name = "permission_ticket_id"),
			inverseJoinColumns = @JoinColumn(name = "claim_id")
	)
	public Collection<Claim> getClaimsSupplied() {
		return claimsSupplied;
	}

	/**
	 * @param claimsSupplied the claimsSupplied to set
	 */
	public void setClaimsSupplied(Collection<Claim> claimsSupplied) {
		this.claimsSupplied = claimsSupplied;
	}

	/**
	 * @return the value
	 */
	@Basic
	@Column(name = "token_value")
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	
}
