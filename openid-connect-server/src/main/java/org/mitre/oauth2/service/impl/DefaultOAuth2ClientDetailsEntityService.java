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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Service
public class DefaultOAuth2ClientDetailsEntityService implements ClientDetailsEntityService {

	private static Logger logger = LoggerFactory.getLogger(DefaultOAuth2ClientDetailsEntityService.class);
	
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

	// map of sector URI -> list of redirect URIs
	private LoadingCache<String, List<String>> sectorRedirects = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.maximumSize(100)
			.build(new SectorIdentifierLoader());

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

		// if the client is flagged to allow for refresh tokens, make sure it's got the right granted scopes
		if (client.isAllowRefresh()) {
			client.getScope().add("offline_access");
		} else {
			client.getScope().remove("offline_access");
		}

		// timestamp this to right now
		client.setCreatedAt(new Date());
		
		
		// check the sector URI
		if (!Strings.isNullOrEmpty(client.getSectorIdentifierUri())) {
			try {
	            List<String> redirects = sectorRedirects.get(client.getSectorIdentifierUri());
	            
	            if (client.getRegisteredRedirectUri() != null) {
	            	for (String uri : client.getRegisteredRedirectUri()) {
	            		if (!redirects.contains(uri)) {
	            			throw new IllegalArgumentException("Requested Redirect URI " + uri + " is not listed at sector identifier " + redirects);
	            		}
	            	}
	            }
	            
            } catch (ExecutionException e) {
            	throw new IllegalArgumentException("Unable to load sector identifier URI: " + client.getSectorIdentifierUri());
            }
		}
		

		return clientRepository.saveClient(client);
	}

	/**
	 * Get the client by its internal ID
	 */
	@Override
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

			// if the client is flagged to allow for refresh tokens, make sure it's got the right scope
			if (newClient.isAllowRefresh()) {
				newClient.getScope().add("offline_access");
			} else {
				newClient.getScope().remove("offline_access");
			}

			// check the sector URI
			if (!Strings.isNullOrEmpty(newClient.getSectorIdentifierUri())) {
				try {
		            List<String> redirects = sectorRedirects.get(newClient.getSectorIdentifierUri());
		            
		            if (newClient.getRegisteredRedirectUri() != null) {
		            	for (String uri : newClient.getRegisteredRedirectUri()) {
		            		if (!redirects.contains(uri)) {
		            			throw new IllegalArgumentException("Requested Redirect URI " + uri + " is not listed at sector identifier " + redirects);
		            		}
		            	}
		            }
		            
	            } catch (ExecutionException e) {
	            	throw new IllegalArgumentException("Unable to load sector identifier URI: " + newClient.getSectorIdentifierUri());
	            }
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

	/**
	 * Utility class to load a sector identifier's set of authorized redirect URIs.
	 * 
     * @author jricher
     *
     */
    private class SectorIdentifierLoader extends CacheLoader<String, List<String>> {
		private HttpClient httpClient = new DefaultHttpClient();
		private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		private RestTemplate restTemplate = new RestTemplate(httpFactory);
		private JsonParser parser = new JsonParser();
		
        @Override
        public List<String> load(String key) throws Exception {

        	if (!key.startsWith("https")) {
        		// TODO: this should optionally throw an error (#506)
        		logger.error("Sector identifier doesn't start with https, loading anyway...");
        	}
        	
        	// key is the sector URI
			String jsonString = restTemplate.getForObject(key, String.class);
			JsonElement json = parser.parse(jsonString);
			
			if (json.isJsonArray()) {
				List<String> redirectUris = new ArrayList<String>();
				for (JsonElement el : json.getAsJsonArray()) {
	                redirectUris.add(el.getAsString());
                }

				logger.info("Found " + redirectUris + " for sector " + key);
				
				return redirectUris;
			} else {
				return null;
			}
        	
        }

    }

}
