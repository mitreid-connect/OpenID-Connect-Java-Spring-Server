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
package org.mitre.openid.connect.view;

import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.UserInfo;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class POCOUserInfoView extends AbstractView{
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		UserInfo userInfo = (UserInfo) model.get("userInfo");

		Set<String> scope = (Set<String>) model.get("scope");
		
		Gson gson = new GsonBuilder()
			.setExclusionStrategies(new ExclusionStrategy() {
				
				public boolean shouldSkipField(FieldAttributes f) {
					
					return false;
				}
				
				public boolean shouldSkipClass(Class<?> clazz) {
					// skip the JPA binding wrapper
					if (clazz.equals(BeanPropertyBindingResult.class)) {
						return true;
					}
					return false;
				}
								
			}).create();

		response.setContentType("application/json");
		Writer out = response.getWriter();
		gson.toJson(toPoco(userInfo, scope), out);
	}
	
	private JsonObject toPoco(UserInfo ui, Set<String> scope) {
		JsonObject poco = new JsonObject();
		
		// Envelope Info
		poco.addProperty("startIndex", 0);
		poco.addProperty("itemsPerPage", 1);
		poco.addProperty("totalResults", 1);
		
		// Build the entry for this userInfo, then add it to entries, then add it to poco
		JsonObject entry = new JsonObject();
		
		if (scope.contains("openid")) {
			entry.addProperty("id", ui.getUserId());
		}
		
		if (scope.contains("profile")) {
			entry.addProperty("displayName", ui.getNickname());
			
			if(ui.getFamilyName() != null 
					|| ui.getGivenName() != null 
					|| ui.getMiddleName() != null
					|| ui.getName() != null) {
				JsonObject name = new JsonObject();
				name.addProperty("familyName", ui.getFamilyName());
				name.addProperty("givenName", ui.getGivenName());
				name.addProperty("middleName", ui.getMiddleName());
				name.addProperty("formatted", ui.getName());
				entry.add("name", name);
			}
		
			entry.addProperty("gender", ui.getGender());
			// TODO: preferred_username
			if(ui.getPicture() != null){
				JsonObject photo = new JsonObject();
				photo.addProperty("value", ui.getPicture());
				
				JsonArray photoArray = new JsonArray();
				photoArray.add(photo);
				entry.add("photos", photoArray);
			}
			
			if(ui.getWebsite() != null) {
				JsonObject website = new JsonObject();
				website.addProperty("value", ui.getWebsite());
				
				JsonArray websiteArray = new JsonArray();
				websiteArray.add(website);
				entry.add("urls", websiteArray);
			}

			entry.addProperty("updated", ui.getUpdatedTime());
			
		}
		
		if (scope.contains("email")) {
			if(ui.getEmail() != null) {
				JsonObject email = new JsonObject();
				email.addProperty("value", ui.getEmail());
				
				JsonArray emailArray = new JsonArray();
				emailArray.add(email);
				entry.add("emails", emailArray);
			}
		}
		
		if (scope.contains("phone")) {
			if(ui.getPhoneNumber() != null){
				JsonObject phone = new JsonObject();
				phone.addProperty("value", ui.getPhoneNumber());
				
				JsonArray phoneArray = new JsonArray();
				phoneArray.add(phone);
				entry.add("phoneNumbers", phoneArray);
			}
		
		}
		
		if (scope.contains("address")) {
			if(ui.getAddress() != null) {
				JsonObject addr = new JsonObject();
				addr.addProperty("formatted", ui.getAddress().getFormatted());
				addr.addProperty("streetAddress", ui.getAddress().getStreetAddress());
				addr.addProperty("locality", ui.getAddress().getLocality());
				addr.addProperty("region", ui.getAddress().getRegion());
				addr.addProperty("postalCode", ui.getAddress().getPostalCode());
				addr.addProperty("country", ui.getAddress().getCountry());
				
				JsonArray addrArray = new JsonArray();
				addrArray.add(addr);
				entry.add("addresses", addrArray);
			}
		}
		
		JsonArray entryArray = new JsonArray();
		entryArray.add(entry);
		poco.add("entry", entryArray);
		return poco;
	}

}
