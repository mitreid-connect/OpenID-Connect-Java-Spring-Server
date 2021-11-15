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
import java.io.Serializable;


public interface UserInfo extends Serializable {

	String getSub();

	void setSub(String sub);

	String getPreferredUsername();

	void setPreferredUsername(String preferredUsername);

	String getName();

	void setName(String name);

	String getGivenName();

	void setGivenName(String givenName);

	String getFamilyName();

	void setFamilyName(String familyName);

	String getMiddleName();

	void setMiddleName(String middleName);

	String getNickname();

	void setNickname(String nickname);

	String getProfile();

	void setProfile(String profile);

	String getPicture();

	void setPicture(String picture);

	String getWebsite();

	void setWebsite(String website);

	String getEmail();

	void setEmail(String email);

	Boolean getEmailVerified();

	void setEmailVerified(Boolean emailVerified);

	String getGender();

	void setGender(String gender);

	String getZoneinfo();

	void setZoneinfo(String zoneinfo);

	String getLocale();

	void setLocale(String locale);

	String getPhoneNumber();

	void setPhoneNumber(String phoneNumber);

	Boolean getPhoneNumberVerified();

	void setPhoneNumberVerified(Boolean phoneNumberVerified);

	Address getAddress();

	void setAddress(Address address);

	String getUpdatedTime();

	void setUpdatedTime(String updatedTime);

	String getBirthdate();

	void setBirthdate(String birthdate);

	JsonObject toJson();

	JsonObject getSource();

}
