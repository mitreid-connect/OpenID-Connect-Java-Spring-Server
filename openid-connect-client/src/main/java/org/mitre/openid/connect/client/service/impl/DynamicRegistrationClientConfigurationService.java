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

import java.util.concurrent.ExecutionException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author jricher
 *
 */
public class DynamicRegistrationClientConfigurationService implements ClientConfigurationService {

	private static Logger logger = LoggerFactory.getLogger(DynamicServerConfigurationService.class);

	private LoadingCache<ServerConfiguration, ClientDetailsEntity> clients;

	private ClientDetailsEntity template;

	public DynamicRegistrationClientConfigurationService() {
		clients = CacheBuilder.newBuilder().build(new DynamicClientRegistrationLoader());
	}

	@Override
	public ClientDetailsEntity getClientConfiguration(ServerConfiguration issuer) {
		try {
			return clients.get(issuer);
		} catch (ExecutionException e) {
			logger.warn("Unable to get client configuration", e);
			return null;
		}
	}

	/**
	 * @return the template
	 */
	public ClientDetailsEntity getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(ClientDetailsEntity template) {
		this.template = template;
	}

	public class DynamicClientRegistrationLoader extends CacheLoader<ServerConfiguration, ClientDetailsEntity> {
		private HttpClient httpClient = new DefaultHttpClient();
		private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		private JsonParser parser = new JsonParser();

		@Override
		public ClientDetailsEntity load(ServerConfiguration serverConfig) throws Exception {
			RestTemplate restTemplate = new RestTemplate(httpFactory);

			// dynamically register this client
			JsonObject jsonRequest = ClientDetailsEntityJsonProcessor.serialize(template, null, null);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

			HttpEntity<String> entity = new HttpEntity<String>(jsonRequest.toString(), headers);

			String registered = restTemplate.postForObject(serverConfig.getRegistrationEndpointUri(), entity, String.class);
			// TODO: handle HTTP errors

			// TODO: save registration token and other important bits
			ClientDetailsEntity client = ClientDetailsEntityJsonProcessor.parse(registered);

			return client;
		}

	}

}
