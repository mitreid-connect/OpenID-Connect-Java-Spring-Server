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

import com.google.gson.JsonElement;
import cz.muni.ics.oauth2.model.convert.JsonElementStringConverter;
import java.util.Set;
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

/**
 * @author jricher
 */
@Entity
@Table(name = "claim")
public class Claim {

	private Long id;
	private String name;
	private String friendlyName;
	private String claimType;
	private JsonElement value;
	private Set<String> claimTokenFormat;
	private Set<String> issuer;

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
	@Column(name = "friendly_name")
	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	@Basic
	@Column(name = "claim_type")
	public String getClaimType() {
		return claimType;
	}

	public void setClaimType(String claimType) {
		this.claimType = claimType;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "claim_token_format")
	@CollectionTable(name = "claim_token_format", joinColumns = @JoinColumn(name = "owner_id"))
	public Set<String> getClaimTokenFormat() {
		return claimTokenFormat;
	}

	public void setClaimTokenFormat(Set<String> claimTokenFormat) {
		this.claimTokenFormat = claimTokenFormat;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "issuer")
	@CollectionTable(name = "claim_issuer", joinColumns = @JoinColumn(name = "owner_id"))
	public Set<String> getIssuer() {
		return issuer;
	}

	public void setIssuer(Set<String> issuer) {
		this.issuer = issuer;
	}

	@Basic
	@Column(name = "claim_value")
	@Convert(converter = JsonElementStringConverter.class)
	public JsonElement getValue() {
		return value;
	}

	public void setValue(JsonElement value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Claim [id=" + id + ", name=" + name + ", friendlyName=" + friendlyName + ", claimType=" + claimType + ", value=" + value + ", claimTokenFormat=" + claimTokenFormat + ", issuer=" + issuer + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((claimTokenFormat == null) ? 0 : claimTokenFormat.hashCode());
		result = prime * result + ((claimType == null) ? 0 : claimType.hashCode());
		result = prime * result + ((friendlyName == null) ? 0 : friendlyName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

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
		Claim other = (Claim) obj;
		if (claimTokenFormat == null) {
			if (other.claimTokenFormat != null) {
				return false;
			}
		} else if (!claimTokenFormat.equals(other.claimTokenFormat)) {
			return false;
		}
		if (claimType == null) {
			if (other.claimType != null) {
				return false;
			}
		} else if (!claimType.equals(other.claimType)) {
			return false;
		}
		if (friendlyName == null) {
			if (other.friendlyName != null) {
				return false;
			}
		} else if (!friendlyName.equals(other.friendlyName)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (issuer == null) {
			if (other.issuer != null) {
				return false;
			}
		} else if (!issuer.equals(other.issuer)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value == null) {
			return other.value == null;
		} else return value.equals(other.value);
	}

}
