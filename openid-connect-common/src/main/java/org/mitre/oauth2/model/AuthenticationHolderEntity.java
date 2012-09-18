package org.mitre.oauth2.model;

import javax.persistence.Basic;
import javax.persistence.Column;
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
	@NamedQuery(name = "AuthenticationHolderEntity.getByAuthentication", query = "select a from AuthenticationHolderEntity a where a.authentication = :authentication")
})
public class AuthenticationHolderEntity {

	private Long id;
	
	private Long ownerId;
	
	private OAuth2Authentication authentication;
	
	public AuthenticationHolderEntity() {
		
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
	@Column(name="owner_id")
	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long owner_id) {
		this.ownerId = owner_id;
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
