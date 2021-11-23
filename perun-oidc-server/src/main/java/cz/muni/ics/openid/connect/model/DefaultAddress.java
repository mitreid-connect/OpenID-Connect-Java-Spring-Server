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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name = "address")
public class DefaultAddress implements Address {

	private static final long serialVersionUID = -1304880008685206811L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "formatted")
	private String formatted;

	@Column(name = "street_address")
	private String streetAddress;

	@Column(name = "locality")
	private String locality;

	@Column(name = "region")
	private String region;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "country")
	private String country;

	public DefaultAddress(Address address) {
		setFormatted(address.getFormatted());
		setStreetAddress(address.getStreetAddress());
		setLocality(address.getLocality());
		setRegion(address.getRegion());
		setPostalCode(address.getPostalCode());
		setCountry(address.getCountry());
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getFormatted() {
		return formatted;
	}

	@Override
	public void setFormatted(String formatted) {
		this.formatted = formatted;
	}

	@Override
	public String getStreetAddress() {
		return streetAddress;
	}

	@Override
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	@Override
	public String getLocality() {
		return locality;
	}

	@Override
	public void setLocality(String locality) {
		this.locality = locality;
	}

	@Override
	public String getRegion() {
		return region;
	}

	@Override
	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public String getPostalCode() {
		return postalCode;
	}

	@Override
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
	}

}
