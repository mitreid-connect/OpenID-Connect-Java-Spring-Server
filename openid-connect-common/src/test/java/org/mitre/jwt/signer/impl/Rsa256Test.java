package org.mitre.jwt.signer.impl;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class Rsa256Test{
	
	
	URL claimsUrl = this.getClass().getResource("/jwt/claims");
	URL rs256Url = this.getClass().getResource("/jwt/rs256");

	Jwt jwt = null;
	JwtClaims claims = null;
	JwtHeader header = null;
	KeyPairGenerator keyGen;
	KeyPair keyPair;
	PublicKey publicKey;
	PrivateKey privateKey;
	
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
		JsonObject headerObject = parser.parse(new BufferedReader(new InputStreamReader(rs256Url.openStream()))).getAsJsonObject();
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
	public void testRsaSigner256() throws Exception {

		keyGen = KeyPairGenerator.getInstance("RSA");
		keyPair = keyGen.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		RsaSigner rsa = new RsaSigner(JwsAlgorithm.RS256.toString(), publicKey, privateKey);
		jwt = rsa.sign(jwt);
		assertEquals(rsa.verify(jwt.toString()), true);

	}
	
}