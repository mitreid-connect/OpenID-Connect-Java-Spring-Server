/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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
package org.mitre.oauth2.introspectingfilter;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;


public class OAuth2AccessTokenImpl implements OAuth2AccessToken {

	private JsonObject introspectionResponse;
	private String tokenString;
	private Set<String> scopes = new HashSet<>();
	private Date expireDate;


	public OAuth2AccessTokenImpl(JsonObject introspectionResponse, String tokenString) {
		this.setIntrospectionResponse(introspectionResponse);
		this.tokenString = tokenString;
		if (introspectionResponse.get("scope") != null) {
			scopes = Sets.newHashSet(Splitter.on(" ").split(introspectionResponse.get("scope").getAsString()));
		}

		if (introspectionResponse.get("exp") != null) {
			expireDate = new Date(introspectionResponse.get("exp").getAsLong() * 1000L);
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


	/**
	 * @return the token
	 */
	public JsonObject getIntrospectionResponse() {
		return introspectionResponse;
	}


	/**
	 * @param token the token to set
	 */
	public void setIntrospectionResponse(JsonObject token) {
		this.introspectionResponse = token;
	}

}
