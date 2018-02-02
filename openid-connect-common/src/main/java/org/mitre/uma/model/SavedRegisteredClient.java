/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.uma.model.convert.RegisteredClientStringConverter;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "saved_registered_client")
public class SavedRegisteredClient {

	private Long id;
	private String issuer;
	private RegisteredClient registeredClient;

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
	 *
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the issuer
	 */
	@Basic
	@Column(name = "issuer")
	public String getIssuer() {
		return issuer;
	}

	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * @return the registeredClient
	 */
	@Basic
	@Column(name = "registered_client")
	@Convert(converter = RegisteredClientStringConverter.class)
	public RegisteredClient getRegisteredClient() {
		return registeredClient;
	}

	/**
	 * @param registeredClient the registeredClient to set
	 */
	public void setRegisteredClient(RegisteredClient registeredClient) {
		this.registeredClient = registeredClient;
	}



}
