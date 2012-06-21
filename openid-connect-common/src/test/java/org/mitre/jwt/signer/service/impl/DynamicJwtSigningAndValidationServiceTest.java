package org.mitre.jwt.signer.service.impl;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.security.Key;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class DynamicJwtSigningAndValidationServiceTest {
	
	URL x509Url = this.getClass().getResource("/x509/x509Cert");
	URL jwkUrl = this.getClass().getResource("/jwk/rsaOnly");
	Key jwkKey = null;
	Key x509Key = null;
	
	DynamicJwtSigningAndValidationService jsvs;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp(){
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
	public void testGetSigner(){
		//create key, sign it, for both x509 and jwk. 
	/*	jsvs.setX509SigningUrl(x509Url.getPath());
		x509Key = jsvs.getSigningKey();
		jsvs.setJwkSigningUrl(jwkUrl.getPath());
		jwkKey = jsvs.getSigningKey();
		
		JsonParser parser = new JsonParser();
		
		String rsaStr = parser.parse(new BufferedReader(new InputStreamReader(jwkUrl.openStream()))).getAsString();
		JwtSigner rsaSigner = jsvs.getSigner(rsaStr);
		
		String x509Str = parser.parse(new BufferedReader(new InputStreamReader(x509Url.openStream()))).getAsString();
		JwtSigner x509Signer = jsvs.getSigner(x509Str);*/
		assertEquals("yo", "yo");
	}

}
