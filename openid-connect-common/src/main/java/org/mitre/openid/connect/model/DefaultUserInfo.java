/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="userinfo")
@NamedQueries({
	@NamedQuery(name="DefaultUserInfo.getAll", query = "select u from DefaultUserInfo u")
})
public class DefaultUserInfo implements UserInfo {
	
	private String userId;	
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
	
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getUserId()
	 */
	@Override
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public String getUserId() {
		return userId;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setUserId(java.lang.String)
	 */
	@Override
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getPreferredUsername
	 */
	@Override
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
	public Boolean getEmailVerified() {
		return emailVerified;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setVerified(java.lang.Boolean)
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
	
}
