/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.openid.connect.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mitre.openid.connect.model.convert.JsonObjectStringConverter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Entity
@Table(name="user_info")
@NamedQueries({
	@NamedQuery(name=DefaultUserInfo.QUERY_BY_USERNAME, query = "select u from DefaultUserInfo u WHERE u.preferredUsername = :" + DefaultUserInfo.PARAM_USERNAME),
	@NamedQuery(name=DefaultUserInfo.QUERY_BY_EMAIL, query = "select u from DefaultUserInfo u WHERE u.email = :" + DefaultUserInfo.PARAM_EMAIL)
})
public class DefaultUserInfo implements UserInfo {

	public static final String QUERY_BY_USERNAME = "DefaultUserInfo.getByUsername";
	public static final String QUERY_BY_EMAIL = "DefaultUserInfo.getByEmailAddress";

	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_EMAIL = "email";

	private static final long serialVersionUID = 6078310513185681918L;

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
	private Boolean phoneNumberVerified;
	private DefaultAddress address;
	private String updatedTime;
	private String birthdate;
	private transient JsonObject src; // source JSON if this is loaded remotely


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
	 * @see org.mitre.openid.connect.model.UserInfo#getPhoneNumberVerified()
	 */
	@Override
	@Basic
	@Column(name="phone_number_verified")
	public Boolean getPhoneNumberVerified() {
		return phoneNumberVerified;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setPhoneNumberVerified(java.lang.Boolean)
	 */
	@Override
	public void setPhoneNumberVerified(Boolean phoneNumberVerified) {
		this.phoneNumberVerified = phoneNumberVerified;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#getAddress()
	 */
	@Override
	@OneToOne(targetEntity = DefaultAddress.class)
	@JoinColumn(name="address_id")
	public Address getAddress() {
		return address;
	}
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.model.UserInfo#setAddress(org.mitre.openid.connect.model.Address)
	 */
	@Override
	public void setAddress(Address address) {
		if (address != null) { 
			this.address = new DefaultAddress(address);
		} else {
			this.address = null;
		}
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

	@Override
	public JsonObject toJson() {
		if (src == null) {

			JsonObject obj = new JsonObject();

			obj.addProperty("sub", this.getSub());

			obj.addProperty("name", this.getName());
			obj.addProperty("preferred_username", this.getPreferredUsername());
			obj.addProperty("given_name", this.getGivenName());
			obj.addProperty("family_name", this.getFamilyName());
			obj.addProperty("middle_name", this.getMiddleName());
			obj.addProperty("nickname", this.getNickname());
			obj.addProperty("profile", this.getProfile());
			obj.addProperty("picture", this.getPicture());
			obj.addProperty("website", this.getWebsite());
			obj.addProperty("gender", this.getGender());
			obj.addProperty("zoneinfo", this.getZoneinfo());
			obj.addProperty("locale", this.getLocale());
			obj.addProperty("updated_at", this.getUpdatedTime());
			obj.addProperty("birthdate", this.getBirthdate());

			obj.addProperty("email", this.getEmail());
			obj.addProperty("email_verified", this.getEmailVerified());

			obj.addProperty("phone_number", this.getPhoneNumber());
			obj.addProperty("phone_number_verified", this.getPhoneNumberVerified());

			if (this.getAddress() != null) {

				JsonObject addr = new JsonObject();
				addr.addProperty("formatted", this.getAddress().getFormatted());
				addr.addProperty("street_address", this.getAddress().getStreetAddress());
				addr.addProperty("locality", this.getAddress().getLocality());
				addr.addProperty("region", this.getAddress().getRegion());
				addr.addProperty("postal_code", this.getAddress().getPostalCode());
				addr.addProperty("country", this.getAddress().getCountry());

				obj.add("address", addr);
			}

			return obj;
		} else {
			return src;
		}

	}

	/**
	 * Parse a JsonObject into a UserInfo.
	 * @param o
	 * @return
	 */
	public static UserInfo fromJson(JsonObject obj) {
		DefaultUserInfo ui = new DefaultUserInfo();
		ui.setSource(obj);

		ui.setSub(nullSafeGetString(obj, "sub"));

		ui.setName(nullSafeGetString(obj, "name"));
		ui.setPreferredUsername(nullSafeGetString(obj, "preferred_username"));
		ui.setGivenName(nullSafeGetString(obj, "given_name"));
		ui.setFamilyName(nullSafeGetString(obj, "family_name"));
		ui.setMiddleName(nullSafeGetString(obj, "middle_name"));
		ui.setNickname(nullSafeGetString(obj, "nickname"));
		ui.setProfile(nullSafeGetString(obj, "profile"));
		ui.setPicture(nullSafeGetString(obj, "picture"));
		ui.setWebsite(nullSafeGetString(obj, "website"));
		ui.setGender(nullSafeGetString(obj, "gender"));
		ui.setZoneinfo(nullSafeGetString(obj, "zoneinfo"));
		ui.setLocale(nullSafeGetString(obj, "locale"));
		ui.setUpdatedTime(nullSafeGetString(obj, "updated_at"));
		ui.setBirthdate(nullSafeGetString(obj, "birthdate"));

		ui.setEmail(nullSafeGetString(obj, "email"));
		ui.setEmailVerified(obj.has("email_verified") && obj.get("email_verified").isJsonPrimitive() ? obj.get("email_verified").getAsBoolean() : null);

		ui.setPhoneNumber(nullSafeGetString(obj, "phone_number"));
		ui.setPhoneNumberVerified(obj.has("phone_number_verified") && obj.get("phone_number_verified").isJsonPrimitive() ? obj.get("phone_number_verified").getAsBoolean() : null);
		
		if (obj.has("address") && obj.get("address").isJsonObject()) {
			JsonObject addr = obj.get("address").getAsJsonObject();
			ui.setAddress(new DefaultAddress());

			ui.getAddress().setFormatted(nullSafeGetString(addr, "formatted"));
			ui.getAddress().setStreetAddress(nullSafeGetString(addr, "street_address"));
			ui.getAddress().setLocality(nullSafeGetString(addr, "locality"));
			ui.getAddress().setRegion(nullSafeGetString(addr, "region"));
			ui.getAddress().setPostalCode(nullSafeGetString(addr, "postal_code"));
			ui.getAddress().setCountry(nullSafeGetString(addr, "country"));

		}


		return ui;

	}
	/**
	 * @return the jsonString
	 */
	@Override
	@Basic
	@Column(name = "src")
	@Convert(converter = JsonObjectStringConverter.class)
	public JsonObject getSource() {
		return src;
	}

	/**
	 * @param jsonString the jsonString to set
	 */
	public void setSource(JsonObject src) {
		this.src = src;
	}


	private static String nullSafeGetString(JsonObject obj, String field) {
		return obj.has(field) && obj.get(field).isJsonPrimitive() ? obj.get(field).getAsString() : null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((birthdate == null) ? 0 : birthdate.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((emailVerified == null) ? 0 : emailVerified.hashCode());
		result = prime * result + ((familyName == null) ? 0 : familyName.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + ((givenName == null) ? 0 : givenName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((phoneNumberVerified == null) ? 0 : phoneNumberVerified.hashCode());
		result = prime * result + ((picture == null) ? 0 : picture.hashCode());
		result = prime * result + ((preferredUsername == null) ? 0 : preferredUsername.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + ((sub == null) ? 0 : sub.hashCode());
		result = prime * result + ((updatedTime == null) ? 0 : updatedTime.hashCode());
		result = prime * result + ((website == null) ? 0 : website.hashCode());
		result = prime * result + ((zoneinfo == null) ? 0 : zoneinfo.hashCode());
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
		if (!(obj instanceof DefaultUserInfo)) {
			return false;
		}
		DefaultUserInfo other = (DefaultUserInfo) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (birthdate == null) {
			if (other.birthdate != null) {
				return false;
			}
		} else if (!birthdate.equals(other.birthdate)) {
			return false;
		}
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (emailVerified == null) {
			if (other.emailVerified != null) {
				return false;
			}
		} else if (!emailVerified.equals(other.emailVerified)) {
			return false;
		}
		if (familyName == null) {
			if (other.familyName != null) {
				return false;
			}
		} else if (!familyName.equals(other.familyName)) {
			return false;
		}
		if (gender == null) {
			if (other.gender != null) {
				return false;
			}
		} else if (!gender.equals(other.gender)) {
			return false;
		}
		if (givenName == null) {
			if (other.givenName != null) {
				return false;
			}
		} else if (!givenName.equals(other.givenName)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (locale == null) {
			if (other.locale != null) {
				return false;
			}
		} else if (!locale.equals(other.locale)) {
			return false;
		}
		if (middleName == null) {
			if (other.middleName != null) {
				return false;
			}
		} else if (!middleName.equals(other.middleName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (nickname == null) {
			if (other.nickname != null) {
				return false;
			}
		} else if (!nickname.equals(other.nickname)) {
			return false;
		}
		if (phoneNumber == null) {
			if (other.phoneNumber != null) {
				return false;
			}
		} else if (!phoneNumber.equals(other.phoneNumber)) {
			return false;
		}
		if (phoneNumberVerified == null) {
			if (other.phoneNumberVerified != null) {
				return false;
			}
		} else if (!phoneNumberVerified.equals(other.phoneNumberVerified)) {
			return false;
		}
		if (picture == null) {
			if (other.picture != null) {
				return false;
			}
		} else if (!picture.equals(other.picture)) {
			return false;
		}
		if (preferredUsername == null) {
			if (other.preferredUsername != null) {
				return false;
			}
		} else if (!preferredUsername.equals(other.preferredUsername)) {
			return false;
		}
		if (profile == null) {
			if (other.profile != null) {
				return false;
			}
		} else if (!profile.equals(other.profile)) {
			return false;
		}
		if (sub == null) {
			if (other.sub != null) {
				return false;
			}
		} else if (!sub.equals(other.sub)) {
			return false;
		}
		if (updatedTime == null) {
			if (other.updatedTime != null) {
				return false;
			}
		} else if (!updatedTime.equals(other.updatedTime)) {
			return false;
		}
		if (website == null) {
			if (other.website != null) {
				return false;
			}
		} else if (!website.equals(other.website)) {
			return false;
		}
		if (zoneinfo == null) {
			if (other.zoneinfo != null) {
				return false;
			}
		} else if (!zoneinfo.equals(other.zoneinfo)) {
			return false;
		}
		return true;
	}


	/*
	 * Custom serialization to handle the JSON object
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (src == null) {
			out.writeObject(null);
		} else {
			out.writeObject(src.toString());
		}
	}
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		Object o = in.readObject();
		if (o != null) {
			JsonParser parser = new JsonParser();
			src = parser.parse((String)o).getAsJsonObject();
		}
	}

}
