package org.mitre.jwt.signer.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;
import org.mitre.jwt.signer.JwsAlgorithm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RsaSignerTest extends TestCase {
	
	
	URL claimsUrl = this.getClass().getResource("/jwt/claims");
	URL rs256Url = this.getClass().getResource("/jwt/rs256");
	URL rs384Url = this.getClass().getResource("/jwt/rs384");
	URL rs512Url = this.getClass().getResource("/jwt/rs512");
	Jwt jwt = null;
	JwtClaims claims = null;
	JwtHeader header = null;
	KeyPairGenerator keyGen;
	KeyPair keyPair;
	PublicKey publicKey;
	PrivateKey privateKey;
	
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
	public void testRsaSigner256() throws Exception {
		setUp(rs256Url);
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyPair = keyGen.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		RsaSigner rsa = new RsaSigner(JwsAlgorithm.RS256.toString(), publicKey, privateKey);
		jwt = rsa.sign(jwt);
		assertEquals(rsa.verify(jwt.toString()), true);

	}
	
	@Test
	public void testRsaSigner384() throws Exception{
		setUp(rs384Url);
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyPair = keyGen.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		RsaSigner rsa = new RsaSigner(JwsAlgorithm.RS384.toString(), publicKey, privateKey);
		jwt = rsa.sign(jwt);
		assertEquals(rsa.verify(jwt.toString()), true);

	}
	
	@Test
	public void testRsaSigner512() throws Exception{
		setUp(rs512Url);
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyPair = keyGen.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		RsaSigner rsa = new RsaSigner(JwsAlgorithm.RS512.toString(), publicKey, privateKey);
		jwt = rsa.sign(jwt);
		assertEquals(rsa.verify(jwt.toString()), true);

	}
	

}
