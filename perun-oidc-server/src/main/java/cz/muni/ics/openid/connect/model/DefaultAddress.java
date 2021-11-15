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
package cz.muni.ics.openid.connect.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="address")
public class DefaultAddress implements Address {

	private static final long serialVersionUID = -1304880008685206811L;

	private Long id;
	private String formatted;
	private String streetAddress;
	private String locality;
	private String region;
	private String postalCode;
	private String country;

	public DefaultAddress() { }

	public DefaultAddress(Address address) {
		setFormatted(address.getFormatted());
		setStreetAddress(address.getStreetAddress());
		setLocality(address.getLocality());
		setRegion(address.getRegion());
		setPostalCode(address.getPostalCode());
		setCountry(address.getCountry());
	}

	@Override
	@Basic
	@Column(name = "formatted")
	public String getFormatted() {
		return formatted;
	}

	@Override
	public void setFormatted(String formatted) {
		this.formatted = formatted;
	}

	@Override
	@Basic
	@Column(name="street_address")
	public String getStreetAddress() {
		return streetAddress;
	}

	@Override
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	@Override
	@Basic
	@Column(name = "locality")
	public String getLocality() {
		return locality;
	}

	@Override
	public void setLocality(String locality) {
		this.locality = locality;
	}

	@Override
	@Basic
	@Column(name = "region")
	public String getRegion() {
		return region;
	}

	@Override
	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	@Basic
	@Column(name="postal_code")
	public String getPostalCode() {
		return postalCode;
	}

	@Override
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	@Override
	@Basic
	@Column(name = "country")
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
	}


	@Override
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((formatted == null) ? 0 : formatted.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((locality == null) ? 0 : locality.hashCode());
		result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((streetAddress == null) ? 0 : streetAddress.hashCode());
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
		if (!(obj instanceof DefaultAddress)) {
			return false;
		}
		DefaultAddress other = (DefaultAddress) obj;
		if (country == null) {
			if (other.country != null) {
				return false;
			}
		} else if (!country.equals(other.country)) {
			return false;
		}
		if (formatted == null) {
			if (other.formatted != null) {
				return false;
			}
		} else if (!formatted.equals(other.formatted)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (locality == null) {
			if (other.locality != null) {
				return false;
			}
		} else if (!locality.equals(other.locality)) {
			return false;
		}
		if (postalCode == null) {
			if (other.postalCode != null) {
				return false;
			}
		} else if (!postalCode.equals(other.postalCode)) {
			return false;
		}
		if (region == null) {
			if (other.region != null) {
				return false;
			}
		} else if (!region.equals(other.region)) {
			return false;
		}
		if (streetAddress == null) {
			return other.streetAddress == null;
		} else return streetAddress.equals(other.streetAddress);
	}

}
