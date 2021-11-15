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
package cz.muni.ics.openid.connect.util;


import static org.junit.Assert.assertEquals;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTParser;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import java.text.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestIdTokenHashUtils {

	@Mock
    OAuth2AccessTokenEntity mockToken256;
	@Mock
	OAuth2AccessTokenEntity mockToken384;
	@Mock
	OAuth2AccessTokenEntity mockToken512;

	@Before
	public void prepare() throws ParseException {

		/*
		Claims for first token:

		claims.setType("JWT");
		claims.setIssuer("www.example.com");
		claims.setSubject("example_user");
		claims.setClaim("alg", "HS256");
		 */
		Mockito.when(mockToken256.getJwt()).thenReturn(JWTParser.parse("eyJhbGciOiJub25lIn0.eyJhbGciOiJIUzI1NiIsInN1YiI6ImV4YW1wbGVfdXNlciIsImlzcyI6Ind3dy5leGFtcGxlLmNvbSIsInR5cCI6IkpXVCJ9."));

		/*
		 * Claims for second token
		claims = new JWTClaimsSet();
		claims.setType("JWT");
		claims.setIssuer("www.another-example.net");
		claims.setSubject("another_user");
		claims.setClaim("alg", "ES384");
		 */
		Mockito.when(mockToken384.getJwt()).thenReturn(JWTParser.parse("eyJhbGciOiJub25lIn0.eyJhbGciOiJFUzM4NCIsInN1YiI6ImFub3RoZXJfdXNlciIsImlzcyI6Ind3dy5hbm90aGVyLWV4YW1wbGUubmV0IiwidHlwIjoiSldUIn0."));

		/*
		 * Claims for third token:
		claims = new JWTClaimsSet();
		claims.setType("JWT");
		claims.setIssuer("www.different.com");
		claims.setSubject("different_user");
		claims.setClaim("alg", "RS512");
		 */
		Mockito.when(mockToken512.getJwt()).thenReturn(JWTParser.parse("eyJhbGciOiJub25lIn0.eyJhbGciOiJSUzUxMiIsInN1YiI6ImRpZmZlcmVudF91c2VyIiwiaXNzIjoid3d3LmRpZmZlcmVudC5jb20iLCJ0eXAiOiJKV1QifQ."));
	}

	@Test
	public void getAccessTokenHash256() {

		mockToken256.getJwt().serialize();
		Base64URL expectedHash = new Base64URL("EP1gXNeESRH-n57baopfTQ");

		Base64URL resultHash = IdTokenHashUtils.getAccessTokenHash(JWSAlgorithm.HS256, mockToken256);

		assertEquals(expectedHash, resultHash);
	}

	@Test
	public void getAccessTokenHash384() {

		/*
		 * independently generate hash
		 ascii of token = eyJhbGciOiJub25lIn0.eyJhbGciOiJFUzM4NCIsInN1YiI6ImFub3RoZXJfdXNlciIsImlzcyI6Ind3dy5hbm90aGVyLWV4YW1wbGUubmV0IiwidHlwIjoiSldUIn0.
		 base64url of hash = BWfFK73PQI36M1rg9R6VjMyWOE0-XvBK
		 */

		mockToken384.getJwt().serialize();
		Base64URL expectedHash = new Base64URL("BWfFK73PQI36M1rg9R6VjMyWOE0-XvBK");

		Base64URL resultHash = IdTokenHashUtils.getAccessTokenHash(JWSAlgorithm.ES384, mockToken384);

		assertEquals(expectedHash, resultHash);
	}

	@Test
	public void getAccessTokenHash512() {

		/*
		 * independently generate hash
		 ascii of token = eyJhbGciOiJub25lIn0.eyJhbGciOiJSUzUxMiIsInN1YiI6ImRpZmZlcmVudF91c2VyIiwiaXNzIjoid3d3LmRpZmZlcmVudC5jb20iLCJ0eXAiOiJKV1QifQ.
		 base64url of hash = vGH3QMY-knpACkLgzdkTqu3C9jtvbf2Wk_RSu2vAx8k
		 */

		mockToken512.getJwt().serialize();
		Base64URL expectedHash = new Base64URL("vGH3QMY-knpACkLgzdkTqu3C9jtvbf2Wk_RSu2vAx8k");

		Base64URL resultHash = IdTokenHashUtils.getAccessTokenHash(JWSAlgorithm.RS512, mockToken512);

		assertEquals(expectedHash, resultHash);
	}

	@Test
	public void getCodeHash512() {

		String testCode = "b0x0rZ";

		Base64URL expectedHash = new Base64URL("R5DCRi5eOjlvyTAJfry2dNM9adJ2ElpDEKYYByYU920"); // independently generated

		Base64URL resultHash = IdTokenHashUtils.getCodeHash(JWSAlgorithm.ES512, testCode);

		assertEquals(expectedHash, resultHash);
	}
}
