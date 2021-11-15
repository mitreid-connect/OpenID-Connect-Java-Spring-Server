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

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * Entity class for authorization codes
 *
 * @author aanganes
 */
@Entity
@Table(name = "authorization_code")
@NamedQueries({
	@NamedQuery(name = AuthorizationCodeEntity.QUERY_BY_VALUE, query = "select a from AuthorizationCodeEntity a where a.code = :code"),
	@NamedQuery(name = AuthorizationCodeEntity.QUERY_EXPIRATION_BY_DATE, query = "select a from AuthorizationCodeEntity a where a.expiration <= :" + AuthorizationCodeEntity.PARAM_DATE)
})
public class AuthorizationCodeEntity {

	public static final String QUERY_BY_VALUE = "AuthorizationCodeEntity.getByValue";
	public static final String QUERY_EXPIRATION_BY_DATE = "AuthorizationCodeEntity.expirationByDate";

	public static final String PARAM_DATE = "date";

	private Long id;
	private String code;
	private AuthenticationHolderEntity authenticationHolder;
	private Date expiration;

	public AuthorizationCodeEntity() { }

	public AuthorizationCodeEntity(String code, AuthenticationHolderEntity authenticationHolder, Date expiration) {
		this.code = code;
		this.authenticationHolder = authenticationHolder;
		this.expiration = expiration;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
	public AuthenticationHolderEntity getAuthenticationHolder() {
		return authenticationHolder;
	}

	public void setAuthenticationHolder(AuthenticationHolderEntity authenticationHolder) {
		this.authenticationHolder = authenticationHolder;
	}

	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

}
