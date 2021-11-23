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

import static cz.muni.ics.oauth2.model.AuthenticationHolderEntity.QUERY_ALL;
import static cz.muni.ics.oauth2.model.AuthenticationHolderEntity.QUERY_GET_UNUSED;

import cz.muni.ics.oauth2.model.convert.SerializableStringConverter;
import cz.muni.ics.oauth2.model.convert.SimpleGrantedAuthorityStringConverter;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name = "authentication_holder")
@NamedQueries ({
	@NamedQuery(name = QUERY_ALL,
				query = "SELECT a FROM AuthenticationHolderEntity a"),
	@NamedQuery(name = QUERY_GET_UNUSED,
				query = "SELECT a FROM AuthenticationHolderEntity a " +
						"WHERE a.id NOT IN (SELECT t.authenticationHolder.id FROM OAuth2AccessTokenEntity t) " +
						"AND a.id NOT IN (SELECT r.authenticationHolder.id FROM OAuth2RefreshTokenEntity r) " +
						"AND a.id NOT IN (SELECT c.authenticationHolder.id FROM AuthorizationCodeEntity c)")
})
public class AuthenticationHolderEntity {

	public static final String QUERY_GET_UNUSED = "AuthenticationHolderEntity.getUnusedAuthenticationHolders";
	public static final String QUERY_ALL = "AuthenticationHolderEntity.getAll";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "user_auth_id")
	@CascadeOnDelete
	private SavedUserAuthentication userAuth;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "authentication_holder_authority", joinColumns = @JoinColumn(name = "owner_id"))
	@Convert(converter = SimpleGrantedAuthorityStringConverter.class)
	@Column(name = "authority")
	@CascadeOnDelete
	private Collection<GrantedAuthority> authorities;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "authentication_holder_resource_id", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "resource_id")
	@CascadeOnDelete
	private Set<String> resourceIds;

	@Column(name = "approved")
	private boolean approved;

	@Column(name = "redirect_uri")
	private String redirectUri;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "authentication_holder_response_type", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "response_type")
	@CascadeOnDelete
	private Set<String> responseTypes;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "authentication_holder_extension", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "val")
	@MapKeyColumn(name = "extension")
	@Convert(converter = SerializableStringConverter.class)
	@CascadeOnDelete
	private Map<String, Serializable> extensions;

	@Column(name = "client_id")
	private String clientId;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "authentication_holder_scope", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "scope")
	@CascadeOnDelete
	private Set<String> scope;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "authentication_holder_request_parameter", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "val")
	@MapKeyColumn(name = "param")
	@CascadeOnDelete
	private Map<String, String> requestParameters;

	@Transient
	public OAuth2Authentication getAuthentication() {
		// TODO: memoize this
		return new OAuth2Authentication(createOAuth2Request(), getUserAuth());
	}

	public void setAuthentication(OAuth2Authentication authentication) {
		// pull apart the request and save its bits
		OAuth2Request o2Request = authentication.getOAuth2Request();
		setAuthorities(convertToSetOrNull((Set<GrantedAuthority>) o2Request.getAuthorities()));
		setClientId(o2Request.getClientId());
		setExtensions(convertToMapOrNull(o2Request.getExtensions()));
		setRedirectUri(o2Request.getRedirectUri());
		setRequestParameters(convertToMapOrNull(o2Request.getRequestParameters()));
		setResourceIds(convertToSetOrNull(o2Request.getResourceIds()));
		setResponseTypes(convertToSetOrNull(o2Request.getResponseTypes()));
		setScope(convertToSetOrNull(o2Request.getScope()));
		setApproved(o2Request.isApproved());

		if (authentication.getUserAuthentication() != null) {
			this.userAuth = new SavedUserAuthentication(authentication.getUserAuthentication());
		} else {
			this.userAuth = null;
		}
	}

	private <T> Set<T> convertToSetOrNull(Collection<T> obj) {
		return obj == null ? null: new HashSet<>(obj);
	}

	private <T, S> Map<T, S> convertToMapOrNull(Map<T, S> obj) {
		return obj == null ? null : new HashMap<>(obj);
	}

	private OAuth2Request createOAuth2Request() {
		return new OAuth2Request(requestParameters, clientId, authorities, approved, scope, resourceIds, redirectUri, responseTypes, extensions);
	}

}
