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
/**
 *
 */
package cz.muni.ics.oauth2.model;

import static cz.muni.ics.oauth2.model.SystemScope.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jricher
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name = "system_scope")
@NamedQueries({
	@NamedQuery(name = QUERY_ALL,
			query = "SELECT s FROM SystemScope s ORDER BY s.id"),
	@NamedQuery(name = QUERY_BY_VALUE,
			query = "SELECT s FROM SystemScope s " +
					"WHERE s.value = :" + PARAM_VALUE)
})
public class SystemScope {

	public static final String QUERY_BY_VALUE = "SystemScope.getByValue";
	public static final String QUERY_ALL = "SystemScope.findAll";

	public static final String PARAM_VALUE = "value";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "scope")
	private String value;

	@Column(name = "description")
	private String description; // human-readable description

	@Column(name = "icon")
	private String icon; // class of the icon to display on the auth page

	@Column(name = "default_scope")
	private boolean defaultScope = false; // is this a default scope for newly-registered clients?

	@Column(name = "restricted")
	private boolean restricted = false; // is this scope restricted to admin-only registration access?

	public SystemScope(String value) {
		this.value = value;
	}

}
