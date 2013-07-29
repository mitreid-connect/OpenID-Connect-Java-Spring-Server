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
package org.mitre.oauth2.introspectingfilter;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
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

/**
 * This ResourceServerTokenServices implementation introspects incoming tokens at a
 * server's introspection endpoint URL and passes an Authentication object along
 * based on the response from the introspection endpoint.
 * @author jricher
 *
 */
public class IntrospectingTokenService implements ResourceServerTokenServices {

	private String clientId;
	private String clientSecret;
	private IntrospectionUrlProvider introspectionUrlProvider;
	private IntrospectionAuthorityGranter introspectionAuthorityGranter = new SimpleIntrospectionAuthorityGranter();

	// Inner class to store in the hash map
	private class TokenCacheObject {
		OAuth2AccessToken token;
		OAuth2Authentication auth;

		private TokenCacheObject(OAuth2AccessToken token, OAuth2Authentication auth) {
			this.token = token;
			this.auth = auth;
		}
	}

	private Map<String, TokenCacheObject> authCache = new HashMap<String, TokenCacheObject>();

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * @return the introspectionUrlProvider
	 */
	public IntrospectionUrlProvider getIntrospectionUrlProvider() {
		return introspectionUrlProvider;
	}

	/**
	 * @param introspectionUrlProvider the introspectionUrlProvider to set
	 */
	public void setIntrospectionUrlProvider(IntrospectionUrlProvider introspectionUrlProvider) {
		this.introspectionUrlProvider = introspectionUrlProvider;
	}

	// Check if there is a token and authentication in the cache
	// and check if it is not expired.
	private TokenCacheObject checkCache(String key) {
		if (authCache.containsKey(key)) {
			TokenCacheObject tco = authCache.get(key);
			if (tco.token.getExpiration().after(new Date())) {
				return tco;
			} else {
				// if the token is expired, don't keep things around.
				authCache.remove(key);
			}
		}
		return null;
	}

	private OAuth2Request createStoredRequest(final JsonObject token) {
		clientId = token.get("client_id").getAsString();
		Set<String> scopes = new HashSet<String>();
		for (JsonElement e : token.get("scope").getAsJsonArray()) {
			scopes.add(e.getAsString());
		}
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("client_id", clientId);
		parameters.put("scope", OAuth2Utils.formatParameterList(scopes));
		OAuth2Request storedRequest = new OAuth2Request(parameters, clientId, null, true, scopes, null, null, null);
		return storedRequest;
	}

	private Authentication createAuthentication(JsonObject token) {
		return new PreAuthenticatedAuthenticationToken(token.get("sub").getAsString(), token, introspectionAuthorityGranter.getAuthorities(token));
	}

	private OAuth2AccessToken createAccessToken(final JsonObject token, final String tokenString) {
		OAuth2AccessToken accessToken = new OAuth2AccessTokenImpl(token, tokenString);
		return accessToken;
	}

	// Validate a token string against the introspection endpoint,
	// then parse it and store it in the local cache. Return true on
	// sucess, false otherwise.
	private boolean parseToken(String accessToken) {

		// find out which URL to ask
		String introspectionUrl = introspectionUrlProvider.getIntrospectionUrl(accessToken);

		// Use the SpringFramework RestTemplate to send the request to the
		// endpoint
		String validatedToken = null;
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("token", accessToken);
		form.add("client_id", this.clientId);
		form.add("client_secret", this.clientSecret);

		try {
			validatedToken = restTemplate.postForObject(introspectionUrl, form, String.class);
		} catch (RestClientException rce) {
			// TODO: LOG THIS!?
			LoggerFactory.getLogger(IntrospectingTokenService.class).error("validateToken", rce);
		}
		if (validatedToken != null) {
			// parse the json
			JsonElement jsonRoot = new JsonParser().parse(validatedToken);
			if (!jsonRoot.isJsonObject()) {
				return false; // didn't get a proper JSON object
			}

			JsonObject tokenResponse = jsonRoot.getAsJsonObject();

			if (tokenResponse.get("error") != null) {
				// report an error?
				return false;
			}

			if (!tokenResponse.get("active").getAsBoolean()) {
				// non-valid token
				return false;
			}
			// create an OAuth2Authentication
			OAuth2Authentication auth = new OAuth2Authentication(createStoredRequest(tokenResponse), createAuthentication(tokenResponse));
			// create an OAuth2AccessToken
			OAuth2AccessToken token = createAccessToken(tokenResponse, accessToken);

			if (token.getExpiration().after(new Date())) {
				// Store them in the cache
				authCache.put(accessToken, new TokenCacheObject(token, auth));

				return true;
			}
		}

		// If we never put a token and an authentication in the cache...
		return false;
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
			if (parseToken(accessToken)) {
				cacheAuth = authCache.get(accessToken);
				if (cacheAuth != null && (cacheAuth.token.getExpiration().after(new Date()))) {
					return cacheAuth.auth;
				} else {
					return null;
				}
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
			if (parseToken(accessToken)) {
				cacheAuth = authCache.get(accessToken);
				if (cacheAuth != null && (cacheAuth.token.getExpiration().after(new Date()))) {
					return cacheAuth.token;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

}
