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

package org.mitre.oauth2.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mitre.oauth2.model.convert.SimpleGrantedAuthorityStringConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * This class stands in for an original Authentication object.
 *
 * @author jricher
 *
 */
@Entity
@Table(name="saved_user_auth")
public class SavedUserAuthentication implements Authentication {

	private static final long serialVersionUID = -1804249963940323488L;

	private String id;
	
	private String hostUuid;

	private String name;

	private Collection<GrantedAuthority> authorities;

	private boolean authenticated;

	private String sourceClass;

	/**
	 * Create a Saved Auth from an existing Auth token
	 */
	public SavedUserAuthentication(Authentication src) {
		this.id = UUID.randomUUID().toString();
		setName(src.getName());
		setAuthorities(new HashSet<>(src.getAuthorities()));
		setAuthenticated(src.isAuthenticated());

		if (src instanceof SavedUserAuthentication) {
			// if we're copying in a saved auth, carry over the original class name
			setSourceClass(((SavedUserAuthentication) src).getSourceClass());
		} else {
			setSourceClass(src.getClass().getName());
		}
	}
	
	public SavedUserAuthentication() {
		this.id = UUID.randomUUID().toString();
	}

	public SavedUserAuthentication(String uuid) {
		this.id = uuid;
	}
	
	@Id
	@Column(name = "uuid")
	public String getId() {
		return id;
	}

	public void setId(String uuid) {
		this.id = uuid;
	}
	
	@Basic
	@Column(name = "host_uuid")
	public String getHostUuid() {
		return hostUuid;
	}

	public void setHostUuid(String hostUuid) {
		this.hostUuid = hostUuid;
	}

	@Override
	@Basic
	@Column(name="name")
	public String getName() {
		return name;
	}

	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="saved_user_auth_authority",
			joinColumns=@JoinColumn(name="user_auth_uuid")
			)
	@Convert(converter = SimpleGrantedAuthorityStringConverter.class)
	@Column(name="authority")
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	@Transient
	public Object getCredentials() {
		return "";
	}

	@Override
	@Transient
	public Object getDetails() {
		return null;
	}

	@Override
	@Transient
	public Object getPrincipal() {
		return getName();
	}

	@Override
	@Basic
	@Column(name="authenticated")
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.authenticated = isAuthenticated;
	}

	/**
	 * @return the sourceClass
	 */
	@Basic
	@Column(name="source_class")
	public String getSourceClass() {
		return sourceClass;
	}

	/**
	 * @param sourceClass the sourceClass to set
	 */
	public void setSourceClass(String sourceClass) {
		this.sourceClass = sourceClass;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param authorities the authorities to set
	 */
	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}


}
