/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntityFactory;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

@Service
@Transactional
public class DefaultOAuth2ClientDetailsEntityService implements ClientDetailsEntityService {

	@Autowired
	private OAuth2ClientRepository clientRepository;
	
	@Autowired
	private OAuth2TokenRepository tokenRepository;
	
	@Autowired
	private ClientDetailsEntityFactory clientFactory;
	
	public DefaultOAuth2ClientDetailsEntityService() {
		
	}
	
	public DefaultOAuth2ClientDetailsEntityService(OAuth2ClientRepository clientRepository, 
			OAuth2TokenRepository tokenRepository, ClientDetailsEntityFactory clientFactory) {
		this.clientRepository = clientRepository;
		this.tokenRepository = tokenRepository;
		this.clientFactory = clientFactory;
	}
	
	/**
	 * Get the client for the given ID
	 */
	@Override
	public ClientDetailsEntity loadClientByClientId(String clientId) throws OAuth2Exception, InvalidClientException, IllegalArgumentException {
		if (!Strings.isNullOrEmpty(clientId)) {
			ClientDetailsEntity client = clientRepository.getClientById(clientId);
			if (client == null) {
				throw new InvalidClientException("Client with id " + clientId + " was not found");
			}
			else {
				return client;
			}
		}
		
		throw new IllegalArgumentException("Client id must not be empty!");
	}
	
	/**
	 * Create a new client with the appropriate fields filled in
	 */
	@Override
    public ClientDetailsEntity createClient(String clientId, String clientSecret, 
    		Set<String> scope, Set<String> grantTypes, 
    		String redirectUri, Set<GrantedAuthority> authorities,
    		Set<String> resourceIds,
    		String name, String description, boolean allowRefresh, 
    		Long accessTokenTimeout, 
    		Long refreshTokenTimeout, String owner) {
		
		//TODO should throw invalid client error exceptions, data validation and sizing
		
		// TODO: check "owner" locally?
		ClientDetailsEntity client = clientFactory.createClient(clientId, clientSecret);
		
		client.setScope(scope);  //check for valid values
		client.setAuthorizedGrantTypes(grantTypes); //check for valid values
		
		// TODO problem found in testing the client.getRegisteredRedirectUri() doesn't return same datatype as expected by this method
		Set<String> redirectUris = new HashSet<String>();
		redirectUris.add(redirectUri);
		client.setRegisteredRedirectUri(redirectUris);  //check for valid values, whitelist, and blacklist, valid URI and also require HTTPS
		
		client.setAuthorities(authorities); //check for valid values
		
		client.setClientName(name); // check for xss
		client.setClientDescription(description); // check for xss
		client.setAllowRefresh(allowRefresh); // db handles actual value
		client.setAccessTokenTimeout(accessTokenTimeout);  //INT range should be 0 - 3600, if in seconds
		client.setRefreshTokenTimeout(refreshTokenTimeout); //INT range should be 0 - 3600, if in seconds
		client.setResourceIds(resourceIds); 
		client.setOwner(owner); // why not rename to setClientOwner like the other text string fields?
		
		clientRepository.saveClient(client);
		return client;
	}
	
	/**
	 * Delete a client and all its associated tokens
	 */
	@Override
    public void deleteClient(ClientDetailsEntity client) throws InvalidClientException {
		
		if (clientRepository.getClientById(client.getClientId()) == null) {
			throw new InvalidClientException("Client with id " + client.getClientId() + " was not found");
		}
		
		// clean out any tokens that this client had issued
		tokenRepository.clearTokensForClient(client);
		
		// take care of the client itself
		clientRepository.deleteClient(client);
		
	}

	/**
	 * Update the oldClient with information from the newClient. The 
	 * id from oldClient is retained.
	 */
	@Override
    public ClientDetailsEntity updateClient(ClientDetailsEntity oldClient, ClientDetailsEntity newClient) throws IllegalArgumentException {
		if (oldClient != null && newClient != null) {
			return clientRepository.updateClient(oldClient.getClientId(), newClient);
		}
		throw new IllegalArgumentException("Neither old client or new client can be null!");
    }

	/**
	 * Get all clients in the system
	 */
	@Override
    public Collection<ClientDetailsEntity> getAllClients() {
		return clientRepository.getAllClients();
    }

}
