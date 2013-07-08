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
package org.mitre.openid.connect.client.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Map;

import net.minidev.json.JSONObject;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mitre.jwt.signer.service.impl.DefaultJwtSigningAndValidationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.Use;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author wkim
 * 
 */
public class TestSignedAuthRequestUrlBuilder {

	// Test fixture:
	private ServerConfiguration serverConfig;
	private RegisteredClient clientConfig;

	private String redirectUri = "https://client.example.org/";
	private String nonce = "34fasf3ds";
	private String state = "af0ifjsldkj";
	private String responseType = "code";

	// RSA key properties:
	// {@link package com.nimbusds.jose.jwk#RSAKey}
	private String n = "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zw" + 
					"u1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc" + 
					"5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8K" + 
					"JZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh" + 
					"6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw";
	private String e = "AQAB";
	private String d = "X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9M7dx5oo7GURknc" +
					"hnrRweUkC7hT5fJLM0WbFAKNLWY2vv7B6NqXSzUvxT0_YSfqijwp3RTzlBaCxWp4doFk5" +
					"N2o8Gy_nHNKroADIkJ46pRUohsXywbReAdYaMwFs9tv8d_cPVY3i07a3t8MN6TNwm0dSa" +
					"wm9v47UiCl3Sk5ZiG7xojPLu4sbg1U2jx4IBTNBznbJSzFHK66jT8bgkuqsk0GjskDJk1" +
					"9Z4qwjwbsnn4j2WBii3RL-Us2lGVkY8fkFzme1z0HbIkfz0Y6mqnOYtqc0X4jfcKoAC8Q";
	private String alg = "RS256";
	private String kid = "2011-04-29";

	private DefaultJwtSigningAndValidationService signingAndValidationService;

	private SignedAuthRequestUrlBuilder urlBuilder = new SignedAuthRequestUrlBuilder();

	@Before
	public void prepare() throws NoSuchAlgorithmException, InvalidKeySpecException {

		RSAKey key = new RSAKey(new Base64URL(n), new Base64URL(e), new Base64URL(d), Use.SIGNATURE, new Algorithm(alg), kid);
		Map<String, JWK> keys = Maps.newHashMap();
		keys.put("client", key);

		signingAndValidationService = new DefaultJwtSigningAndValidationService(keys);
		signingAndValidationService.setDefaultSignerKeyId("client");
		signingAndValidationService.setDefaultSigningAlgorithmName(alg);

		urlBuilder.setSigningAndValidationService(signingAndValidationService);

		serverConfig = Mockito.mock(ServerConfiguration.class);
		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("https://server.example.com/authorize");

		clientConfig = Mockito.mock(RegisteredClient.class);
		Mockito.when(clientConfig.getClientId()).thenReturn("s6BhdRkqt3");
		Mockito.when(clientConfig.getScope()).thenReturn(Sets.newHashSet("openid", "profile"));
	}

	@Test
	public void buildAuthRequestUrl() {

		JWTClaimsSet claims = new JWTClaimsSet();

		//set parameters to JwtClaims
		claims.setCustomClaim("response_type", responseType);
		claims.setCustomClaim("client_id", clientConfig.getClientId());
		claims.setCustomClaim("scope", Joiner.on(" ").join(clientConfig.getScope()));

		// build our redirect URI
		claims.setCustomClaim("redirect_uri", redirectUri);

		// this comes back in the id token
		claims.setCustomClaim("nonce", nonce);

		// this comes back in the auth request return
		claims.setCustomClaim("state", state);

		SignedJWT jwt = new SignedJWT(new JWSHeader(signingAndValidationService.getDefaultSigningAlgorithm()), claims);

		signingAndValidationService.signJwt(jwt);

		String expected = null;

		try {

			URIBuilder uriBuilder = new URIBuilder(serverConfig.getAuthorizationEndpointUri());
			uriBuilder.addParameter("request", jwt.serialize());

			expected = uriBuilder.build().toString();

		} catch (URISyntaxException e) {
			fail("URISyntaxException occurred.");
		}

		String actual = urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, redirectUri, nonce, state);

		assertEquals(expected, actual);
	}

	@Test(expected = AuthenticationServiceException.class)
	public void buildAuthRequestUrl_badUri() {

		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("e=mc^2");

		urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "example.com", "", "");
	}

	/**
	 * This test takes the URI from the result of building a signed request, 
	 * and attempts to parse the JWS object to make sure that the content of the
	 * JWS object match up with the expected claim values.
	 * 
	 * @throws URISyntaxException
	 * @throws ParseException 
	 */
	@Test
	public void verifyJwt() throws URISyntaxException, ParseException {

		// The URI below was taken from the results of the buildAuthRequestUrl() unit test..
		URI uri = new URI("https://server.example.com/authorize?request=" + 
						"eyJhbGciOiJSUzI1NiJ9." + 
						"eyJyZXNwb25zZV90eXBlIjoiY29kZSIsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUiLCJyZWRpcmVjdF91cmkiOiJodHRwczpcL1wvY2xpZW50LmV4YW1wbGUub3JnXC8iLCJub25jZSI6IjM0ZmFzZjNkcyIsInN0YXRlIjoiYWYwaWZqc2xka2oiLCJjbGllbnRfaWQiOiJzNkJoZFJrcXQzIn0." + 
						"m_1UVCTlr_3ksYmZzN5WUAhbr2E3x0RTWq8ZO7SZwNtIu_kGI29BeHUDaGM3A40A-IX9dMsNQlkr-88g6BdHU2Nd5LJCe4FCrvEo7xSQiGbEAKeFn_q_paVL2P_GJgVrwc7cKAECQzc8iJylXm_ZZgyMEU2YtR-CMXHM3pkY1hCYy6kkmteAMwvEYIz3JtLQ6P4QhMCRVYl_AY9LlwS1gmNpoCcwhnQRjxOk8SKIhMFgFkauyy97H9bM0bcy619awigdnP4ZFaEK1O7SQ3-3M_qDZ3BHGM3fWKy6ql5HZPKm2e8SqdShmnA0JKmEZegRBqms_Fpk9X81Tln7Bi883w");

		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
		UriComponents components = builder.build();
		
		String jwtString = components.getQueryParams().get("request").get(0);
		JWSObject jws = JWSObject.parse(jwtString);
		JSONObject json = JSONObjectUtils.parseJSONObject(jws.getPayload().toString());
		
		assertEquals(redirectUri, json.get("redirect_uri"));
		assertEquals(clientConfig.getClientId(), json.get("client_id"));
		assertEquals(responseType, json.get("response_type"));
		assertEquals(Joiner.on(" ").join(clientConfig.getScope()), json.get("scope"));
		assertEquals(nonce, json.get("nonce"));
		assertEquals(state, json.get("state"));
		
	}
}
