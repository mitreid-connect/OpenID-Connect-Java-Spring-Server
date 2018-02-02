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
package org.mitre.oauth2.service.impl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

/**
 * Shim layer to convert a ClientDetails service into a UserDetails service
 *
 * @author AANGANES
 *
 */
@Service("clientUserDetailsService")
public class DefaultClientUserDetailsService implements UserDetailsService {

	private static GrantedAuthority ROLE_CLIENT = new SimpleGrantedAuthority("ROLE_CLIENT");

	@Autowired
	private ClientDetailsEntityService clientDetailsService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@Override
	public UserDetails loadUserByUsername(String clientId) throws  UsernameNotFoundException {

		try {
			ClientDetailsEntity client = clientDetailsService.loadClientByClientId(clientId);

			if (client != null) {

				String password = Strings.nullToEmpty(client.getClientSecret());

				if (config.isHeartMode() || // if we're running HEART mode turn off all client secrets
						(client.getTokenEndpointAuthMethod() != null &&
						(client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY) ||
								client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT)))) {

					// Issue a random password each time to prevent password auth from being used (or skipped)
					// for private key or shared key clients, see #715

					password = new BigInteger(512, new SecureRandom()).toString(16);
				}

				boolean enabled = true;
				boolean accountNonExpired = true;
				boolean credentialsNonExpired = true;
				boolean accountNonLocked = true;
				Collection<GrantedAuthority> authorities = new HashSet<>(client.getAuthorities());
				authorities.add(ROLE_CLIENT);

				return new User(clientId, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
			} else {
				throw new UsernameNotFoundException("Client not found: " + clientId);
			}
		} catch (InvalidClientException e) {
			throw new UsernameNotFoundException("Client not found: " + clientId);
		}

	}

	public ClientDetailsEntityService getClientDetailsService() {
		return clientDetailsService;
	}

	public void setClientDetailsService(ClientDetailsEntityService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}

}
