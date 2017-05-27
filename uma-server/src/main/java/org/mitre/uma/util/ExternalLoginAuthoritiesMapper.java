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

package org.mitre.uma.util;

import java.util.Collection;

import org.mitre.openid.connect.client.OIDCAuthoritiesMapper;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.common.collect.Sets;
import com.nimbusds.jwt.JWT;

/**
 * Utility class to map all external logins to the ROLE_EXTERNAL_USER authority
 * to prevent them from accessing other parts of the server.
 *
 * @author jricher
 *
 */
public class ExternalLoginAuthoritiesMapper implements OIDCAuthoritiesMapper {

	private static final GrantedAuthority ROLE_EXTERNAL_USER = new SimpleGrantedAuthority("ROLE_EXTERNAL_USER");

	@Override
	public Collection<? extends GrantedAuthority> mapAuthorities(JWT idToken, UserInfo userInfo) {
		return Sets.newHashSet(ROLE_EXTERNAL_USER);
	}

}
