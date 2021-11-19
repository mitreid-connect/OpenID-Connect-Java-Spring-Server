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

package cz.muni.ics.oauth2.model;

import cz.muni.ics.oauth2.model.convert.SimpleGrantedAuthorityStringConverter;
import cz.muni.ics.oidc.saml.SamlPrincipal;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
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
import javax.persistence.Transient;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;

/**
 * This class stands in for an original Authentication object.
 *
 * @author jricher
 */
@Entity
@Table(name="saved_user_auth")
@Slf4j
@ToString
public class SavedUserAuthentication implements Authentication {

	private static final long serialVersionUID = -1804249963940323488L;

	private Long id;
	private String name;
	private Collection<GrantedAuthority> authorities;
	private boolean authenticated;
	private String acr;

	public SavedUserAuthentication(Authentication src) {
		setName(src.getName());
		setAuthorities(new HashSet<>(src.getAuthorities()));
		setAuthenticated(src.isAuthenticated());
		if (src instanceof SavedUserAuthentication) {
			this.setAcr(((SavedUserAuthentication) src).getAcr());
		} else if (src instanceof ExpiringUsernameAuthenticationToken) {
			ExpiringUsernameAuthenticationToken token = (ExpiringUsernameAuthenticationToken) src;
			this.acr = ((SamlPrincipal) token.getPrincipal()).getSamlCredential()
					.getAuthenticationAssertion()
					.getAuthnStatements().stream()
					.map(AuthnStatement::getAuthnContext)
					.map(AuthnContext::getAuthnContextClassRef)
					.map(AuthnContextClassRef::getAuthnContextClassRef)
					.collect(Collectors.joining());
		}
	}

	public SavedUserAuthentication() { }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@Basic
	@Column(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="saved_user_auth_authority", joinColumns=@JoinColumn(name="owner_id"))
	@Convert(converter = SimpleGrantedAuthorityStringConverter.class)
	@Column(name="authority")
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Basic
	@Column(name = "acr")
	public String getAcr() {
		return acr;
	}

	public void setAcr(String acr) {
		this.acr = acr;
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

}
