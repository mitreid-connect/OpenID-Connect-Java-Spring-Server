package org.mitre.oauth2.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.springframework.security.oauth2.provider.OAuth2Authentication;

@Entity
@Table(name="authentication_holder")
@NamedQueries ({
	@NamedQuery(name = "AuthenticationHolder.getByAuthentication", query = "select a from AuthenticationHolder a where a.authentication = :authentication")
})
public class AuthenticationHolder {

	private Long id;
	
	private Long owner_id;
	
	private OAuth2Authentication authentication;
	
	public AuthenticationHolder() {
		
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	public Long getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(Long owner_id) {
		this.owner_id = owner_id;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	public OAuth2Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(OAuth2Authentication authentication) {
		this.authentication = authentication;
	}
	
	
	
}
