/**
 * 
 */
package org.mitre.oauth2.model;

import java.util.Date;

import org.junit.Test;
import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;

import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;

import static org.junit.Assert.*;

/**
 * @author jricher
 *
 */
public class ClientDetailsEntityTest {

	/**
	 * Test method for {@link org.mitre.oauth2.model.ClientDetailsEntity#ClientDetailsEntity()}.
	 */
	@Test
	public void testClientDetailsEntity() {
		Date now = new Date(); 
		
		ClientDetailsEntity c = new ClientDetailsEntity();
		
		c.setClientId("s6BhdRkqt3");
		c.setClientSecret("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk");
		c.setApplicationType(ClientDetailsEntity.AppType.WEB);
		c.setRedirectUris(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"));
		c.setClientName("My Example");
		c.setLogoUri("https://client.example.org/logo.png");
		c.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		c.setSectorIdentifierUri("https://other.example.net/file_of_redirect_uris.json");
		c.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
		c.setJwksUri("https://client.example.org/my_public_keys.jwks");
		c.setUserInfoEncryptedResponseAlg(new JWEAlgorithmEmbed(JWEAlgorithm.RSA1_5));
		c.setUserInfoEncryptedResponseEnc(new JWEEncryptionMethodEmbed(EncryptionMethod.A128CBC_HS256));
		c.setContacts(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"));
		c.setRequestUris(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"));
		c.setCreatedAt(now);
		c.setAccessTokenValiditySeconds(600);

		assertEquals("s6BhdRkqt3", c.getClientId());
		assertEquals("ZJYCqe3GGRvdrudKyZS0XhGv_Z45DuKhCUk0gBR1vZk", c.getClientSecret());
		assertEquals(ClientDetailsEntity.AppType.WEB, c.getApplicationType());
		assertEquals(ImmutableSet.of("https://client.example.org/callback", "https://client.example.org/callback2"), c.getRedirectUris());
		assertEquals("My Example", c.getClientName());
		assertEquals("https://client.example.org/logo.png", c.getLogoUri());
		assertEquals(ClientDetailsEntity.SubjectType.PAIRWISE, c.getSubjectType());
		assertEquals("https://other.example.net/file_of_redirect_uris.json", c.getSectorIdentifierUri());
		assertEquals(ClientDetailsEntity.AuthMethod.SECRET_BASIC, c.getTokenEndpointAuthMethod());
		assertEquals("https://client.example.org/my_public_keys.jwks", c.getJwksUri());
		assertEquals(JWEAlgorithm.RSA1_5, c.getUserInfoEncryptedResponseAlg().getAlgorithm());
		assertEquals(EncryptionMethod.A128CBC_HS256, c.getUserInfoEncryptedResponseEnc().getAlgorithm());
		assertEquals(ImmutableSet.of("ve7jtb@example.org", "mary@example.org"), c.getContacts());
		assertEquals(ImmutableSet.of("https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"), c.getRequestUris());
		assertEquals(now, c.getCreatedAt());
		assertEquals(600, c.getAccessTokenValiditySeconds().intValue());

	}

}
