/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
/**
 *
 */
package org.mitre.oauth2.introspectingfilter.service.impl;

import java.util.List;

import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.google.gson.JsonObject;

/**
 *
 * Grants the same set of authorities no matter what's passed in.
 *
 * @author jricher
 *
 */
public class SimpleIntrospectionAuthorityGranter implements IntrospectionAuthorityGranter {

	private List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_API");

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionAuthorityGranter#getAuthorities(net.minidev.json.JSONObject)
	 */
	@Override
	public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
		return authorities;
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
