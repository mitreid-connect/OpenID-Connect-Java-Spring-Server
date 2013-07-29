/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
 ******************************************************************************/
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

/**
 * Entity class for authorization codes
 * 
 * @author aanganes
 *
 */
@Entity
@Table(name = "authorization_code")
@NamedQueries({
	@NamedQuery(name = "AuthorizationCodeEntity.getByValue", query = "select a from AuthorizationCodeEntity a where a.code = :code")
})
public class AuthorizationCodeEntity {

	private Long id;

	private String code;

	private OAuth2Authentication authentication;

	/**
	 * Default constructor.
	 */
	public AuthorizationCodeEntity() {

	}

	/**
	 * Create a new AuthorizationCodeEntity with the given code and AuthorizationRequestHolder.
	 * 
	 * @param code 			the authorization code
	 * @param authRequest	the AuthoriztionRequestHolder associated with the original code request
	 */
	public AuthorizationCodeEntity(String code, OAuth2Authentication authRequest) {
		this.code = code;
		this.authentication = authRequest;
	}

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
	 * @return the code
	 */
	@Basic
	@Column(name = "code")
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the authentication
	 */
	@Lob
	@Basic(fetch=FetchType.EAGER)
	@Column(name="authentication")
	public OAuth2Authentication getAuthentication() {
		return authentication;
	}

	/**
	 * @param authentication the authentication to set
	 */
	public void setAuthentication(OAuth2Authentication authentication) {
		this.authentication = authentication;
	}

}
