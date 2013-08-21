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
/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.RegisteredClientService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
public class DynamicRegistrationClientConfigurationService implements ClientConfigurationService {

	private static Logger logger = LoggerFactory.getLogger(DynamicServerConfigurationService.class);

	private LoadingCache<ServerConfiguration, RegisteredClient> clients;

	private RegisteredClientService registeredClientService = new InMemoryRegisteredClientService();

	// TODO: make sure the template doesn't have "client_id", "client_secret", or "registration_access_token" set on it already
	private RegisteredClient template;

	private Set<String> whitelist = new HashSet<String>();
	private Set<String> blacklist = new HashSet<String>();

	public DynamicRegistrationClientConfigurationService() {
		clients = CacheBuilder.newBuilder().build(new DynamicClientRegistrationLoader());
	}

	@Override
	public RegisteredClient getClientConfiguration(ServerConfiguration issuer) {
		try {
			if (!whitelist.isEmpty() && !whitelist.contains(issuer)) {
				throw new AuthenticationServiceException("Whitelist was nonempty, issuer was not in whitelist: " + issuer);
			}

			if (blacklist.contains(issuer)) {
				throw new AuthenticationServiceException("Issuer was in blacklist: " + issuer);
			}

			return clients.get(issuer);
		} catch (ExecutionException e) {
			logger.warn("Unable to get client configuration", e);
			return null;
		}
	}

	/**
	 * @return the template
	 */
	public RegisteredClient getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(RegisteredClient template) {
		this.template = template;
	}

	/**
	 * @return the registeredClientService
	 */
	public RegisteredClientService getRegisteredClientService() {
		return registeredClientService;
	}

	/**
	 * @param registeredClientService the registeredClientService to set
	 */
	public void setRegisteredClientService(RegisteredClientService registeredClientService) {
		this.registeredClientService = registeredClientService;
	}


	/**
	 * @return the whitelist
	 */
	public Set<String> getWhitelist() {
		return whitelist;
	}

	/**
	 * @param whitelist the whitelist to set
	 */
	public void setWhitelist(Set<String> whitelist) {
		this.whitelist = whitelist;
	}

	/**
	 * @return the blacklist
	 */
	public Set<String> getBlacklist() {
		return blacklist;
	}

	/**
	 * @param blacklist the blacklist to set
	 */
	public void setBlacklist(Set<String> blacklist) {
		this.blacklist = blacklist;
	}


	/**
	 * Loader class that fetches the client information.
	 * 
	 * If a client has been registered (ie, it's known to the RegisteredClientService), then this
	 * will fetch the client's configuration from the server.
	 * 
	 * @author jricher
	 *
	 */
	public class DynamicClientRegistrationLoader extends CacheLoader<ServerConfiguration, RegisteredClient> {
		private HttpClient httpClient = new DefaultHttpClient();
		private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		private Gson gson = new Gson(); // note that this doesn't serialize nulls by default
		
		@Override
		public RegisteredClient load(ServerConfiguration serverConfig) throws Exception {
			RestTemplate restTemplate = new RestTemplate(httpFactory);


			RegisteredClient knownClient = registeredClientService.getByIssuer(serverConfig.getIssuer());
			if (knownClient == null) {

				// dynamically register this client
				JsonObject jsonRequest = ClientDetailsEntityJsonProcessor.serialize(template);
				String serializedClient = gson.toJson(jsonRequest);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

				HttpEntity<String> entity = new HttpEntity<String>(serializedClient, headers);

				String registered = restTemplate.postForObject(serverConfig.getRegistrationEndpointUri(), entity, String.class);
				// TODO: handle HTTP errors

				RegisteredClient client = ClientDetailsEntityJsonProcessor.parseRegistered(registered);

				// save this client for later
				registeredClientService.save(serverConfig.getIssuer(), client);

				return client;
			} else {

				// load this client's information from the server
				HttpHeaders headers = new HttpHeaders();
				headers.set("Authorization", String.format("%s %s", OAuth2AccessToken.BEARER_TYPE, knownClient.getRegistrationAccessToken()));
				headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

				HttpEntity<String> entity = new HttpEntity<String>(headers);

				String registered = restTemplate.exchange(knownClient.getRegistrationClientUri(), HttpMethod.GET, entity, String.class).getBody();
				// TODO: handle HTTP errors

				RegisteredClient client = ClientDetailsEntityJsonProcessor.parseRegistered(registered);

				return client;
			}
		}

	}

}
