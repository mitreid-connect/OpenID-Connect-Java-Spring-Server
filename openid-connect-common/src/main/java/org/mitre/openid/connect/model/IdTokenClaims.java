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

import java.util.Date;
import java.util.Map.Entry;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mitre.jwt.model.JwtClaims;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Entity
@Table(name="idtokenclaims")
public class IdTokenClaims extends JwtClaims {

	public static final String AUTHENTICATION_CONTEXT_CLASS_REFERENCE = "acr";
	public static final String AUTH_TIME = "auth_time";
	public static final String AUTHORIZED_PARTY = "azp";

	private Long id;
	
	
	
	public IdTokenClaims() {
	    super();
    }

	public IdTokenClaims(JsonObject json) {
	    super(json);
    }

	public IdTokenClaims(String b64) {
	    super(b64);
    }
	
	public IdTokenClaims(JwtClaims jwtClaims) {
		super(jwtClaims);
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

	@Transient
	public String getAuthContext() {
		return getClaimAsString(AUTHENTICATION_CONTEXT_CLASS_REFERENCE);		
	}
	
	public void setAuthContext(String acr) {
		setClaim(AUTHENTICATION_CONTEXT_CLASS_REFERENCE, acr);
	}
	
	@Transient
	public Date getAuthTime() {
		return getClaimAsDate(AUTH_TIME);
	}
	
	public void setAuthTime(Date authTime) {
		setClaim(AUTH_TIME, authTime);
	}
	
	@Transient
	public String getAuthorizedParty() {
		return getClaimAsString(AUTHORIZED_PARTY);
	}
	
	public void setAuthorizedParty(String azp) {
		setClaim(AUTHORIZED_PARTY, azp);
	}
	
	
	/**
	 * Load this IdToken from a JSON Object
     */
    @Override
    public void loadFromJsonObject(JsonObject json) {
    	JsonObject pass = new JsonObject();
    	
		for (Entry<String, JsonElement> element : json.entrySet()) {
			if (element.getValue().isJsonNull()) {
				pass.add(element.getKey(), element.getValue());
			} else if (element.getKey().equals(AUTHENTICATION_CONTEXT_CLASS_REFERENCE)) {
				setAuthContext(element.getValue().getAsString());
			} else if (element.getKey().equals(AUTH_TIME)) {
				setAuthTime(new Date(element.getValue().getAsLong() * 1000L));
			} else if (element.getKey().equals(AUTHORIZED_PARTY)) {
				setAuthorizedParty(element.getValue().getAsString());
	        } else {
	        	pass.add(element.getKey(), element.getValue());
	        }
        }    	
    	
	    super.loadFromJsonObject(pass);
    }
}
