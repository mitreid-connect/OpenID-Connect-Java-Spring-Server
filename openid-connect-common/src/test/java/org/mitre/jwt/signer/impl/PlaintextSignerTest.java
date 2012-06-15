package org.mitre.jwt.signer.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import junit.framework.TestCase;

public class PlaintextSignerTest extends TestCase {
	
	URL claimsUrl = this.getClass().getResource("/jwt/claims");
	URL plaintextUrl = this.getClass().getResource("/jwt/plaintext");
	Jwt jwt = null;
	JwtClaims claims = null;
	JwtHeader header = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp(URL url) throws Exception {
		JsonParser parser = new JsonParser();
		JsonObject claimsObject = parser.parse(new BufferedReader(new InputStreamReader(claimsUrl.openStream()))).getAsJsonObject();
		JsonObject headerObject = parser.parse(new BufferedReader(new InputStreamReader(url.openStream()))).getAsJsonObject();
		claims = new JwtClaims(claimsObject);
		header = new JwtHeader(headerObject);
		jwt = new Jwt(header, claims, null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testPlaintextSigner() throws Exception {
		setUp(plaintextUrl);
		PlaintextSigner plaintext = new PlaintextSigner();
		jwt = plaintext.sign(jwt);
		assertEquals(plaintext.verify(jwt.toString()), true);
	}

}
