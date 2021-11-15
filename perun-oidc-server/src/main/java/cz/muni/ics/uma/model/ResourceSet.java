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
package cz.muni.ics.uma.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "resource_set")
@NamedQueries ({
	@NamedQuery(name = ResourceSet.QUERY_BY_OWNER, query = "select r from ResourceSet r where r.owner = :" + ResourceSet.PARAM_OWNER),
	@NamedQuery(name = ResourceSet.QUERY_BY_OWNER_AND_CLIENT, query = "select r from ResourceSet r where r.owner = :" + ResourceSet.PARAM_OWNER + " and r.clientId = :" + ResourceSet.PARAM_CLIENTID),
	@NamedQuery(name = ResourceSet.QUERY_BY_CLIENT, query = "select r from ResourceSet r where r.clientId = :" + ResourceSet.PARAM_CLIENTID),
	@NamedQuery(name = ResourceSet.QUERY_ALL, query = "select r from ResourceSet r")
})
public class ResourceSet {

	public static final String QUERY_BY_OWNER = "ResourceSet.queryByOwner";
	public static final String QUERY_BY_OWNER_AND_CLIENT = "ResourceSet.queryByOwnerAndClient";
	public static final String QUERY_BY_CLIENT = "ResourceSet.queryByClient";
	public static final String QUERY_ALL = "ResourceSet.queryAll";

	public static final String PARAM_OWNER = "owner";
	public static final String PARAM_CLIENTID = "clientId";

	private Long id;
	private String name;
	private String uri;
	private String type;
	private Set<String> scopes = new HashSet<>();
	private String iconUri;
	private String owner; // username of the person responsible for the registration (either directly or via OAuth token)
	private String clientId; // client id of the protected resource that registered this resource set via OAuth token

	private Collection<Policy> policies = new HashSet<>();

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
	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Basic
	@Column(name = "uri")
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Basic
	@Column(name = "rs_type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "scope")
	@CollectionTable(name = "resource_set_scope", joinColumns = @JoinColumn(name = "owner_id"))
	public Set<String> getScopes() {
		return scopes;
	}

	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}

	@Basic
	@Column(name = "icon_uri")
	public String getIconUri() {
		return iconUri;
	}

	public void setIconUri(String iconUri) {
		this.iconUri = iconUri;
	}

	@Basic
	@Column(name = "owner")
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Basic
	@Column(name = "client_id")
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "resource_set_id")
	public Collection<Policy> getPolicies() {
		return policies;
	}

	public void setPolicies(Collection<Policy> policies) {
		this.policies = policies;
	}

	@Override
	public String toString() {
		return "ResourceSet [id=" + id + ", name=" + name + ", uri=" + uri + ", type=" + type + ", scopes=" + scopes + ", iconUri=" + iconUri + ", owner=" + owner + ", clientId=" + clientId + ", policies=" + policies + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((iconUri == null) ? 0 : iconUri.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((policies == null) ? 0 : policies.hashCode());
		result = prime * result + ((scopes == null) ? 0 : scopes.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		ResourceSet other = (ResourceSet) obj;
		if (clientId == null) {
			if (other.clientId != null) {
				return false;
			}
		} else if (!clientId.equals(other.clientId)) {
			return false;
		}
		if (iconUri == null) {
			if (other.iconUri != null) {
				return false;
			}
		} else if (!iconUri.equals(other.iconUri)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (owner == null) {
			if (other.owner != null) {
				return false;
			}
		} else if (!owner.equals(other.owner)) {
			return false;
		}
		if (policies == null) {
			if (other.policies != null) {
				return false;
			}
		} else if (!policies.equals(other.policies)) {
			return false;
		}
		if (scopes == null) {
			if (other.scopes != null) {
				return false;
			}
		} else if (!scopes.equals(other.scopes)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
