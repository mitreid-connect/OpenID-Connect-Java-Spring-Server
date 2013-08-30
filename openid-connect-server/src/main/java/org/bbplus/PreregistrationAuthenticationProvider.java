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
package org.bbplus;

import java.text.ParseException;

import net.minidev.json.JSONObject;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetSigningAndValidationServiceCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author Josh Mandel
 *
 */
@Service
public class PreregistrationAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(PreregistrationAuthenticationProvider.class);

	@Autowired
	private JWKSetSigningAndValidationServiceCacheService validators;
	
	private HttpClient httpClient = new DefaultHttpClient();
	private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
	private RestTemplate restTemplate = new RestTemplate(httpFactory);

	
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {

		PreregistrationToken p = (PreregistrationToken) authentication;
	
		String raw = ((OAuth2AuthenticationDetails) p.getDetails()).getTokenValue();
		
		logger.debug("auth providr making its auth'd tok on raw: " + raw);
		
		SignedJWT parsed;
		
		try {
			parsed = (SignedJWT) JWTParser.parse(raw);
		} catch (ParseException e) {
			throw new AuthenticationServiceException("Invalid JWT format");
		}

		JSONObject payload = parsed.getPayload().toJSONObject();
		String appUrl = payload.get("sub").toString();
		String registryUrl = payload.get("iss").toString();
		
		String registryRaw = restTemplate.getForObject(registryUrl+"/.well-known/bb/registry.json", String.class);
		JsonObject registry = (JsonObject) new JsonParser().parse(registryRaw);
		String jwksUrl = registry.get("jwks_uri").getAsString();
		
		JwtSigningAndValidationService validator = validators.get(jwksUrl);
		if (validator == null || !validator.validateSignature(parsed)) {
			throw new AuthenticationServiceException("Invalid signature");
		}
		
		String appsUrl = registryUrl+"/.well-known/bb/apps.json";
		String appsRaw = restTemplate.getForObject(appsUrl, String.class);
		JsonArray apps = (JsonArray) new JsonParser().parse(appsRaw);

		JsonObject client = null;
		for (JsonElement app : apps) {
			if (((JsonObject) app).get("url").getAsString().equals(appUrl)){
				client = (JsonObject) app;
				break;
			}
		}
		
		if (client == null){
			throw new AuthenticationServiceException("App "+appUrl+" not found at " + appsUrl);
		}
		
		client.addProperty("augmented", true);
		
		logger.debug(client.toString());
		PreregistrationToken ret = new PreregistrationToken(appUrl,	client, true);
		ret.setJwt(parsed);
		
		return ret;
	}


	/**
	 * We support {@link PreregistrationToken}s only.
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return (PreregistrationToken.class.isAssignableFrom(authentication));
	}

}
