/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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
package org.mitre.openid.connect.client.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mitre.jwt.signer.service.impl.DefaultJWTSigningAndValidationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	private Map<String, String> options = ImmutableMap.of("foo", "bar");


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
	private String loginHint = "bob";

	private DefaultJWTSigningAndValidationService signingAndValidationService;

	private SignedAuthRequestUrlBuilder urlBuilder = new SignedAuthRequestUrlBuilder();

	@Before
	public void prepare() throws NoSuchAlgorithmException, InvalidKeySpecException {

		RSAKey key = new RSAKey(new Base64URL(n), new Base64URL(e), new Base64URL(d), KeyUse.SIGNATURE, null, new Algorithm(alg), kid, null, null, null, null);
		Map<String, JWK> keys = Maps.newHashMap();
		keys.put("client", key);

		signingAndValidationService = new DefaultJWTSigningAndValidationService(keys);
		signingAndValidationService.setDefaultSignerKeyId("client");
		signingAndValidationService.setDefaultSigningAlgorithmName(alg);

		urlBuilder.setSigningAndValidationService(signingAndValidationService);

		serverConfig = Mockito.mock(ServerConfiguration.class);
		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("https://server.example.com/authorize");

		clientConfig = Mockito.mock(RegisteredClient.class);
		Mockito.when(clientConfig.getClientId()).thenReturn("s6BhdRkqt3");
		Mockito.when(clientConfig.getScope()).thenReturn(Sets.newHashSet("openid", "profile"));
	}

	/**
	 * This test takes the URI from the result of building a signed request
	 * and checks that the JWS object parsed from the request URI matches up
	 * with the expected claim values.
	 */
	@Test
	public void buildAuthRequestUrl() {

		String requestUri = urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, redirectUri, nonce, state, options, null);

		// parsing the result
		UriComponentsBuilder builder = null;

		try {
			builder = UriComponentsBuilder.fromUri(new URI(requestUri));
		} catch (URISyntaxException e1) {
			fail("URISyntaxException was thrown.");
		}

		UriComponents components = builder.build();
		String jwtString = components.getQueryParams().get("request").get(0);
		JWTClaimsSet claims = null;

		try {
			SignedJWT jwt = SignedJWT.parse(jwtString);
			claims = jwt.getJWTClaimsSet();
		} catch (ParseException e) {
			fail("ParseException was thrown.");
		}

		assertEquals(responseType, claims.getClaim("response_type"));
		assertEquals(clientConfig.getClientId(), claims.getClaim("client_id"));

		List<String> scopeList = Arrays.asList(((String) claims.getClaim("scope")).split(" "));
		assertTrue(scopeList.containsAll(clientConfig.getScope()));

		assertEquals(redirectUri, claims.getClaim("redirect_uri"));
		assertEquals(nonce, claims.getClaim("nonce"));
		assertEquals(state, claims.getClaim("state"));
		for (String claim : options.keySet()) {
			assertEquals(options.get(claim), claims.getClaim(claim));
		}
	}

	@Test
	public void buildAuthRequestUrl_withLoginHint() {

		String requestUri = urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, redirectUri, nonce, state, options, loginHint);

		// parsing the result
		UriComponentsBuilder builder = null;

		try {
			builder = UriComponentsBuilder.fromUri(new URI(requestUri));
		} catch (URISyntaxException e1) {
			fail("URISyntaxException was thrown.");
		}

		UriComponents components = builder.build();
		String jwtString = components.getQueryParams().get("request").get(0);
		JWTClaimsSet claims = null;

		try {
			SignedJWT jwt = SignedJWT.parse(jwtString);
			claims = jwt.getJWTClaimsSet();
		} catch (ParseException e) {
			fail("ParseException was thrown.");
		}

		assertEquals(responseType, claims.getClaim("response_type"));
		assertEquals(clientConfig.getClientId(), claims.getClaim("client_id"));

		List<String> scopeList = Arrays.asList(((String) claims.getClaim("scope")).split(" "));
		assertTrue(scopeList.containsAll(clientConfig.getScope()));

		assertEquals(redirectUri, claims.getClaim("redirect_uri"));
		assertEquals(nonce, claims.getClaim("nonce"));
		assertEquals(state, claims.getClaim("state"));
		for (String claim : options.keySet()) {
			assertEquals(options.get(claim), claims.getClaim(claim));
		}
		assertEquals(loginHint, claims.getClaim("login_hint"));
	}

	@Test(expected = AuthenticationServiceException.class)
	public void buildAuthRequestUrl_badUri() {

		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("e=mc^2");

		urlBuilder.buildAuthRequestUrl(serverConfig, clientConfig, "example.com", "", "", options, null);
	}
}
