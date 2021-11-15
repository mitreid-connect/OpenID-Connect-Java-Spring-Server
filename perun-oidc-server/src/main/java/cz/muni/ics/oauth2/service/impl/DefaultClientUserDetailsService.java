/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.oauth2.service.impl;

import com.google.common.base.Strings;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.stereotype.Service;

/**
 * Shim layer to convert a ClientDetails service into a UserDetails service
 *
 * @author AANGANES
 */
@Service("clientUserDetailsService")
public class DefaultClientUserDetailsService implements UserDetailsService {

	private static GrantedAuthority ROLE_CLIENT = new SimpleGrantedAuthority("ROLE_CLIENT");

	private ClientDetailsEntityService clientDetailsService;
	private final ConfigurationPropertiesBean config;

	@Autowired
	public DefaultClientUserDetailsService(ClientDetailsEntityService clientDetailsService, ConfigurationPropertiesBean config) {
		this.clientDetailsService = clientDetailsService;
		this.config = config;
	}

	public ClientDetailsEntityService getClientDetailsService() {
		return clientDetailsService;
	}

	public void setClientDetailsService(ClientDetailsEntityService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}

	@Override
	public UserDetails loadUserByUsername(String clientId) throws  UsernameNotFoundException {
		try {
			ClientDetailsEntity client = clientDetailsService.loadClientByClientId(clientId);
			if (client != null) {
				String password = Strings.nullToEmpty(client.getClientSecret());

				return ServiceUtils.getUserDetails(clientId, client, password, config, ROLE_CLIENT);
			} else {
				throw new UsernameNotFoundException("Client not found: " + clientId);
			}
		} catch (InvalidClientException e) {
			throw new UsernameNotFoundException("Client not found: " + clientId);
		}
	}

}
