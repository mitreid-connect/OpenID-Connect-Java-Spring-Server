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
/**
 *
 */
package cz.muni.ics.openid.connect.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author jricher
 *
 */
public class ServerConfigurationTest {

	/**
	 * Test getters and setters for server configuration bean
	 */
	@Test
	public void testServerConfiguration() {
		String authorizationEndpointUri = "http://localhost:8080/openid-connect-server/authorize";
		String tokenEndpointUri = "http://localhost:8080/openid-connect-server/token";
		String registrationEndpointUri = "http://localhost:8080/openid-connect-server/register";
		String issuer = "http://localhost:8080/openid-connect-server/";
		String jwksUri = "http://localhost:8080/openid-connect-server/jwk";
		String userInfoUri = "http://localhost:8080/openid-connect-server/userinfo";

		ServerConfiguration sc = new ServerConfiguration();
		sc.setAuthorizationEndpointUri(authorizationEndpointUri);
		sc.setTokenEndpointUri(tokenEndpointUri);
		sc.setRegistrationEndpointUri(registrationEndpointUri);
		sc.setIssuer(issuer);
		sc.setJwksUri(jwksUri);
		sc.setUserInfoUri(userInfoUri);

		assertEquals(authorizationEndpointUri, sc.getAuthorizationEndpointUri());
		assertEquals(tokenEndpointUri, sc.getTokenEndpointUri());
		assertEquals(registrationEndpointUri, sc.getRegistrationEndpointUri());
		assertEquals(issuer, sc.getIssuer());
		assertEquals(jwksUri, sc.getJwksUri());
		assertEquals(userInfoUri, sc.getUserInfoUri());

	}


	/**
	 * Test method for {@link ServerConfiguration#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		String authorizationEndpointUri = "http://localhost:8080/openid-connect-server/authorize";
		String tokenEndpointUri = "http://localhost:8080/openid-connect-server/token";
		String registrationEndpointUri = "http://localhost:8080/openid-connect-server/register";
		String issuer = "http://localhost:8080/openid-connect-server/";
		String jwksUri = "http://localhost:8080/openid-connect-server/jwk";
		String userInfoUri = "http://localhost:8080/openid-connect-server/userinfo";

		ServerConfiguration sc1 = new ServerConfiguration();
		sc1.setAuthorizationEndpointUri(authorizationEndpointUri);
		sc1.setTokenEndpointUri(tokenEndpointUri);
		sc1.setRegistrationEndpointUri(registrationEndpointUri);
		sc1.setIssuer(issuer);
		sc1.setJwksUri(jwksUri);
		sc1.setUserInfoUri(userInfoUri);

		ServerConfiguration sc2 = new ServerConfiguration();
		sc2.setAuthorizationEndpointUri(authorizationEndpointUri);
		sc2.setTokenEndpointUri(tokenEndpointUri);
		sc2.setRegistrationEndpointUri(registrationEndpointUri);
		sc2.setIssuer(issuer);
		sc2.setJwksUri(jwksUri);
		sc2.setUserInfoUri(userInfoUri);

		assertTrue(sc1.equals(sc2));

	}

}
