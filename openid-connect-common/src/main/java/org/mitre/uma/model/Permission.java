/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author  jricher
 */
@Entity
@Table(name = "permission")
public class Permission {

	private String uuid;
	private String hostUuid;
	private ResourceSet resourceSet;
	private Set<String> scopes;

	public Permission() {
		this.uuid = UUID.randomUUID().toString();
	}
	
	public Permission(String uuid) {
		this.uuid = uuid;
	}	
	
	@Id
	@Column(name = "uuid")	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Basic
	@Column(name = "host_uuid")	
	public String getHostUuid() {
		return hostUuid;
	}

	public void setHostUuid(String hostUuid) {
		this.hostUuid = hostUuid;
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
			joinColumns = @JoinColumn(name = "permission_uuid")
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