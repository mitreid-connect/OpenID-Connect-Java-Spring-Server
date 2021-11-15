/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

/**
 * Loads client details based on URI encoding as passed in from basic auth.
 *
 *  Should only get called if non-encoded provider fails.
 *
 * @author AANGANES
 */
@Service("uriEncodedClientUserDetailsService")
public class UriEncodedClientUserDetailsService implements UserDetailsService {

	private static GrantedAuthority ROLE_CLIENT = new SimpleGrantedAuthority("ROLE_CLIENT");

	private ClientDetailsEntityService clientDetailsService;
	private final ConfigurationPropertiesBean config;

	@Autowired
	public UriEncodedClientUserDetailsService(ClientDetailsEntityService clientDetailsService, ConfigurationPropertiesBean config) {
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
			String decodedClientId = UriUtils.decode(clientId, "UTF-8");
			ClientDetailsEntity client = clientDetailsService.loadClientByClientId(decodedClientId);

			if (client != null) {
				String encodedPassword = UriUtils.encodePathSegment(Strings.nullToEmpty(client.getClientSecret()), "UTF-8");
				return ServiceUtils.getUserDetails(decodedClientId, client, encodedPassword, config, ROLE_CLIENT);
			} else {
				throw new UsernameNotFoundException("Client not found: " + clientId);
			}
		} catch (UnsupportedEncodingException | InvalidClientException e) {
			throw new UsernameNotFoundException("Client not found: " + clientId);
		}
	}

}
