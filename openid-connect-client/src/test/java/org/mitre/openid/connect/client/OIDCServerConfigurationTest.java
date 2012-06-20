package org.mitre.openid.connect.client;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.key.fetch.KeyFetcher;
import org.mitre.util.Utility;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class OIDCServerConfigurationTest{
	
	private URL jwkUrl = this.getClass().getResource("/jwk/jwk");
	private URL x509Url = this.getClass().getResource("/x509/x509");
	private URL jwkEncryptedUrl = this.getClass().getResource("/jwk/jwkEncrypted");
	private URL x509EncryptedUrl = this.getClass().getResource("/x509/x509Encrypted");
	private OIDCServerConfiguration oidc;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp(){
		oidc = new OIDCServerConfiguration();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown(){
	}

	/**
	 * Test method for {@link org.mitre.util.Utility#retrieveJwk(java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testGetSigningKeyBoth(){
		oidc.setX509SigningUrl(x509Url.getPath());
		oidc.setJwkSigningUrl(jwkUrl.getPath());
		Key key = oidc.getSigningKey();
		try {
			assertEquals(key, KeyFetcher.retrieveX509Key());
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetSigningKeyJwk(){
		oidc.setX509SigningUrl(null);
		oidc.setJwkSigningUrl(jwkUrl.getPath());
		Key key1 = oidc.getSigningKey();
		try {
			assertEquals(key1, KeyFetcher.retrieveJwkKey());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetSigningKeyX509(){
		oidc.setX509SigningUrl(x509Url.getPath());
		oidc.setJwkSigningUrl(null);
		Key key2 = oidc.getSigningKey();
		try {
			assertEquals(key2, KeyFetcher.retrieveX509Key());
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetSigningKeyNone(){
		oidc.setX509SigningUrl(null);
		oidc.setJwkSigningUrl(null);
		Key key3 = oidc.getSigningKey();
		assertEquals(key3, null);
	}
	
	@Test
	public void testGetEncryptionKeyBoth(){
		oidc.setX509EncryptUrl(x509EncryptedUrl.getPath());
		oidc.setJwkEncryptUrl(jwkEncryptedUrl.getPath());
		Key key = oidc.getEncryptionKey();
		try {
			assertEquals(key, KeyFetcher.retrieveX509Key());
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetEncryptionKeyJwk(){
		oidc.setX509EncryptUrl(null);
		oidc.setJwkEncryptUrl(jwkEncryptedUrl.getPath());
		Key key1 = oidc.getEncryptionKey();
		try {
			assertEquals(key1, KeyFetcher.retrieveJwkKey());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetEncryptionKeyX509(){
		oidc.setX509EncryptUrl(x509EncryptedUrl.getPath());
		oidc.setJwkEncryptUrl(null);
		Key key2 = oidc.getEncryptionKey();
		try {
			assertEquals(key2, KeyFetcher.retrieveX509Key());
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetEncryptionKeyNone(){
		oidc.setX509EncryptUrl(null);
		oidc.setJwkEncryptUrl(null);
		Key key3 = oidc.getEncryptionKey();
		assertEquals(key3, null);
	}
}
