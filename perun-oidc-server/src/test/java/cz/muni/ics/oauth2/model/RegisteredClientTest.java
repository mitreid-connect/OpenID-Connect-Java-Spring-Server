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
package cz.muni.ics.oauth2.model;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import java.sql.Date;
import org.junit.Test;

/**
 * @author jricher
 *
 */
public class RegisteredClientTest {

	/**
	 * Test method for {@link RegisteredClient#RegisteredClient()}.
	 */
	@Test
	public void testRegisteredClient() {

		// make sure all the pass-through getters and setters work

		RegisteredClient c = new RegisteredClient();

		c.setClientId("s6BhdRkqt3");
		c.setClientSecret("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk");
		c.setClientSecretExpiresAt(new Date(1577858400L * 1000L));
		c.setRegistrationAccessToken("this.is.an.access.token.value.ffx83");
		c.setRegistrationClientUri("https://server.example.com/connect/register?client_id=s6BhdRkqt3");
		c.setApplicationType(ClientDetailsEntity.AppType.WEB);
		c.setRedirectUris(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"));
		c.setClientName("My Example");
		c.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		c.setSectorIdentifierUri("https://other.example.net/file_of_redirect_uris.json");
		c.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
		c.setJwksUri("https://client.example.org/my_public_keys.jwks");
		c.setUserInfoEncryptedResponseAlg(JWEAlgorithm.RSA1_5);
		c.setUserInfoEncryptedResponseEnc(EncryptionMethod.A128CBC_HS256);
		c.setContacts(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"));
		c.setRequestUris(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"));

		assertEquals("s6BhdRkqt3", c.getClientId());
		assertEquals("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk", c.getClientSecret());
		assertEquals(new Date(1577858400L * 1000L), c.getClientSecretExpiresAt());
		assertEquals("this.is.an.access.token.value.ffx83", c.getRegistrationAccessToken());
		assertEquals("https://server.example.com/connect/register?client_id=s6BhdRkqt3", c.getRegistrationClientUri());
		assertEquals(ClientDetailsEntity.AppType.WEB, c.getApplicationType());
		assertEquals(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"), c.getRedirectUris());
		assertEquals("My Example", c.getClientName());
		assertEquals(ClientDetailsEntity.SubjectType.PAIRWISE, c.getSubjectType());
		assertEquals("https://other.example.net/file_of_redirect_uris.json", c.getSectorIdentifierUri());
		assertEquals(ClientDetailsEntity.AuthMethod.SECRET_BASIC, c.getTokenEndpointAuthMethod());
		assertEquals("https://client.example.org/my_public_keys.jwks", c.getJwksUri());
		assertEquals(JWEAlgorithm.RSA1_5, c.getUserInfoEncryptedResponseAlg());
		assertEquals(EncryptionMethod.A128CBC_HS256, c.getUserInfoEncryptedResponseEnc());
		assertEquals(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"), c.getContacts());
		assertEquals(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"), c.getRequestUris());
	}

	/**
	 * Test method for {@link RegisteredClient#RegisteredClient(ClientDetailsEntity)}.
	 */
	@Test
	public void testRegisteredClientClientDetailsEntity() {
		ClientDetailsEntity c = new ClientDetailsEntity();

		c.setClientId("s6BhdRkqt3");
		c.setClientSecret("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk");
		c.setApplicationType(ClientDetailsEntity.AppType.WEB);
		c.setRedirectUris(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"));
		c.setClientName("My Example");
		
		c.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		c.setSectorIdentifierUri("https://other.example.net/file_of_redirect_uris.json");
		c.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
		c.setJwksUri("https://client.example.org/my_public_keys.jwks");
		c.setUserInfoEncryptedResponseAlg(JWEAlgorithm.RSA1_5);
		c.setUserInfoEncryptedResponseEnc(EncryptionMethod.A128CBC_HS256);
		c.setContacts(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"));
		c.setRequestUris(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"));

		// Create a RegisteredClient based on a ClientDetailsEntity object and set several properties
		RegisteredClient rc = new RegisteredClient(c);
		rc.setClientSecretExpiresAt(new Date(1577858400L * 1000L));
		rc.setRegistrationAccessToken("this.is.an.access.token.value.ffx83");
		rc.setRegistrationClientUri("https://server.example.com/connect/register?client_id=s6BhdRkqt3");

		// make sure all the pass-throughs work
		assertEquals("s6BhdRkqt3", rc.getClientId());
		assertEquals("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk", rc.getClientSecret());
		assertEquals(new Date(1577858400L * 1000L), rc.getClientSecretExpiresAt());
		assertEquals("this.is.an.access.token.value.ffx83", rc.getRegistrationAccessToken());
		assertEquals("https://server.example.com/connect/register?client_id=s6BhdRkqt3", rc.getRegistrationClientUri());
		assertEquals(ClientDetailsEntity.AppType.WEB, rc.getApplicationType());
		assertEquals(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"), rc.getRedirectUris());
		assertEquals("My Example", rc.getClientName());
		assertEquals(ClientDetailsEntity.SubjectType.PAIRWISE, rc.getSubjectType());
		assertEquals("https://other.example.net/file_of_redirect_uris.json", rc.getSectorIdentifierUri());
		assertEquals(ClientDetailsEntity.AuthMethod.SECRET_BASIC, rc.getTokenEndpointAuthMethod());
		assertEquals("https://client.example.org/my_public_keys.jwks", rc.getJwksUri());
		assertEquals(JWEAlgorithm.RSA1_5, rc.getUserInfoEncryptedResponseAlg());
		assertEquals(EncryptionMethod.A128CBC_HS256, rc.getUserInfoEncryptedResponseEnc());
		assertEquals(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"), rc.getContacts());
		assertEquals(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"), rc.getRequestUris());
	}

	/**
	 * Test method for {@link RegisteredClient#RegisteredClient(ClientDetailsEntity, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testRegisteredClientClientDetailsEntityStringString() {
		ClientDetailsEntity c = new ClientDetailsEntity();

		c.setClientId("s6BhdRkqt3");
		c.setClientSecret("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk");
		c.setApplicationType(ClientDetailsEntity.AppType.WEB);
		c.setRedirectUris(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"));
		c.setClientName("My Example");
		
		c.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		c.setSectorIdentifierUri("https://other.example.net/file_of_redirect_uris.json");
		c.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
		c.setJwksUri("https://client.example.org/my_public_keys.jwks");
		c.setUserInfoEncryptedResponseAlg(JWEAlgorithm.RSA1_5);
		c.setUserInfoEncryptedResponseEnc(EncryptionMethod.A128CBC_HS256);
		c.setContacts(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"));
		c.setRequestUris(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"));

		// Create a RegisteredClient based on a ClientDetails, a token, and a server URI
		RegisteredClient rc = new RegisteredClient(c, "this.is.an.access.token.value.ffx83", "https://server.example.com/connect/register?client_id=s6BhdRkqt3");

		// make sure all the pass-throughs work
		assertEquals("s6BhdRkqt3", rc.getClientId());
		assertEquals("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk", rc.getClientSecret());
		assertEquals("this.is.an.access.token.value.ffx83", rc.getRegistrationAccessToken());
		assertEquals("https://server.example.com/connect/register?client_id=s6BhdRkqt3", rc.getRegistrationClientUri());
		assertEquals(ClientDetailsEntity.AppType.WEB, rc.getApplicationType());
		assertEquals(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"), rc.getRedirectUris());
		assertEquals("My Example", rc.getClientName());
		assertEquals(ClientDetailsEntity.SubjectType.PAIRWISE, rc.getSubjectType());
		assertEquals("https://other.example.net/file_of_redirect_uris.json", rc.getSectorIdentifierUri());
		assertEquals(ClientDetailsEntity.AuthMethod.SECRET_BASIC, rc.getTokenEndpointAuthMethod());
		assertEquals("https://client.example.org/my_public_keys.jwks", rc.getJwksUri());
		assertEquals(JWEAlgorithm.RSA1_5, rc.getUserInfoEncryptedResponseAlg());
		assertEquals(EncryptionMethod.A128CBC_HS256, rc.getUserInfoEncryptedResponseEnc());
		assertEquals(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"), rc.getContacts());
		assertEquals(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"), rc.getRequestUris());
	}

}
