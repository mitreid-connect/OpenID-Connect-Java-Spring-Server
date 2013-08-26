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
package org.bbplus;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="trusted_registry")
@NamedQueries({
	@NamedQuery(name = "TrustedRegistry.getAll", query = "select r from TrustedRegistry r")
})
public class TrustedRegistry {

	// Base URI of trusted registry
	@Id
	private String uri;

	/**
	 * Empty constructor
	 */
	public TrustedRegistry() {

	}

	/**
	 * @return the uri
	 */
	@Basic
	@Column(name="uri")
	public String getValue() {
		return uri;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String uri) {
		this.uri = uri;
	}
	

}
