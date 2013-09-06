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
package org.mitre.oauth2.introspectingfilter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class OAuth2AccessTokenImpl implements OAuth2AccessToken {

	private JsonObject token;
	private String tokenString;
	private Set<String> scopes = new HashSet<String>();
	private Date expireDate;


	public OAuth2AccessTokenImpl(JsonObject token, String tokenString) {
		this.token = token;
		this.tokenString = tokenString;
		if (token.get("scope") != null) {
			scopes = Sets.newHashSet(Splitter.on(" ").split(token.get("scope").getAsString()));
		}

		DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		if (token.get("exp") != null) {
			try {
					expireDate = dateFormater.parse(token.get("exp").getAsString());
			} catch (ParseException ex) {
				Logger.getLogger(IntrospectingTokenService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}


	@Override
	public Map<String, Object> getAdditionalInformation() {
		return null;
	}

	@Override
	public Set<String> getScope() {
		return scopes;
	}

	@Override
	public OAuth2RefreshToken getRefreshToken() {
		return null;
	}

	@Override
	public String getTokenType() {
		return BEARER_TYPE;
	}

	@Override
	public boolean isExpired() {
		if (expireDate != null && expireDate.before(new Date())) {
			return true;
		}
		return false;
	}

	@Override
	public Date getExpiration() {
		return expireDate;
	}

	@Override
	public int getExpiresIn() {
		if (expireDate != null) {
			return (int)TimeUnit.MILLISECONDS.toSeconds(expireDate.getTime() - (new Date()).getTime());
		}
		return 0;
	}

	@Override
	public String getValue() {
		return tokenString;
	}

}
