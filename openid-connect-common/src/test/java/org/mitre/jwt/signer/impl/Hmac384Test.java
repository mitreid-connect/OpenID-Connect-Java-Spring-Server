package org.mitre.jwt.signer.impl;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class Hmac384Test {
	
	URL claimsUrl = this.getClass().getResource("/jwt/claims");
	URL hs384Url = this.getClass().getResource("/jwt/hs384");

	Jwt jwt = null;
	JwtClaims claims = null;
	JwtHeader header = null;
	
	/**
	 * @throws IOException 
	 * @throws JsonSyntaxException 
	 * @throws JsonIOException 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws JsonIOException, JsonSyntaxException, IOException{
		JsonParser parser = new JsonParser();
		JsonObject claimsObject = parser.parse(new BufferedReader(new InputStreamReader(claimsUrl.openStream()))).getAsJsonObject();
		JsonObject headerObject = parser.parse(new BufferedReader(new InputStreamReader(hs384Url.openStream()))).getAsJsonObject();
		claims = new JwtClaims(claimsObject);
		header = new JwtHeader(headerObject);
		jwt = new Jwt(header, claims, null);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown(){
	}
	
	@Test
	public void testHmacSigner384() throws Exception {
		setUp();
		HmacSigner hmac = new HmacSigner(header.getAlgorithm(), "secret");
		jwt = hmac.sign(jwt);
		assertEquals(hmac.verify(jwt.toString()), true);
	}

}
