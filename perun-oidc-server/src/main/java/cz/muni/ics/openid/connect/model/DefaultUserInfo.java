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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.muni.ics.openid.connect.model.convert.JsonObjectStringConverter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
	@Basic
	@Column(name="sub")
	public String getSub() {
		return sub;
	}

	@Override
	public void setSub(String sub) {
		this.sub = sub;
	}

	@Override
	@Basic
	@Column(name="preferred_username")
	public String getPreferredUsername() {
		return this.preferredUsername;
	}

	@Override
	public void setPreferredUsername(String preferredUsername) {
		this.preferredUsername = preferredUsername;
	}

	@Override
	@Basic
	@Column(name = "name")
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	@Basic
	@Column(name="given_name")
	public String getGivenName() {
		return givenName;
	}

	@Override
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	@Override
	@Basic
	@Column(name="family_name")
	public String getFamilyName() {
		return familyName;
	}

	@Override
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	@Override
	@Basic
	@Column(name="middle_name")
	public String getMiddleName() {
		return middleName;
	}

	@Override
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Override
	@Basic
	@Column(name = "nickname")
	public String getNickname() {
		return nickname;
	}

	@Override
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Override
	@Basic
	@Column(name = "profile")
	public String getProfile() {
		return profile;
	}

	@Override
	public void setProfile(String profile) {
		this.profile = profile;
	}

	@Override
	@Basic
	@Column(name = "picture")
	public String getPicture() {
		return picture;
	}

	@Override
	public void setPicture(String picture) {
		this.picture = picture;
	}

	@Override
	@Basic
	@Column(name = "website")
	public String getWebsite() {
		return website;
	}

	@Override
	public void setWebsite(String website) {
		this.website = website;
	}

	@Override
	@Basic
	@Column(name = "email")
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	@Basic
	@Column(name="email_verified")
	public Boolean getEmailVerified() {
		return emailVerified;
	}

	@Override
	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	@Override
	@Basic
	@Column(name = "gender")
	public String getGender() {
		return gender;
	}

	@Override
	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	@Basic
	@Column(name="zone_info")
	public String getZoneinfo() {
		return zoneinfo;
	}

	@Override
	public void setZoneinfo(String zoneinfo) {
		this.zoneinfo = zoneinfo;
	}

	@Override
	@Basic
	@Column(name = "locale")
	public String getLocale() {
		return locale;
	}

	@Override
	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	@Basic
	@Column(name="phone_number")
	public String getPhoneNumber() {
		return phoneNumber;
	}

	@Override
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	@Basic
	@Column(name="phone_number_verified")
	public Boolean getPhoneNumberVerified() {
		return phoneNumberVerified;
	}

	@Override
	public void setPhoneNumberVerified(Boolean phoneNumberVerified) {
		this.phoneNumberVerified = phoneNumberVerified;
	}

	@Override
	@OneToOne(targetEntity = DefaultAddress.class, cascade = CascadeType.ALL)
	@JoinColumn(name="address_id")
	public Address getAddress() {
		return address;
	}

	@Override
	public void setAddress(Address address) {
		if (address != null) {
			this.address = new DefaultAddress(address);
		} else {
			this.address = null;
		}
	}

	@Override
	@Basic
	@Column(name="updated_time")
	public String getUpdatedTime() {
		return updatedTime;
	}

	@Override
	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}

	@Override
	@Basic
	@Column(name="birthdate")
	public String getBirthdate() {
		return birthdate;
	}

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

	@Override
	@Basic
	@Column(name = "src")
	@Convert(converter = JsonObjectStringConverter.class)
	public JsonObject getSource() {
		return src;
	}

	public void setSource(JsonObject src) {
		this.src = src;
	}

	private static String nullSafeGetString(JsonObject obj, String field) {
		return obj.has(field) && obj.get(field).isJsonPrimitive() ? obj.get(field).getAsString() : null;
	}

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
			return other.zoneinfo == null;
		} else return zoneinfo.equals(other.zoneinfo);
	}

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
