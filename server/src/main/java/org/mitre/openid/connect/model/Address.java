package org.mitre.openid.connect.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Address {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Basic
	private String formatted;
	
	@Basic
	private String street_address;
	
	@Basic
	private String locality;
	
	@Basic
	private String region;
	
	@Basic
	private String postal_code;
	
	@Basic
	private String country;
	
	/**
	 * Empty constructor
	 */
	public Address() {
		
	}
	
	/**
	 * @return the formatted address string
	 */
	public String getFormatted() {
		return formatted;
	}
	/**
	 * @param formatted the formatted address to set
	 */
	public void setFormatted(String formatted) {
		this.formatted = formatted;
	}
	/**
	 * @return the street_address
	 */
	public String getStreet_address() {
		return street_address;
	}
	/**
	 * @param street_address the street_address to set
	 */
	public void setStreet_address(String street_address) {
		this.street_address = street_address;
	}
	/**
	 * @return the locality
	 */
	public String getLocality() {
		return locality;
	}
	/**
	 * @param locality the locality to set
	 */
	public void setLocality(String locality) {
		this.locality = locality;
	}
	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}
	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}
	/**
	 * @return the postal_code
	 */
	public String getPostal_code() {
		return postal_code;
	}
	/**
	 * @param postal_code the postal_code to set
	 */
	public void setPostal_code(String postal_code) {
		this.postal_code = postal_code;
	}
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
     * @return the id
     */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
    	return id;
    }

	/**
     * @param id the id to set
     */
    public void setId(Long id) {
    	this.id = id;
    }
		
}
