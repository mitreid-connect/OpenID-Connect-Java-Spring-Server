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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

@Service
public class DefaultOAuth2ClientDetailsEntityService implements ClientDetailsEntityService {
	
	@Autowired
	private OAuth2ClientRepository clientRepository;
	
	@Autowired
	private OAuth2TokenRepository tokenRepository;
	
	@Autowired
	private ApprovedSiteService approvedSiteService;
	
	@Autowired
	private WhitelistedSiteService whitelistedSiteService;
	
	@Autowired
	private BlacklistedSiteService blacklistedSiteService;
	
	public DefaultOAuth2ClientDetailsEntityService() {
		
	}
	
	public DefaultOAuth2ClientDetailsEntityService(OAuth2ClientRepository clientRepository, 
			OAuth2TokenRepository tokenRepository) {
		this.clientRepository = clientRepository;
		this.tokenRepository = tokenRepository;
	}
	
	@Override
	public ClientDetailsEntity saveNewClient(ClientDetailsEntity client) {
		if (client.getId() != null) { // if it's not null, it's already been saved, this is an error
			throw new IllegalArgumentException("Tried to save a new client with an existing ID: " + client.getId());
		}

		if (client.getRegisteredRedirectUri() != null) {
			for (String uri : client.getRegisteredRedirectUri()) {
				if (blacklistedSiteService.isBlacklisted(uri)) {
					throw new IllegalArgumentException("Client URI is blacklisted: " + uri);
				}
		    }
		}
		
		// assign a random clientid if it's empty 
		// NOTE: don't assign a random client secret without asking, since public clients have no secret
        if (Strings.isNullOrEmpty(client.getClientId())) {
            client = generateClientId(client);
        }
        
        // if the client is flagged to allow for refresh tokens, make sure it's got the right granted authority
        if (client.isAllowRefresh()) {
        	client.getAuthorizedGrantTypes().add("refresh_token");
        } else {
        	client.getAuthorizedGrantTypes().remove("refresh_token");
        }
        if (client.getAuthorizedGrantTypes().contains("refresh_token")) {
        	client.setAllowRefresh(true);
        } else {
        	client.setAllowRefresh(false);
        }
        
        return clientRepository.saveClient(client);
	}
	
	/**
	 * Get the client by its internal ID
	 */
	public ClientDetailsEntity getClientById(Long id) {
		ClientDetailsEntity client = clientRepository.getById(id);
		
		return client;
	}
	
	/**
	 * Get the client for the given ClientID
	 */
	@Override
	public ClientDetailsEntity loadClientByClientId(String clientId) throws OAuth2Exception, InvalidClientException, IllegalArgumentException {
		if (!Strings.isNullOrEmpty(clientId)) {
			ClientDetailsEntity client = clientRepository.getClientByClientId(clientId);
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
	 * Delete a client and all its associated tokens
	 */
	@Override
    public void deleteClient(ClientDetailsEntity client) throws InvalidClientException {
		
		if (clientRepository.getById(client.getId()) == null) {
			throw new InvalidClientException("Client with id " + client.getClientId() + " was not found");
		}
		
		// clean out any tokens that this client had issued
		tokenRepository.clearTokensForClient(client);
		
		// clean out any approved sites for this client
		approvedSiteService.clearApprovedSitesForClient(client);
		
		// clear out any whitelisted sites for this client
		WhitelistedSite whitelistedSite = whitelistedSiteService.getByClientId(client.getClientId());
		if (whitelistedSite != null) {
            whitelistedSiteService.remove(whitelistedSite);
		}
		
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
			
			for (String uri : newClient.getRegisteredRedirectUri()) {
				if (blacklistedSiteService.isBlacklisted(uri)) {
					throw new IllegalArgumentException("Client URI is blacklisted: " + uri);
				}
	        }
			
	        // if the client is flagged to allow for refresh tokens, make sure it's got the right granted authority
	        if (newClient.isAllowRefresh()) {
	        	newClient.getAuthorizedGrantTypes().add("refresh_token");
	        } else {
	        	newClient.getAuthorizedGrantTypes().remove("refresh_token");
	        }
	        if (newClient.getAuthorizedGrantTypes().contains("refresh_token")) {
	        	newClient.setAllowRefresh(true);
	        } else {
	        	newClient.setAllowRefresh(false);
	        }

	        return clientRepository.updateClient(oldClient.getId(), newClient);
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

	/**
	 * Generates a clientId for the given client and sets it to the client's clientId field. Returns the client that was passed in, now with id set.
	 */
	@Override
    public ClientDetailsEntity generateClientId(ClientDetailsEntity client) {
		client.setClientId(UUID.randomUUID().toString());
	    return client;
    }

	/**
	 * Generates a new clientSecret for the given client and sets it to the client's clientSecret field. Returns the client that was passed in, now with secret set.
	 */
	@Override
    public ClientDetailsEntity generateClientSecret(ClientDetailsEntity client) {
		client.setClientSecret(Base64.encodeBase64URLSafeString(new BigInteger(512, new SecureRandom()).toByteArray()).replace("=", ""));
	    return client;
    }

}
