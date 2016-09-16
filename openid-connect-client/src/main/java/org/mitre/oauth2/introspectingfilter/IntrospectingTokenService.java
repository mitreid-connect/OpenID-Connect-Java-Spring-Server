/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.oauth2.introspectingfilter;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.SimpleIntrospectionAuthorityGranter;
import org.mitre.oauth2.model.RegisteredClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.util.Base64;

import static org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod.SECRET_BASIC;

/**
 * This ResourceServerTokenServices implementation introspects incoming tokens at a
 * server's introspection endpoint URL and passes an Authentication object along
 * based on the response from the introspection endpoint.
 * @author jricher
 *
 */
public class IntrospectingTokenService implements ResourceServerTokenServices {

	private IntrospectionConfigurationService introspectionConfigurationService;
	private IntrospectionAuthorityGranter introspectionAuthorityGranter = new SimpleIntrospectionAuthorityGranter();

	private int defaultExpireTime = 300000; // 5 minutes in milliseconds
	private boolean forceCacheExpireTime = false; // force removal of cached tokens based on default expire time
	private boolean cacheNonExpiringTokens = false;
	private boolean cacheTokens = true;

	private HttpComponentsClientHttpRequestFactory factory;

	public IntrospectingTokenService() {
		this(HttpClientBuilder.create().useSystemProperties().build());
	}

	public IntrospectingTokenService(HttpClient httpClient) {
		this.factory = new HttpComponentsClientHttpRequestFactory(httpClient);
	}

	// Inner class to store in the hash map
	private class TokenCacheObject {
		OAuth2AccessToken token;
		OAuth2Authentication auth;
		Date cacheExpire;

		private TokenCacheObject(OAuth2AccessToken token, OAuth2Authentication auth) {
			this.token = token;
			this.auth = auth;

			// we don't need to check the cacheTokens values, because this won't actually be added to the cache if cacheTokens is false
			// if the token isn't null we use the token expire time
			// if forceCacheExpireTime is also true, we also make sure that the token expire time is shorter than the default expire time
			if ((this.token.getExpiration() != null) && (!forceCacheExpireTime || (forceCacheExpireTime && (this.token.getExpiration().getTime() - System.currentTimeMillis() <= defaultExpireTime)))) {
				this.cacheExpire = this.token.getExpiration();
			} else { // if the token doesn't have an expire time, or if the using forceCacheExpireTime the token expire time is longer than the default, then use the default expire time
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MILLISECOND, defaultExpireTime);
				this.cacheExpire = cal.getTime();
			}
		}
	}

	private Map<String, TokenCacheObject> authCache = new HashMap<>();
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(IntrospectingTokenService.class);

	/**
	 * @return the introspectionConfigurationService
	 */
	public IntrospectionConfigurationService getIntrospectionConfigurationService() {
		return introspectionConfigurationService;
	}

	/**
	 * @param introspectionConfigurationService the introspectionConfigurationService to set
	 */
	public void setIntrospectionConfigurationService(IntrospectionConfigurationService introspectionUrlProvider) {
		this.introspectionConfigurationService = introspectionUrlProvider;
	}

	/**
	 * @param introspectionAuthorityGranter the introspectionAuthorityGranter to set
	 */
	public void setIntrospectionAuthorityGranter(IntrospectionAuthorityGranter introspectionAuthorityGranter) {
		this.introspectionAuthorityGranter = introspectionAuthorityGranter;
	}

	/**
	 * @return the introspectionAuthorityGranter
	 */
	public IntrospectionAuthorityGranter getIntrospectionAuthorityGranter() {
		return introspectionAuthorityGranter;
	}

	/**
	 * get the default cache expire time in milliseconds
	 * @return
	 */
	public int getDefaultExpireTime() {
		return defaultExpireTime;
	}

	/**
	 * set the default cache expire time in milliseconds
	 * @param defaultExpireTime
	 */
	public void setDefaultExpireTime(int defaultExpireTime) {
		this.defaultExpireTime = defaultExpireTime;
	}

	/**
	 * check if forcing a cache expire time maximum value
	 * @return the forceCacheExpireTime setting
	 */
	public boolean isForceCacheExpireTime() {
		return forceCacheExpireTime;
	}

	/**
	 * set forcing a cache expire time maximum value
	 * @param forceCacheExpireTime
	 */
	public void setForceCacheExpireTime(boolean forceCacheExpireTime) {
		this.forceCacheExpireTime = forceCacheExpireTime;
	}

	/**
	 * Are non-expiring tokens cached using the default cache time
	 * @return state of cacheNonExpiringTokens
	 */
	public boolean isCacheNonExpiringTokens() {
		return cacheNonExpiringTokens;
	}

	/**
	 * should non-expiring tokens be cached using the default cache timeout
	 * @param cacheNonExpiringTokens
	 */
	public void setCacheNonExpiringTokens(boolean cacheNonExpiringTokens) {
		this.cacheNonExpiringTokens = cacheNonExpiringTokens;
	}

	/**
	 * Is the service caching tokens, or is it hitting the introspection end point every time
	 * @return true is caching tokens locally, false hits the introspection end point every time
	 */
	public boolean isCacheTokens() {
		return cacheTokens;
	}

	/**
	 * Configure if the client should cache tokens locally or not
	 * @param cacheTokens
	 */
	public void setCacheTokens(boolean cacheTokens) {
		this.cacheTokens = cacheTokens;
	}

	/**
	 * Check to see if the introspection end point response for a token has been cached locally
	 * This call will return the token if it has been cached and is still valid according to
	 * the cache expire time on the TokenCacheObject. If a cached value has been found but is
	 * expired, either by default expire times or the token's own expire time, then the token is
	 * removed from the cache and null is returned.
	 * @param key is the token to check
	 * @return the cached TokenCacheObject or null
	 */
	private TokenCacheObject checkCache(String key) {
		if (cacheTokens && authCache.containsKey(key)) {
			TokenCacheObject tco = authCache.get(key);

			if (tco != null && tco.cacheExpire != null && tco.cacheExpire.after(new Date())) {
				return tco;
			} else {
				// if the token is expired, don't keep things around.
				authCache.remove(key);
			}
		}
		return null;
	}

	private OAuth2Request createStoredRequest(final JsonObject token) {
		String clientId = token.get("client_id").getAsString();
		Set<String> scopes = new HashSet<>();
		if (token.has("scope")) {
			scopes.addAll(OAuth2Utils.parseParameterList(token.get("scope").getAsString()));
		}
		Map<String, String> parameters = new HashMap<>();
		parameters.put("client_id", clientId);
		parameters.put("scope", OAuth2Utils.formatParameterList(scopes));
		OAuth2Request storedRequest = new OAuth2Request(parameters, clientId, null, true, scopes, null, null, null, null);
		return storedRequest;
	}

	private Authentication createUserAuthentication(JsonObject token) {
		JsonElement userId = token.get("user_id");
		if(userId == null) {
			return null;
		}

		return new PreAuthenticatedAuthenticationToken(userId.getAsString(), token, introspectionAuthorityGranter.getAuthorities(token));
	}

	private OAuth2AccessToken createAccessToken(final JsonObject token, final String tokenString) {
		OAuth2AccessToken accessToken = new OAuth2AccessTokenImpl(token, tokenString);
		return accessToken;
	}

	/**
	 * Validate a token string against the introspection endpoint,
	 * then parse it and store it in the local cache if caching is enabled.
	 *
	 * @param accessToken Token to pass to the introspection endpoint
	 * @return TokenCacheObject containing authentication and token if the token was valid, otherwise null
	 */
	private TokenCacheObject parseToken(String accessToken) {

		// find out which URL to ask
		String introspectionUrl;
		RegisteredClient client;
		try {
			introspectionUrl = introspectionConfigurationService.getIntrospectionUrl(accessToken);
			client = introspectionConfigurationService.getClientConfiguration(accessToken);
		} catch (IllegalArgumentException e) {
			logger.error("Unable to load introspection URL or client configuration", e);
			return null;
		}
		// Use the SpringFramework RestTemplate to send the request to the
		// endpoint
		String validatedToken = null;

		RestTemplate restTemplate;
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

		final String clientId = client.getClientId();
		final String clientSecret = client.getClientSecret();

		if (SECRET_BASIC.equals(client.getTokenEndpointAuthMethod())){
			// use BASIC auth if configured to do so
			restTemplate = new RestTemplate(factory) {

				@Override
				protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
					ClientHttpRequest httpRequest = super.createRequest(url, method);
					httpRequest.getHeaders().add("Authorization",
							String.format("Basic %s", Base64.encode(String.format("%s:%s", clientId, clientSecret)) ));
					return httpRequest;
				}
			};
		} else {  //Alternatively use form based auth
			restTemplate = new RestTemplate(factory);

			form.add("client_id", clientId);
			form.add("client_secret", clientSecret);
		}

		form.add("token", accessToken);

		try {
			validatedToken = restTemplate.postForObject(introspectionUrl, form, String.class);
		} catch (RestClientException rce) {
			logger.error("validateToken", rce);
			return null;
		}
		if (validatedToken != null) {
			// parse the json
			JsonElement jsonRoot = new JsonParser().parse(validatedToken);
			if (!jsonRoot.isJsonObject()) {
				return null; // didn't get a proper JSON object
			}

			JsonObject tokenResponse = jsonRoot.getAsJsonObject();

			if (tokenResponse.get("error") != null) {
				// report an error?
				logger.error("Got an error back: " + tokenResponse.get("error") + ", " + tokenResponse.get("error_description"));
				return null;
			}

			if (!tokenResponse.get("active").getAsBoolean()) {
				// non-valid token
				logger.info("Server returned non-active token");
				return null;
			}
			// create an OAuth2Authentication
			OAuth2Authentication auth = new OAuth2Authentication(createStoredRequest(tokenResponse), createUserAuthentication(tokenResponse));
			// create an OAuth2AccessToken
			OAuth2AccessToken token = createAccessToken(tokenResponse, accessToken);

			if (token.getExpiration() == null || token.getExpiration().after(new Date())) {
				// Store them in the cache
				TokenCacheObject tco = new TokenCacheObject(token, auth);
				if (cacheTokens && (cacheNonExpiringTokens || token.getExpiration() != null)) {
					authCache.put(accessToken, tco);
				}
				return tco;
			}
		}

		// when the token is invalid for whatever reason
		return null;
	}

	@Override
	public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException {
		// First check if the in memory cache has an Authentication object, and
		// that it is still valid
		// If Valid, return it
		TokenCacheObject cacheAuth = checkCache(accessToken);
		if (cacheAuth != null) {
			return cacheAuth.auth;
		} else {
			cacheAuth = parseToken(accessToken);
			if (cacheAuth != null) {
				return cacheAuth.auth;
			} else {
				return null;
			}
		}
	}

	@Override
	public OAuth2AccessToken readAccessToken(String accessToken) {
		// First check if the in memory cache has a Token object, and that it is
		// still valid
		// If Valid, return it
		TokenCacheObject cacheAuth = checkCache(accessToken);
		if (cacheAuth != null) {
			return cacheAuth.token;
		} else {
			cacheAuth = parseToken(accessToken);
			if (cacheAuth != null) {
				return cacheAuth.token;
			} else {
				return null;
			}
		}
	}

}
