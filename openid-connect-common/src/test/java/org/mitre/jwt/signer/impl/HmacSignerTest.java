package org.mitre.jwt.signer.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HmacSignerTest extends TestCase {
	
	URL claimsUrl = this.getClass().getResource("/jwt/claims");
	URL hs256Url = this.getClass().getResource("/jwt/hs256");
	URL hs384Url = this.getClass().getResource("/jwt/hs384");
	URL hs512Url = this.getClass().getResource("/jwt/hs512");
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
	public void testHmacSigner256() throws Exception {
		setUp(hs256Url);
		HmacSigner hmac = new HmacSigner(header.getAlgorithm(), "secret");
		jwt = hmac.sign(jwt);
		assertEquals(hmac.verify(jwt.toString()), true);
	}
	
	@Test
	public void testHmacSigner384() throws Exception {
		setUp(hs384Url);
		HmacSigner hmac = new HmacSigner(header.getAlgorithm(), "secret");
		jwt = hmac.sign(jwt);
		assertEquals(hmac.verify(jwt.toString()), true);
	}
	
	@Test
	public void testHmacSigner512() throws Exception {
		setUp(hs512Url);
		HmacSigner hmac = new HmacSigner(header.getAlgorithm(), "secret");
		jwt = hmac.sign(jwt);
		assertEquals(hmac.verify(jwt.toString()), true);
	}

}
