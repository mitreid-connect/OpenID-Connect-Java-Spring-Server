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

import static cz.muni.ics.openid.connect.model.DefaultUserInfo.PARAM_EMAIL;
import static cz.muni.ics.openid.connect.model.DefaultUserInfo.PARAM_USERNAME;
import static cz.muni.ics.openid.connect.model.DefaultUserInfo.QUERY_BY_EMAIL;
import static cz.muni.ics.openid.connect.model.DefaultUserInfo.QUERY_BY_USERNAME;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.muni.ics.openid.connect.model.convert.JsonObjectStringConverter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
@Table(name="user_info")
@NamedQueries({
	@NamedQuery(name = QUERY_BY_USERNAME,
				query = "SELECT u FROM DefaultUserInfo u " +
						"WHERE u.preferredUsername = :" + PARAM_USERNAME),
	@NamedQuery(name = QUERY_BY_EMAIL,
				query = "SELECT u FROM DefaultUserInfo u " +
						"WHERE u.email = :" + PARAM_EMAIL)
})
public class DefaultUserInfo implements UserInfo {

	public static final String QUERY_BY_USERNAME = "DefaultUserInfo.getByUsername";
	public static final String QUERY_BY_EMAIL = "DefaultUserInfo.getByEmailAddress";

	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_EMAIL = "email";

	private static final long serialVersionUID = 6078310513185681918L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "sub")
	private String sub;

	@Column(name = "preferred_username")
	private String preferredUsername;

	@Column(name = "name")
	private String name;

	@Column(name = "given_name")
	private String givenName;

	@Column(name = "family_name")
	private String familyName;

	@Column(name = "middle_name")
	private String middleName;

	@Column(name = "nickname")
	private String nickname;

	@Column(name = "profile")
	private String profile;

	@Column(name = "picture")
	private String picture;

	@Column(name = "website")
	private String website;

	@Column(name = "email")
	private String email;

	@Column(name = "email_verified")
	private Boolean emailVerified;

	@Column(name = "gender")
	private String gender;

	@Column(name = "zone_info")
	private String zoneinfo;

	@Column(name = "locale")
	private String locale;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "phone_number_verified")
	private Boolean phoneNumberVerified;

	@OneToOne(targetEntity = DefaultAddress.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id")
	private DefaultAddress address;

	@Column(name = "updated_time")
	private String updatedTime;

	@Column(name = "birthdate")
	private String birthdate;

	@Column(name = "src")
	@Convert(converter = JsonObjectStringConverter.class)
	private transient JsonObject source; // source JSON if this is loaded remotely

	@Override
	public String getSub() {
		return sub;
	}

	@Override
	public void setSub(String sub) {
		this.sub = sub;
	}

	@Override
	public String getPreferredUsername() {
		return this.preferredUsername;
	}

	@Override
	public void setPreferredUsername(String preferredUsername) {
		this.preferredUsername = preferredUsername;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getGivenName() {
		return givenName;
	}

	@Override
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	@Override
	public String getFamilyName() {
		return familyName;
	}

	@Override
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	@Override
	public String getMiddleName() {
		return middleName;
	}

	@Override
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Override
	public String getNickname() {
		return nickname;
	}

	@Override
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Override
	public String getProfile() {
		return profile;
	}

	@Override
	public void setProfile(String profile) {
		this.profile = profile;
	}

	@Override
	public String getPicture() {
		return picture;
	}

	@Override
	public void setPicture(String picture) {
		this.picture = picture;
	}

	@Override
	public String getWebsite() {
		return website;
	}

	@Override
	public void setWebsite(String website) {
		this.website = website;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public Boolean getEmailVerified() {
		return emailVerified;
	}

	@Override
	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	@Override
	public String getGender() {
		return gender;
	}

	@Override
	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String getZoneinfo() {
		return zoneinfo;
	}

	@Override
	public void setZoneinfo(String zoneinfo) {
		this.zoneinfo = zoneinfo;
	}

	@Override
	public String getLocale() {
		return locale;
	}

	@Override
	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String getPhoneNumber() {
		return phoneNumber;
	}

	@Override
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public Boolean getPhoneNumberVerified() {
		return phoneNumberVerified;
	}

	@Override
	public void setPhoneNumberVerified(Boolean phoneNumberVerified) {
		this.phoneNumberVerified = phoneNumberVerified;
	}

	@Override
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
	public String getUpdatedTime() {
		return updatedTime;
	}

	@Override
	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}

	@Override
	public String getBirthdate() {
		return birthdate;
	}

	@Override
	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	@Override
	public JsonObject toJson() {
		if (source == null) {
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
			return source;
		}
	}

	@Override
	public JsonObject getSource() {
		return source;
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

	private static String nullSafeGetString(JsonObject obj, String field) {
		return obj.has(field) && obj.get(field).isJsonPrimitive() ? obj.get(field).getAsString() : null;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (source == null) {
			out.writeObject(null);
		} else {
			out.writeObject(source.toString());
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		Object o = in.readObject();
		if (o != null) {
			JsonParser parser = new JsonParser();
			source = parser.parse((String)o).getAsJsonObject();
		}
	}

}
