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
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.mitre.discovery.util.JsonUtils.*;

/**
 * 
 * Dynamically fetches OpenID Connect server configurations based on the issuer. Caches the server configurations.
 * 
 * @author jricher
 *
 */
public class DynamicServerConfigurationService implements ServerConfigurationService {

	private static Logger logger = LoggerFactory.getLogger(DynamicServerConfigurationService.class);

	// map of issuer -> server configuration, loaded dynamically from service discovery
	private LoadingCache<String, ServerConfiguration> servers;

	private Set<String> whitelist = new HashSet<String>();
	private Set<String> blacklist = new HashSet<String>();

	public DynamicServerConfigurationService() {
		// initialize the cache
		servers = CacheBuilder.newBuilder().build(new OpenIDConnectServiceConfigurationFetcher());
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

	@Override
	public ServerConfiguration getServerConfiguration(String issuer) {
		try {

			if (!whitelist.isEmpty() && !whitelist.contains(issuer)) {
				throw new AuthenticationServiceException("Whitelist was nonempty, issuer was not in whitelist: " + issuer);
			}

			if (blacklist.contains(issuer)) {
				throw new AuthenticationServiceException("Issuer was in blacklist: " + issuer);
			}

			return servers.get(issuer);
		} catch (ExecutionException e) {
			logger.warn("Couldn't load configuration for " + issuer, e);
			return null;
		}

	}

	/**
	 * @author jricher
	 *
	 */
	private class OpenIDConnectServiceConfigurationFetcher extends CacheLoader<String, ServerConfiguration> {
		private HttpClient httpClient = new DefaultHttpClient();
		private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		private JsonParser parser = new JsonParser();

		@Override
		public ServerConfiguration load(String issuer) throws Exception {
			RestTemplate restTemplate = new RestTemplate(httpFactory);

			// data holder
			ServerConfiguration conf = new ServerConfiguration();

			// construct the well-known URI
			String url = issuer + "/.well-known/openid-configuration";

			// fetch the value
			String jsonString = restTemplate.getForObject(url, String.class);

			JsonElement parsed = parser.parse(jsonString);
			if (parsed.isJsonObject()) {

				JsonObject o = parsed.getAsJsonObject();

				// sanity checks
				if (!o.has("issuer")) {
					throw new IllegalStateException("Returned object did not have an 'issuer' field");
				}

				if (!issuer.equals(o.get("issuer").getAsString())) {
					throw new IllegalStateException("Discovered issuers didn't match, expected " + issuer + " got " + o.get("issuer").getAsString());
				}

				conf.setIssuer(o.get("issuer").getAsString());

				
				conf.setAuthorizationEndpointUri(getAsString(o, "authorization_endpoint"));
				conf.setTokenEndpointUri(getAsString(o, "token_endpoint"));
				conf.setJwksUri(getAsString(o, "jwks_uri"));
				conf.setUserInfoUri(getAsString(o, "userinfo_endpoint"));
				conf.setRegistrationEndpointUri(getAsString(o, "registration_endpoint"));
				conf.setIntrospectionEndpointUri(getAsString(o, "introspection_endpoint"));
				conf.setAcrValuesSupported(getAsStringList(o, "acr_values_supported"));
				conf.setCheckSessionIframe(getAsString(o, "check_session_iframe"));
				conf.setClaimsLocalesSupported(getAsStringList(o, "claims_locales_supported"));
				conf.setClaimsParameterSupported(getAsBoolean(o, "claims_parameter_supported"));
				conf.setClaimsSupported(getAsStringList(o, "claims_supported"));
				conf.setDisplayValuesSupported(getAsStringList(o, "display_values_supported"));
				conf.setEndSessionEndpoint(getAsString(o, "end_session_endpoint"));
				conf.setGrantTypesSupported(getAsStringList(o, "grant_types_supported"));
				conf.setIdTokenSigningAlgValuesSupported(getAsJwsAlgorithmList(o, "id_token_signing_alg_values_supported"));
				conf.setIdTokenEncryptionAlgValuesSupported(getAsJweAlgorithmList(o, "id_token_encryption_alg_values_supported"));
				conf.setIdTokenEncryptionEncValuesSupported(getAsEncryptionMethodList(o, "id_token_encryption_enc_values_supported"));
				conf.setOpPolicyUri(getAsString(o, "op_policy_uri"));
				conf.setOpTosUri(getAsString(o, "op_tos_uri"));
				conf.setRequestObjectEncryptionAlgValuesSupported(getAsJweAlgorithmList(o, "request_object_encryption_alg_values_supported"));
				conf.setRequestObjectEncryptionEncValuesSupported(getAsEncryptionMethodList(o, "request_object_encryption_enc_values_supported"));
				conf.setRequestObjectSigningAlgValuesSupported(getAsJwsAlgorithmList(o, "request_object_signing_alg_values_supported"));
				conf.setRequestParameterSupported(getAsBoolean(o, "request_parameter_supported"));
				conf.setRequestUriParameterSupported(getAsBoolean(o, "request_uri_parameter_supported"));
				conf.setResponseTypesSupported(getAsStringList(o, "response_types_supported"));
				conf.setScopesSupported(getAsStringList(o, "scopes_supported"));
				conf.setSubjectTypesSupported(getAsStringList(o, "subject_types_supported"));
				conf.setServiceDocumentation(getAsString(o, "service_documentation"));
				conf.setTokenEndpointAuthMethodsSupported(getAsStringList(o, "token_endpoint_auth_methods"));
				conf.setTokenEndpointAuthSigningAlgValuesSupported(getAsJwsAlgorithmList(o, "token_endpoint_auth_signing_alg_values_supported"));
				conf.setUiLocalesSupported(getAsStringList(o, "ui_locales_supported"));
				conf.setUserinfoEncryptionAlgValuesSupported(getAsJweAlgorithmList(o, "userinfo_encryption_alg_values_supported"));
				conf.setUserinfoEncryptionEncValuesSupported(getAsEncryptionMethodList(o, "userinfo_encryption_enc_values_supported"));
				conf.setUserinfoSigningAlgValuesSupported(getAsJwsAlgorithmList(o, "userinfo_signing_alg_values_supported"));

				return conf;
			} else {
				throw new IllegalStateException("Couldn't parse server discovery results for " + url);
			}

		}

	}

}
