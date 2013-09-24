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
package org.mitre.oauth2.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

/**
 * Shim layer to convert a ClientDetails service into a UserDetails service
 * 
 * @author AANGANES
 *
 */
@Service("clientUserDetailsService")
public class DefaultClientUserDetailsService implements UserDetailsService {

	@Autowired
	private ClientDetailsService clientDetailsService;

	@Override
	public UserDetails loadUserByUsername(String clientId) throws  UsernameNotFoundException {

		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);

		if (client != null) {

			String password = client.getClientSecret();
			boolean enabled = true;
			boolean accountNonExpired = true;
			boolean credentialsNonExpired = true;
			boolean accountNonLocked = true;
			Collection<GrantedAuthority> authorities = client.getAuthorities();
			if (authorities == null || authorities.isEmpty()) {
				// automatically inject ROLE_CLIENT if none exists ...
				// TODO: this should probably happen on the client service side instead to keep it in the real data model
				authorities = new ArrayList<GrantedAuthority>();
				GrantedAuthority roleClient = new SimpleGrantedAuthority("ROLE_CLIENT");
				authorities.add(roleClient);
			}

			return new User(clientId, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		} else {
			throw new UsernameNotFoundException("Client not found: " + clientId);
		}

	}

	public ClientDetailsService getClientDetailsService() {
		return clientDetailsService;
	}

	public void setClientDetailsService(ClientDetailsService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}

}
