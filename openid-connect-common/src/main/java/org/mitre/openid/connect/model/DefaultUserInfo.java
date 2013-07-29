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
package org.mitre.openid.connect.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.google.gson.JsonObject;

@Entity
@Table(name="user_info")
@NamedQueries({
	@NamedQuery(name="DefaultUserInfo.getAll", query = "select u from DefaultUserInfo u"),
	@NamedQuery(name="DefaultUserInfo.getByUsername", query = "select u from DefaultUserInfo u WHERE u.preferredUsername = :username"),
	@NamedQuery(name="DefaultUserInfo.getBySubject", query = "select u from DefaultUserInfo u WHERE u.sub = :sub")
})
public class DefaultUserInfo implements UserInfo {

	private Long id;
	private String sub;
	private String preferredUsername;
	private String name;
	private String givenName;
	private String familyName;
	private String middleName;
	private String nickname;
	private String profile;
	private String picture;
	private String website;
	private String email;
	private Boolean emailVerified;
	private String gender;
	private String zoneinfo;
	private String locale;
	private String phoneNumber;
	private Address address;
	private String updatedTime;
	private String birthdate;


	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getUserId()
	 */
	@Override
	@Basic
	@Column(name="sub")
	public String getSub() {
		return sub;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setUserId(java.lang.String)
	 */
	@Override
	public void setSub(String sub) {
		this.sub = sub;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getPreferredUsername
	 */
	@Override
	@Basic
	@Column(name="preferred_username")
	public String getPreferredUsername() {
		return this.preferredUsername;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setPreferredUsername(java.lang.String)
	 */
	@Override
	public void setPreferredUsername(String preferredUsername) {
		this.preferredUsername = preferredUsername;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getName()
	 */
	@Override
	@Basic
	@Column(name = "name")
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getGivenName()
	 */
	@Override
	@Basic
	@Column(name="given_name")
	public String getGivenName() {
		return givenName;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setGivenName(java.lang.String)
	 */
	@Override
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getFamilyName()
	 */
	@Override
	@Basic
	@Column(name="family_name")
	public String getFamilyName() {
		return familyName;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setFamilyName(java.lang.String)
	 */
	@Override
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getMiddleName()
	 */
	@Override
	@Basic
	@Column(name="middle_name")
	public String getMiddleName() {
		return middleName;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setMiddleName(java.lang.String)
	 */
	@Override
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getNickname()
	 */
	@Override
	@Basic
	@Column(name = "nickname")
	public String getNickname() {
		return nickname;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setNickname(java.lang.String)
	 */
	@Override
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getProfile()
	 */
	@Override
	@Basic
	@Column(name = "profile")
	public String getProfile() {
		return profile;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setProfile(java.lang.String)
	 */
	@Override
	public void setProfile(String profile) {
		this.profile = profile;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getPicture()
	 */
	@Override
	@Basic
	@Column(name = "picture")
	public String getPicture() {
		return picture;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setPicture(java.lang.String)
	 */
	@Override
	public void setPicture(String picture) {
		this.picture = picture;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getWebsite()
	 */
	@Override
	@Basic
	@Column(name = "website")
	public String getWebsite() {
		return website;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setWebsite(java.lang.String)
	 */
	@Override
	public void setWebsite(String website) {
		this.website = website;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getEmail()
	 */
	@Override
	@Basic
	@Column(name = "email")
	public String getEmail() {
		return email;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setEmail(java.lang.String)
	 */
	@Override
	public void setEmail(String email) {
		this.email = email;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getVerified()
	 */
	@Override
	@Basic
	@Column(name="email_verified")
	public Boolean getEmailVerified() {
		return emailVerified;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setVerified(java.lang.boolean)
	 */
	@Override
	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getGender()
	 */
	@Override
	@Basic
	@Column(name = "gender")
	public String getGender() {
		return gender;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setGender(java.lang.String)
	 */
	@Override
	public void setGender(String gender) {
		this.gender = gender;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getZoneinfo()
	 */
	@Override
	@Basic
	@Column(name="zone_info")
	public String getZoneinfo() {
		return zoneinfo;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setZoneinfo(java.lang.String)
	 */
	@Override
	public void setZoneinfo(String zoneinfo) {
		this.zoneinfo = zoneinfo;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getLocale()
	 */
	@Override
	@Basic
	@Column(name = "locale")
	public String getLocale() {
		return locale;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setLocale(java.lang.String)
	 */
	@Override
	public void setLocale(String locale) {
		this.locale = locale;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getPhoneNumber()
	 */
	@Override
	@Basic
	@Column(name="phone_number")
	public String getPhoneNumber() {
		return phoneNumber;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setPhoneNumber(java.lang.String)
	 */
	@Override
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getAddress()
	 */
	@Override
	@OneToOne
	@JoinColumn(name="address_id")
	public Address getAddress() {
		return address;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setAddress(org.mitre.openid.connect.model.Address)
	 */
	@Override
	public void setAddress(Address address) {
		this.address = address;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getUpdatedTime()
	 */
	@Override
	@Basic
	@Column(name="updated_time")
	public String getUpdatedTime() {
		return updatedTime;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setUpdatedTime(java.lang.String)
	 */
	@Override
	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @return the birthdate
	 */
	@Override
	@Basic
	@Column(name="birthdate")
	public String getBirthdate() {
		return birthdate;
	}
	/**
	 * @param birthdate the birthdate to set
	 */
	@Override
	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	/**
	 * Parse a JsonObject into a UserInfo.
	 * @param o
	 * @return
	 */
	public static UserInfo fromJson(JsonObject obj) {
		DefaultUserInfo ui = new DefaultUserInfo();

		ui.setSub(obj.has("sub") ? obj.get("sub").getAsString() : null);

		ui.setName(obj.has("name") ? obj.get("name").getAsString() : null);
		ui.setPreferredUsername(obj.has("preferred_username") ? obj.get("preferred_username").getAsString() : null);
		ui.setGivenName(obj.has("given_name") ? obj.get("given_name").getAsString() : null);
		ui.setFamilyName(obj.has("family_name") ? obj.get("family_name").getAsString() : null);
		ui.setMiddleName(obj.has("middle_name") ? obj.get("middle_name").getAsString() : null);
		ui.setNickname(obj.has("nickname") ? obj.get("nickname").getAsString() : null);
		ui.setProfile(obj.has("profile") ? obj.get("profile").getAsString() : null);
		ui.setPicture(obj.has("picture") ? obj.get("picture").getAsString() : null);
		ui.setWebsite(obj.has("website") ? obj.get("website").getAsString() : null);
		ui.setGender(obj.has("gender") ? obj.get("gender").getAsString() : null);
		ui.setZoneinfo(obj.has("zone_info") ? obj.get("zone_info").getAsString() : null);
		ui.setLocale(obj.has("locale") ? obj.get("locale").getAsString() : null);
		ui.setUpdatedTime(obj.has("updated_time") ? obj.get("updated_time").getAsString() : null);
		ui.setBirthdate(obj.has("birthdate") ? obj.get("birthdate").getAsString() : null);

		ui.setEmail(obj.has("email") ? obj.get("email").getAsString() : null);
		ui.setEmailVerified(obj.has("email_verified") ? obj.get("email_verified").getAsBoolean() : null);

		ui.setPhoneNumber(obj.has("phone_number") ? obj.get("phone_number").getAsString() : null);


		if (obj.has("address") && obj.get("address").isJsonObject()) {
			JsonObject addr = obj.get("address").getAsJsonObject();
			ui.setAddress(new Address());

			ui.getAddress().setFormatted(addr.has("formatted") ? addr.get("formatted").getAsString() : null);
			ui.getAddress().setStreetAddress(addr.has("street_address") ? addr.get("street_address").getAsString() : null);
			ui.getAddress().setLocality(addr.has("locality") ? addr.get("locality").getAsString() : null);
			ui.getAddress().setRegion(addr.has("region") ? addr.get("region").getAsString() : null);
			ui.getAddress().setPostalCode(addr.has("postal_code") ? addr.get("postal_code").getAsString() : null);
			ui.getAddress().setCountry(addr.has("country") ? addr.get("country").getAsString() : null);

		}


		return ui;

	}

}
