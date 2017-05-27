/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

package org.mitre.oauth2.introspectingfilter.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.util.OAuth2Utils;

import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
public class ScopeBasedIntrospectionAuthoritiesGranter implements IntrospectionAuthorityGranter {

	private List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_API");

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionAuthorityGranter#getAuthorities(net.minidev.json.JSONObject)
	 */
	@Override
	public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
		List<GrantedAuthority> auth = new ArrayList<>(getAuthorities());

		if (introspectionResponse.has("scope") && introspectionResponse.get("scope").isJsonPrimitive()) {
			String scopeString = introspectionResponse.get("scope").getAsString();
			Set<String> scopes = OAuth2Utils.parseParameterList(scopeString);
			for (String scope : scopes) {
				auth.add(new SimpleGrantedAuthority("OAUTH_SCOPE_" + scope));
			}
		}

		return auth;
	}

	/**
	 * @return the authorities
	 */
	public List<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * @param authorities the authorities to set
	 */
	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

}
