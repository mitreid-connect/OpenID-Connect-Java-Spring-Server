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
package cz.muni.ics.openid.connect.model;

import java.io.Serializable;

public interface Address extends Serializable {

	/**
	 * Get the system-specific ID of the Address object
	 * @return
	 */
	Long getId();

	/**
	 * @return the formatted address
	 */
	String getFormatted();

	/**
	 * @param formatted the formatted address to set
	 */
	void setFormatted(String formatted);

	/**
	 * @return the streetAddress
	 */
	String getStreetAddress();

	/**
	 * @param streetAddress the streetAddress to set
	 */
	void setStreetAddress(String streetAddress);

	/**
	 * @return the locality
	 */
	String getLocality();

	/**
	 * @param locality the locality to set
	 */
	void setLocality(String locality);

	/**
	 * @return the region
	 */
	String getRegion();

	/**
	 * @param region the region to set
	 */
	void setRegion(String region);

	/**
	 * @return the postalCode
	 */
	String getPostalCode();

	/**
	 * @param postalCode the postalCode to set
	 */
	void setPostalCode(String postalCode);

	/**
	 * @return the country
	 */
	String getCountry();

	/**
	 * @param country the country to set
	 */
	void setCountry(String country);

}
