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
package cz.muni.ics.oauth2.model;

import cz.muni.ics.oauth2.model.convert.SerializableStringConverter;
import cz.muni.ics.oauth2.model.convert.SimpleGrantedAuthorityStringConverter;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

@Entity
@Table(name = "authentication_holder")
@NamedQueries ({
	@NamedQuery(name = AuthenticationHolderEntity.QUERY_ALL, query = "select a from AuthenticationHolderEntity a"),
	@NamedQuery(name = AuthenticationHolderEntity.QUERY_GET_UNUSED, query = "select a from AuthenticationHolderEntity a where " +
			"a.id not in (select t.authenticationHolder.id from OAuth2AccessTokenEntity t) and " +
			"a.id not in (select r.authenticationHolder.id from OAuth2RefreshTokenEntity r) and " +
			"a.id not in (select c.authenticationHolder.id from AuthorizationCodeEntity c)")
})
public class AuthenticationHolderEntity {

	public static final String QUERY_GET_UNUSED = "AuthenticationHolderEntity.getUnusedAuthenticationHolders";
	public static final String QUERY_ALL = "AuthenticationHolderEntity.getAll";

	private Long id;
	private SavedUserAuthentication userAuth;
	private Collection<GrantedAuthority> authorities;
	private Set<String> resourceIds;
	private boolean approved;
	private String redirectUri;
	private Set<String> responseTypes;
	private Map<String, Serializable> extensions;
	private String clientId;
	private Set<String> scope;
	private Map<String, String> requestParameters;

	public AuthenticationHolderEntity() { }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Transient
	public OAuth2Authentication getAuthentication() {
		// TODO: memoize this
		return new OAuth2Authentication(createOAuth2Request(), getUserAuth());
	}

	private OAuth2Request createOAuth2Request() {
		return new OAuth2Request(requestParameters, clientId, authorities, approved, scope, resourceIds, redirectUri, responseTypes, extensions);
	}

	public void setAuthentication(OAuth2Authentication authentication) {
		// pull apart the request and save its bits
		OAuth2Request o2Request = authentication.getOAuth2Request();
		setAuthorities(o2Request.getAuthorities() == null ? null : new HashSet<>(o2Request.getAuthorities()));
		setClientId(o2Request.getClientId());
		setExtensions(o2Request.getExtensions() == null ? null : new HashMap<>(o2Request.getExtensions()));
		setRedirectUri(o2Request.getRedirectUri());
		setRequestParameters(o2Request.getRequestParameters() == null ? null : new HashMap<>(o2Request.getRequestParameters()));
		setResourceIds(o2Request.getResourceIds() == null ? null : new HashSet<>(o2Request.getResourceIds()));
		setResponseTypes(o2Request.getResponseTypes() == null ? null : new HashSet<>(o2Request.getResponseTypes()));
		setScope(o2Request.getScope() == null ? null : new HashSet<>(o2Request.getScope()));
		setApproved(o2Request.isApproved());

		if (authentication.getUserAuthentication() != null) {
			this.userAuth = new SavedUserAuthentication(authentication.getUserAuthentication());
		} else {
			this.userAuth = null;
		}
	}

	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "user_auth_id")
	public SavedUserAuthentication getUserAuth() {
		return userAuth;
	}

	public void setUserAuth(SavedUserAuthentication userAuth) {
		this.userAuth = userAuth;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="authentication_holder_authority", joinColumns=@JoinColumn(name="owner_id"))
	@Convert(converter = SimpleGrantedAuthorityStringConverter.class)
	@Column(name="authority")
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="authentication_holder_resource_id", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="resource_id")
	public Set<String> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(Set<String> resourceIds) {
		this.resourceIds = resourceIds;
	}

	@Basic
	@Column(name="approved")
	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@Basic
	@Column(name="redirect_uri")
	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="authentication_holder_response_type", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="response_type")
	public Set<String> getResponseTypes() {
		return responseTypes;
	}

	public void setResponseTypes(Set<String> responseTypes) {
		this.responseTypes = responseTypes;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="authentication_holder_extension", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="val")
	@MapKeyColumn(name="extension")
	@Convert(converter= SerializableStringConverter.class)
	public Map<String, Serializable> getExtensions() {
		return extensions;
	}

	public void setExtensions(Map<String, Serializable> extensions) {
		this.extensions = extensions;
	}

	@Basic
	@Column(name="client_id")
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="authentication_holder_scope", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="scope")
	public Set<String> getScope() {
		return scope;
	}

	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="authentication_holder_request_parameter", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="val")
	@MapKeyColumn(name="param")
	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String> requestParameters) {
		this.requestParameters = requestParameters;
	}

}
