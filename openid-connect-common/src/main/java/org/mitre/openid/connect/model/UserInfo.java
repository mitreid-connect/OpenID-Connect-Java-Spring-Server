package org.mitre.openid.connect.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Table(name="user_info")
public interface UserInfo {

	/**
	 * @return the userId
	 */
	@Id
	@Column(name="user_id")
	public abstract String getUserId();

	/**
	 * @param userId the userId to set
	 */
	public abstract void setUserId(String userId);
	
	/**
	 * @return the preferred username
	 */
	@Basic
	@Column(name="preferred_username")
	public abstract String getPreferredUsername();
	
	/**
	 * @param preferredUsername the preferredUsername to set
	 */
	public abstract void setPreferredUsername(String preferredUsername);

	/**
	 * @return the name
	 */
	@Basic
	public abstract String getName();

	/**
	 * @param name the name to set
	 */
	public abstract void setName(String name);

	/**
	 * @return the givenName
	 */
	@Basic
	@Column(name="given_name")
	public abstract String getGivenName();

	/**
	 * @param givenName the givenName to set
	 */
	public abstract void setGivenName(String givenName);

	/**
	 * @return the familyName
	 */
	@Basic
	@Column(name="family_name")
	public abstract String getFamilyName();

	/**
	 * @param familyName the familyName to set
	 */
	public abstract void setFamilyName(String familyName);

	/**
	 * @return the middleName
	 */
	@Basic
	@Column(name="middle_name")
	public abstract String getMiddleName();

	/**
	 * @param middleName the middleName to set
	 */
	public abstract void setMiddleName(String middleName);

	/**
	 * @return the nickname
	 */
	@Basic
	public abstract String getNickname();

	/**
	 * @param nickname the nickname to set
	 */
	public abstract void setNickname(String nickname);

	/**
	 * @return the profile
	 */
	@Basic
	public abstract String getProfile();

	/**
	 * @param profile the profile to set
	 */
	public abstract void setProfile(String profile);

	/**
	 * @return the picture
	 */
	@Basic
	public abstract String getPicture();

	/**
	 * @param picture the picture to set
	 */
	public abstract void setPicture(String picture);

	/**
	 * @return the website
	 */
	@Basic
	public abstract String getWebsite();

	/**
	 * @param website the website to set
	 */
	public abstract void setWebsite(String website);

	/**
	 * @return the email
	 */
	@Basic
	public abstract String getEmail();

	/**
	 * @param email the email to set
	 */
	public abstract void setEmail(String email);

	/**
	 * @return the verified
	 */
	@Basic
	@Column(name="email_verified")
	public abstract Boolean getEmailVerified();

	/**
	 * @param verified the verified to set
	 */
	public abstract void setEmailVerified(Boolean emailVerified);

	/**
	 * @return the gender
	 */
	@Basic
	public abstract String getGender();

	/**
	 * @param gender the gender to set
	 */
	public abstract void setGender(String gender);

	/**
	 * @return the zoneinfo
	 */
	@Basic
	@Column(name="zone_info")
	public abstract String getZoneinfo();

	/**
	 * @param zoneinfo the zoneinfo to set
	 */
	public abstract void setZoneinfo(String zoneinfo);

	/**
	 * @return the locale
	 */
	@Basic
	public abstract String getLocale();

	/**
	 * @param locale the locale to set
	 */
	public abstract void setLocale(String locale);

	/**
	 * @return the phoneNumber
	 */
	@Basic
	@Column(name="phone_number")
	public abstract String getPhoneNumber();

	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public abstract void setPhoneNumber(String phoneNumber);

	/**
	 * @return the address
	 */
	@OneToOne
	public abstract Address getAddress();

	/**
	 * @param address the address to set
	 */
	public abstract void setAddress(Address address);

	/**
	 * @return the updatedTime
	 */
	@Basic
	@Column(name="updated_time")
	public abstract String getUpdatedTime();

	/**
	 * @param updatedTime the updatedTime to set
	 */
	public abstract void setUpdatedTime(String updatedTime);

}