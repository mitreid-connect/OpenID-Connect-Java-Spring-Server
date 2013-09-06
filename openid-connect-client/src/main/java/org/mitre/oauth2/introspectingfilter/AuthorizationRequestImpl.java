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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizationRequest;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AuthorizationRequestImpl implements AuthorizationRequest {

	private JsonObject token;
	private String clientId;
	private Set<String> scopes = new HashSet<String>();

	public AuthorizationRequestImpl(JsonObject token) {
		this.token = token;
		clientId = token.get("client_id").getAsString();
		if (token.get("scope") != null) {
			scopes = Sets.newHashSet(Splitter.on(" ").split(token.get("scope").getAsString()));
		}
	}

	@Override
	public Map<String, String> getAuthorizationParameters() {
		return null;
	}

	@Override
	public Map<String, String> getApprovalParameters() {
		return null;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public Set<String> getScope() {

		return scopes;
	}

	@Override
	public Set<String> getResourceIds() {
		return null;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public boolean isApproved() {
		return true;
	}

	@Override
	public boolean isDenied() {
		return false;
	}

	@Override
	public String getState() {
		return null;
	}

	@Override
	public String getRedirectUri() {
		return null;
	}

	@Override
	public Set<String> getResponseTypes() {
		return null;
	}

}
