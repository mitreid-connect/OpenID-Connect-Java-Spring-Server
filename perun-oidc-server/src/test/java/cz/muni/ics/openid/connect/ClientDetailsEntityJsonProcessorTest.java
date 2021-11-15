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
package cz.muni.ics.openid.connect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.RegisteredClient;
import java.sql.Date;
import org.junit.Test;

/**
 * @author jricher
 *
 */
public class ClientDetailsEntityJsonProcessorTest {

	/**
	 * Test method for {@link ClientDetailsEntityJsonProcessor#parse(java.lang.String)}.
	 */
	@Test
	public void testParse() {
		String json = "  {\n" +
				"   \"application_type\": \"web\",\n" +
				"   \"redirect_uris\":\n" +
				"     [\"https://client.example.org/callback\",\n" +
				"      \"https://client.example.org/callback2\"],\n" +
				"   \"client_name\": \"My Example\",\n" +
				"   \"client_name#ja-Jpan-JP\":\n" +
				"     \"クライアント名\",\n" +
				"   \"response_types\": [\"code\", \"token\"],\n" +
				"   \"grant_types\": [\"authorization_code\", \"implicit\"],\n" +
				"   \"logo_uri\": \"https://client.example.org/logo.png\",\n" +
				"   \"subject_type\": \"pairwise\",\n" +
				"   \"sector_identifier_uri\":\n" +
				"     \"https://other.example.net/file_of_redirect_uris.json\",\n" +
				"   \"token_endpoint_auth_method\": \"client_secret_basic\",\n" +
				"   \"jwks_uri\": \"https://client.example.org/my_public_keys.jwks\",\n" +
				"   \"userinfo_encrypted_response_alg\": \"RSA1_5\",\n" +
				"   \"userinfo_encrypted_response_enc\": \"A128CBC-HS256\",\n" +
				"   \"contacts\": [\"ve7jtb@example.org\", \"mary@example.org\"],\n" +
				"   \"request_uris\":\n" +
				"     [\"https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA\"]\n" +
				"  }";
		ClientDetailsEntity c = ClientDetailsEntityJsonProcessor.parse(json);

		assertEquals(ClientDetailsEntity.AppType.WEB, c.getApplicationType());
		assertEquals(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"), c.getRedirectUris());
		assertEquals("My Example", c.getClientName());
		assertEquals(ImmutableSet.of("code", "token"), c.getResponseTypes());
		assertEquals(ImmutableSet.of("authorization_code", "implicit"), c.getGrantTypes());
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
	 * Test method for {@link ClientDetailsEntityJsonProcessor#parseRegistered(java.lang.String)}.
	 */
	@Test
	public void testParseRegistered() {
		String json = "  {\n" +
				"   \"client_id\": \"s6BhdRkqt3\",\n" +
				"   \"client_secret\":\n" +
				"     \"ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk\",\n" +
				"   \"client_secret_expires_at\": 1577858400,\n" +
				"   \"registration_access_token\":\n" +
				"     \"this.is.an.access.token.value.ffx83\",\n" +
				"   \"registration_client_uri\":\n" +
				"     \"https://server.example.com/connect/register?client_id=s6BhdRkqt3\",\n" +
				"   \"token_endpoint_auth_method\":\n" +
				"     \"client_secret_basic\",\n" +
				"   \"application_type\": \"web\",\n" +
				"   \"redirect_uris\":\n" +
				"     [\"https://client.example.org/callback\",\n" +
				"      \"https://client.example.org/callback2\"],\n" +
				"   \"client_name\": \"My Example\",\n" +
				"   \"client_name#ja-Jpan-JP\":\n" +
				"     \"クライアント名\",\n" +
				"   \"response_types\": [\"code\", \"token\"],\n" +
				"   \"grant_types\": [\"authorization_code\", \"implicit\"],\n" +
				"   \"logo_uri\": \"https://client.example.org/logo.png\",\n" +
				"   \"subject_type\": \"pairwise\",\n" +
				"   \"sector_identifier_uri\":\n" +
				"     \"https://other.example.net/file_of_redirect_uris.json\",\n" +
				"   \"jwks_uri\": \"https://client.example.org/my_public_keys.jwks\",\n" +
				"   \"userinfo_encrypted_response_alg\": \"RSA1_5\",\n" +
				"   \"userinfo_encrypted_response_enc\": \"A128CBC-HS256\",\n" +
				"   \"contacts\": [\"ve7jtb@example.org\", \"mary@example.org\"],\n" +
				"   \"request_uris\":\n" +
				"     [\"https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA\"]\n" +
				"  }";

		RegisteredClient c = ClientDetailsEntityJsonProcessor.parseRegistered(json);


		assertEquals("s6BhdRkqt3", c.getClientId());
		assertEquals("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk", c.getClientSecret());
		assertEquals(new Date(1577858400L * 1000L), c.getClientSecretExpiresAt());
		assertEquals("this.is.an.access.token.value.ffx83", c.getRegistrationAccessToken());
		assertEquals("https://server.example.com/connect/register?client_id=s6BhdRkqt3", c.getRegistrationClientUri());
		assertEquals(ClientDetailsEntity.AppType.WEB, c.getApplicationType());
		assertEquals(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"), c.getRedirectUris());
		assertEquals("My Example", c.getClientName());
		assertEquals(ImmutableSet.of("code", "token"), c.getResponseTypes());
		assertEquals(ImmutableSet.of("authorization_code", "implicit"), c.getGrantTypes());
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
	 * Test method for {@link ClientDetailsEntityJsonProcessor#serialize(RegisteredClient)}.
	 */
	@Test
	public void testSerialize() {
		RegisteredClient c = new RegisteredClient();

		c.setClientId("s6BhdRkqt3");
		c.setClientSecret("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk");
		c.setClientSecretExpiresAt(new Date(1577858400L * 1000L));
		c.setRegistrationAccessToken("this.is.an.access.token.value.ffx83");
		c.setRegistrationClientUri("https://server.example.com/connect/register?client_id=s6BhdRkqt3");
		c.setApplicationType(ClientDetailsEntity.AppType.WEB);
		c.setRedirectUris(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"));
		c.setClientName("My Example");
		c.setResponseTypes(ImmutableSet.of("code", "token"));
		c.setGrantTypes(ImmutableSet.of("authorization_code", "implicit"));
		c.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		c.setSectorIdentifierUri("https://other.example.net/file_of_redirect_uris.json");
		c.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
		c.setJwksUri("https://client.example.org/my_public_keys.jwks");
		c.setUserInfoEncryptedResponseAlg(JWEAlgorithm.RSA1_5);
		c.setUserInfoEncryptedResponseEnc(EncryptionMethod.A128CBC_HS256);
		c.setContacts(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"));
		c.setRequestUris(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"));

		JsonObject j = ClientDetailsEntityJsonProcessor.serialize(c);

		assertEquals("s6BhdRkqt3", j.get("client_id").getAsString());
		assertEquals("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk", j.get("client_secret").getAsString());
		assertEquals(1577858400L, j.get("client_secret_expires_at").getAsNumber());
		assertEquals("this.is.an.access.token.value.ffx83", j.get("registration_access_token").getAsString());
		assertEquals("https://server.example.com/connect/register?client_id=s6BhdRkqt3", j.get("registration_client_uri").getAsString());
		assertEquals(ClientDetailsEntity.AppType.WEB.getValue(), j.get("application_type").getAsString());
		for (JsonElement e : j.get("redirect_uris").getAsJsonArray()) {
			assertTrue(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2").contains(e.getAsString()));
		}
		assertEquals("My Example", j.get("client_name").getAsString());
		for (JsonElement e : j.get("response_types").getAsJsonArray()) {
			assertTrue(ImmutableSet.of("code", "token").contains(e.getAsString()));
		}
		for (JsonElement e : j.get("grant_types").getAsJsonArray()) {
			assertTrue(ImmutableSet.of("authorization_code", "implicit").contains(e.getAsString()));
		}
		assertEquals(ClientDetailsEntity.SubjectType.PAIRWISE.getValue(), j.get("subject_type").getAsString());
		assertEquals("https://other.example.net/file_of_redirect_uris.json", j.get("sector_identifier_uri").getAsString());
		assertEquals(ClientDetailsEntity.AuthMethod.SECRET_BASIC.getValue(), j.get("token_endpoint_auth_method").getAsString());
		assertEquals("https://client.example.org/my_public_keys.jwks", j.get("jwks_uri").getAsString());
		assertEquals(JWEAlgorithm.RSA1_5.getName(), j.get("userinfo_encrypted_response_alg").getAsString());
		assertEquals(EncryptionMethod.A128CBC_HS256.getName(), j.get("userinfo_encrypted_response_enc").getAsString());
		for (JsonElement e : j.get("contacts").getAsJsonArray()) {
			assertTrue(ImmutableSet.of("ve7jtb@example.org", "mary@example.org").contains(e.getAsString()));
		}
		for (JsonElement e : j.get("request_uris").getAsJsonArray()) {
			assertTrue(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA").contains(e.getAsString()));
		}

	}

}
