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

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.mitre.oauth2.model.convert.JsonElementStringConverter;

import com.google.gson.JsonElement;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "claim")
public class Claim {

	private Long id;
	private String name;
	private String friendlyName;
	private String claimType;
	private JsonElement value;
	private Set<String> claimTokenFormat;
	private Set<String> issuer;
	
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
	 * @return the friendlyName
	 */
	@Basic
	@Column(name = "friendly_name")
	public String getFriendlyName() {
		return friendlyName;
	}
	/**
	 * @param friendlyName the friendlyName to set
	 */
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	/**
	 * @return the claimType
	 */
	@Basic
	@Column(name = "claim_type")
	public String getClaimType() {
		return claimType;
	}
	/**
	 * @param claimType the claimType to set
	 */
	public void setClaimType(String claimType) {
		this.claimType = claimType;
	}
	
	/**
	 * @return the claimTokenFormat
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "claim_token_format")
	@CollectionTable(
		name = "claim_token_format",
		joinColumns = @JoinColumn(name = "owner_id")
	)
	public Set<String> getClaimTokenFormat() {
		return claimTokenFormat;
	}
	/**
	 * @param claimTokenFormat the claimTokenFormat to set
	 */
	public void setClaimTokenFormat(Set<String> claimTokenFormat) {
		this.claimTokenFormat = claimTokenFormat;
	}

	/**
	 * @return the issuer
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "issuer")
	@CollectionTable(
		name = "claim_issuer",
		joinColumns = @JoinColumn(name = "owner_id")
	)
	public Set<String> getIssuer() {
		return issuer;
	}
	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(Set<String> issuer) {
		this.issuer = issuer;
	}

	/**
	 * @return the value
	 */
	@Basic
	@Column(name = "claim_value")
	@Convert(converter = JsonElementStringConverter.class)
	public JsonElement getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(JsonElement value) {
		this.value = value;
	}
}
