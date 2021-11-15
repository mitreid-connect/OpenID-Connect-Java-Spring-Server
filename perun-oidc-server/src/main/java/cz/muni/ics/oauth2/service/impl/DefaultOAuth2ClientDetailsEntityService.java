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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AuthMethod;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.repository.OAuth2ClientRepository;
import cz.muni.ics.oauth2.repository.OAuth2TokenRepository;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.model.WhitelistedSite;
import cz.muni.ics.openid.connect.service.ApprovedSiteService;
import cz.muni.ics.openid.connect.service.BlacklistedSiteService;
import cz.muni.ics.openid.connect.service.WhitelistedSiteService;
import cz.muni.ics.uma.model.ResourceSet;
import cz.muni.ics.uma.service.ResourceSetService;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
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

	@Autowired
	private SystemScopeService scopeService;

	@Autowired
	private ResourceSetService resourceSetService;

	@Autowired
	private ConfigurationPropertiesBean config;

	// map of sector URI -> list of redirect URIs
	private LoadingCache<String, List<String>> sectorRedirects = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.maximumSize(100)
			.build(new SectorIdentifierLoader(HttpClientBuilder.create().useSystemProperties().build()));

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

		// make sure that clients with the "refresh_token" grant type have the "offline_access" scope, and vice versa
		ensureRefreshTokenConsistency(client);

		// make sure we don't have both a JWKS and a JWKS URI
		ensureKeyConsistency(client);

		// check consistency when using HEART mode
		checkHeartMode(client);

		// timestamp this to right now
		client.setCreatedAt(new Date());


		// check the sector URI
		checkSectorIdentifierUri(client);

		ensureNoReservedScopes(client);
		return clientRepository.saveClient(client);
	}

	/**
	 * Make sure the client has only one type of key registered
	 * @param client
	 */
	private void ensureKeyConsistency(ClientDetailsEntity client) {
		if (client.getJwksUri() != null && client.getJwks() != null) {
			// a client can only have one key type or the other, not both
			throw new IllegalArgumentException("A client cannot have both JWKS URI and JWKS value");
		}
	}

	/**
	 * Make sure the client doesn't request any system reserved scopes
	 */
	private void ensureNoReservedScopes(ClientDetailsEntity client) {
		// make sure a client doesn't get any special system scopes
		Set<SystemScope> requestedScope = scopeService.fromStrings(client.getScope());

		requestedScope = scopeService.removeReservedScopes(requestedScope);

		client.setScope(scopeService.toStrings(requestedScope));
	}

	/**
	 * Load the sector identifier URI if it exists and check the redirect URIs against it
	 * @param client
	 */
	private void checkSectorIdentifierUri(ClientDetailsEntity client) {
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

			} catch (UncheckedExecutionException | ExecutionException e) {
				throw new IllegalArgumentException("Unable to load sector identifier URI " + client.getSectorIdentifierUri() + ": " + e.getMessage());
			}
		}
	}

	/**
	 * Make sure the client has the appropriate scope and grant type.
	 * @param client
	 */
	private void ensureRefreshTokenConsistency(ClientDetailsEntity client) {
		if (client.getAuthorizedGrantTypes().contains("refresh_token")
				|| client.getScope().contains(SystemScopeService.OFFLINE_ACCESS)) {
			client.getScope().add(SystemScopeService.OFFLINE_ACCESS);
			client.getAuthorizedGrantTypes().add("refresh_token");
		}
	}

	/**
	 * If HEART mode is enabled, make sure the client meets the requirements:
	 *  - Only one of authorization_code, implicit, or client_credentials can be used at a time
	 *  - A redirect_uri must be registered with either authorization_code or implicit
	 *  - A key must be registered
	 *  - A client secret must not be generated
	 *  - authorization_code and client_credentials must use the private_key authorization method
	 * @param client
	 */
	private void checkHeartMode(ClientDetailsEntity client) {
		if (config.isHeartMode()) {
			if (client.getGrantTypes().contains("authorization_code")) {
				// make sure we don't have incompatible grant types
				if (client.getGrantTypes().contains("implicit") || client.getGrantTypes().contains("client_credentials")) {
					throw new IllegalArgumentException("[HEART mode] Incompatible grant types");
				}

				// make sure we've got the right authentication method
				if (client.getTokenEndpointAuthMethod() == null || !client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY)) {
					throw new IllegalArgumentException("[HEART mode] Authorization code clients must use the private_key authentication method");
				}

				// make sure we've got a redirect URI
				if (client.getRedirectUris().isEmpty()) {
					throw new IllegalArgumentException("[HEART mode] Authorization code clients must register at least one redirect URI");
				}
			}

			if (client.getGrantTypes().contains("implicit")) {
				// make sure we don't have incompatible grant types
				if (client.getGrantTypes().contains("authorization_code") || client.getGrantTypes().contains("client_credentials") || client.getGrantTypes().contains("refresh_token")) {
					throw new IllegalArgumentException("[HEART mode] Incompatible grant types");
				}

				// make sure we've got the right authentication method
				if (client.getTokenEndpointAuthMethod() == null || !client.getTokenEndpointAuthMethod().equals(AuthMethod.NONE)) {
					throw new IllegalArgumentException("[HEART mode] Implicit clients must use the none authentication method");
				}

				// make sure we've got a redirect URI
				if (client.getRedirectUris().isEmpty()) {
					throw new IllegalArgumentException("[HEART mode] Implicit clients must register at least one redirect URI");
				}
			}

			if (client.getGrantTypes().contains("client_credentials")) {
				// make sure we don't have incompatible grant types
				if (client.getGrantTypes().contains("authorization_code") || client.getGrantTypes().contains("implicit") || client.getGrantTypes().contains("refresh_token")) {
					throw new IllegalArgumentException("[HEART mode] Incompatible grant types");
				}

				// make sure we've got the right authentication method
				if (client.getTokenEndpointAuthMethod() == null || !client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY)) {
					throw new IllegalArgumentException("[HEART mode] Client credentials clients must use the private_key authentication method");
				}

				// make sure we've got a redirect URI
				if (!client.getRedirectUris().isEmpty()) {
					throw new IllegalArgumentException("[HEART mode] Client credentials clients must not register a redirect URI");
				}

			}

			if (client.getGrantTypes().contains("password")) {
				throw new IllegalArgumentException("[HEART mode] Password grant type is forbidden");
			}

			// make sure we don't have a client secret
			if (!Strings.isNullOrEmpty(client.getClientSecret())) {
				throw new IllegalArgumentException("[HEART mode] Client secrets are not allowed");
			}

			// make sure we've got a key registered
			if (client.getJwks() == null && Strings.isNullOrEmpty(client.getJwksUri())) {
				throw new IllegalArgumentException("[HEART mode] All clients must have a key registered");
			}

			// make sure our redirect URIs each fit one of the allowed categories
			if (client.getRedirectUris() != null && !client.getRedirectUris().isEmpty()) {
				boolean localhost = false;
				boolean remoteHttps = false;
				boolean customScheme = false;
				for (String uri : client.getRedirectUris()) {
					UriComponents components = UriComponentsBuilder.fromUriString(uri).build();
					if (components.getScheme() == null) {
						// this is a very unknown redirect URI
						customScheme = true;
					} else if (components.getScheme().equals("http")) {
						// http scheme, check for localhost
						if (components.getHost().equals("localhost") || components.getHost().equals("127.0.0.1")) {
							localhost = true;
						} else {
							throw new IllegalArgumentException("[HEART mode] Can't have an http redirect URI on non-local host");
						}
					} else if (components.getScheme().equals("https")) {
						remoteHttps = true;
					} else {
						customScheme = true;
					}
				}

				// now we make sure the client has a URI in only one of each of the three categories
				if (!((localhost ^ remoteHttps ^ customScheme)
						&& !(localhost && remoteHttps && customScheme))) {
					throw new IllegalArgumentException("[HEART mode] Can't have more than one class of redirect URI");
				}
			}

		}
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

		// clear out resource sets registered for this client
		Collection<ResourceSet> resourceSets = resourceSetService.getAllForClient(client);
		for (ResourceSet rs : resourceSets) {
			resourceSetService.remove(rs);
		}

		// take care of the client itself
		clientRepository.deleteClient(client);
	}

	/**
	 * Update the oldClient with information from the newClient. The
	 * id from oldClient is retained.
	 *
	 * Checks to make sure the refresh grant type and
	 * the scopes are set appropriately.
	 *
	 * Checks to make sure the redirect URIs aren't blacklisted.
	 *
	 * Attempts to load the redirect URI (possibly cached) to check the
	 * sector identifier against the contents there.
	 *
	 *
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
			ensureRefreshTokenConsistency(newClient);

			// make sure we don't have both a JWKS and a JWKS URI
			ensureKeyConsistency(newClient);

			// check consistency when using HEART mode
			checkHeartMode(newClient);

			// check the sector URI
			checkSectorIdentifierUri(newClient);

			// make sure a client doesn't get any special system scopes
			ensureNoReservedScopes(newClient);

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
		if (config.isHeartMode()) {
			log.error("[HEART mode] Can't generate a client secret, skipping step; client won't be saved due to invalid configuration");
			client.setClientSecret(null);
		} else {
			client.setClientSecret(Base64.encodeBase64URLSafeString(new BigInteger(512, new SecureRandom()).toByteArray()).replace("=", ""));
		}
		return client;
	}

	/**
	 * Utility class to load a sector identifier's set of authorized redirect URIs.
	 *
	 * @author jricher
	 *
	 */
	private class SectorIdentifierLoader extends CacheLoader<String, List<String>> {
		private HttpComponentsClientHttpRequestFactory httpFactory;
		private RestTemplate restTemplate;
		private JsonParser parser = new JsonParser();

		SectorIdentifierLoader(HttpClient httpClient) {
			this.httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			this.restTemplate = new RestTemplate(httpFactory);
		}

		@Override
		public List<String> load(String key) throws Exception {

			if (!key.startsWith("https")) {
				if (config.isForceHttps()) {
					throw new IllegalArgumentException("Sector identifier must start with https: " + key);
				}
				log.error("Sector identifier doesn't start with https, loading anyway...");
			}

			// key is the sector URI
			String jsonString = restTemplate.getForObject(key, String.class);
			JsonElement json = parser.parse(jsonString);

			if (json.isJsonArray()) {
				List<String> redirectUris = new ArrayList<>();
				for (JsonElement el : json.getAsJsonArray()) {
					redirectUris.add(el.getAsString());
				}

				log.info("Found " + redirectUris + " for sector " + key);

				return redirectUris;
			} else {
				throw new IllegalArgumentException("JSON Format Error");
			}

		}

	}

}
