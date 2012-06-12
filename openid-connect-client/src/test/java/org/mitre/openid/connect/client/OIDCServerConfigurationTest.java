package org.mitre.openid.connect.client;

import java.net.URL;
import java.security.Key;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mitre.util.Utility;

import junit.framework.TestCase;

public class OIDCServerConfigurationTest extends TestCase {
	
	URL jwkUrl = this.getClass().getResource("/jwk/jwk");
	URL x509Url = this.getClass().getResource("/x509/x509");
	URL jwkEncryptedUrl = this.getClass().getResource("/jwk/jwkEncrypted");
	URL x509EncryptedUrl = this.getClass().getResource("/x509/x509Encrypted");
	OIDCServerConfiguration oidc;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		oidc = new OIDCServerConfiguration();
		super.setUp();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.mitre.util.Utility#retrieveJwk(java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testGetSigningKeyBoth() throws Exception {
		oidc.setX509SigningUrl(x509Url.getPath());
		oidc.setJwkSigningUrl(jwkUrl.getPath());
		Key key = oidc.getSigningKey();
		assertEquals(key, Utility.retrieveX509Key(x509Url));
	}
	
	@Test
	public void testGetSigningKeyJwk() throws Exception {
		oidc.setX509SigningUrl(null);
		oidc.setJwkSigningUrl(jwkUrl.getPath());
		Key key1 = oidc.getSigningKey();
		assertEquals(key1, Utility.retrieveJwkKey(jwkUrl));
	}
	
	@Test
	public void testGetSigningKeyX509() throws Exception {
		oidc.setX509SigningUrl(x509Url.getPath());
		oidc.setJwkSigningUrl(null);
		Key key2 = oidc.getSigningKey();
		assertEquals(key2, Utility.retrieveX509Key(x509Url));
	}
	
	@Test
	public void testGetSigningKeyNone() throws Exception {
		oidc.setX509SigningUrl(null);
		oidc.setJwkSigningUrl(null);
		Key key3 = oidc.getSigningKey();
		assertEquals(key3, null);
	}
	
	@Test
	public void testGetEncryptionKeyBoth() throws Exception {
		oidc.setX509EncryptUrl(x509EncryptedUrl.getPath());
		oidc.setJwkEncryptUrl(jwkEncryptedUrl.getPath());
		Key key = oidc.getEncryptionKey();
		assertEquals(key, Utility.retrieveX509Key(x509EncryptedUrl));
	}
	
	@Test
	public void testGetEncryptionKeyJwk() throws Exception {
		oidc.setX509EncryptUrl(null);
		oidc.setJwkEncryptUrl(jwkEncryptedUrl.getPath());
		Key key1 = oidc.getEncryptionKey();
		assertEquals(key1, Utility.retrieveJwkKey(jwkEncryptedUrl));
	}
	
	@Test
	public void testGetEncryptionKeyX509() throws Exception {
		oidc.setX509EncryptUrl(x509EncryptedUrl.getPath());
		oidc.setJwkEncryptUrl(null);
		Key key2 = oidc.getEncryptionKey();
		assertEquals(key2, Utility.retrieveX509Key(x509EncryptedUrl));
	}
	
	@Test
	public void testGetEncryptionKeyNone() throws Exception {
		oidc.setX509EncryptUrl(null);
		oidc.setJwkEncryptUrl(null);
		Key key3 = oidc.getEncryptionKey();
		assertEquals(key3, null);
	}
	
	@Test
	public void testGetDynamic() throws Exception {
		oidc.setX509SigningUrl(x509Url.getPath());
		oidc.setJwkSigningUrl(jwkUrl.getPath());
		oidc.setClientSecret("foo");
		assertEquals(oidc.getDynamic().getSigningX509Url(), x509Url.getPath());
		assertEquals(oidc.getDynamic().getSigningJwkUrl(), jwkUrl.getPath());
		assertEquals(oidc.getDynamic().getClientSecret(), "foo");
	}
}
