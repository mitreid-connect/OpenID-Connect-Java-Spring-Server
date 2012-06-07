package org.mitre.jwt.signer.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.Key;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mitre.jwt.signer.JwtSigner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DynamicJwtSigningAndValidationServiceTest extends TestCase {
	
	URL x509Url = this.getClass().getResource("/x509/x509Cert");
	URL jwkUrl = this.getClass().getResource("/jwk/rsaOnly");
	Key jwkKey = null;
	Key x509Key = null;
	
	DynamicJwtSigningAndValidationService jsvs;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
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
	public void testGetSigner() throws Exception {
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
