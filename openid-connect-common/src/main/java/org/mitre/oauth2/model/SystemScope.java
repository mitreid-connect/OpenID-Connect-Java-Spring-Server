/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.oauth2.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "system_scope")
@NamedQueries({
	@NamedQuery(name = SystemScope.QUERY_ALL, query = "select s from SystemScope s ORDER BY s.id"),
	@NamedQuery(name = SystemScope.QUERY_BY_VALUE, query = "select s from SystemScope s WHERE s.value = :" + SystemScope.PARAM_VALUE)
})
public class SystemScope {

	public static final String QUERY_BY_VALUE = "SystemScope.getByValue";
	public static final String QUERY_ALL = "SystemScope.findAll";

	public static final String PARAM_VALUE = "value";

	private Long id;
	private String value; // scope value
	private String description; // human-readable description
	private String icon; // class of the icon to display on the auth page
	private boolean defaultScope = false; // is this a default scope for newly-registered clients?
	private boolean restricted = false; // is this scope restricted to admin-only registration access?

	/**
	 * Make a blank system scope with no value
	 */
	public SystemScope() {

	}

	/**
	 * Make a system scope with the given scope value
	 * @param value
	 */
	public SystemScope(String value) {
		this.value = value;
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
	 * @return the value
	 */
	@Basic
	@Column(name = "scope")
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the description
	 */
	@Basic
	@Column(name = "description")
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the icon
	 */
	@Basic
	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}
	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the defaultScope
	 */
	@Basic
	@Column(name = "default_scope")
	public boolean isDefaultScope() {
		return defaultScope;
	}

	/**
	 * @param defaultScope the defaultScope to set
	 */
	public void setDefaultScope(boolean defaultScope) {
		this.defaultScope = defaultScope;
	}

	/**
	 * @return the restricted
	 */
	@Basic
	@Column(name = "restricted")
	public boolean isRestricted() {
		return restricted;
	}

	/**
	 * @param restricted the restricted to set
	 */
	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (defaultScope ? 1231 : 1237);
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (restricted ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SystemScope other = (SystemScope) obj;
		if (defaultScope != other.defaultScope) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (icon == null) {
			if (other.icon != null) {
				return false;
			}
		} else if (!icon.equals(other.icon)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (restricted != other.restricted) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SystemScope [id=" + id + ", value=" + value + ", description="
				+ description + ", icon=" + icon + ", defaultScope="
				+ defaultScope + ", restricted=" + restricted + "]";
	}

}
