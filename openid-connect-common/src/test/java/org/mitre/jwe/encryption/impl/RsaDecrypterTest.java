package org.mitre.jwe.encryption.impl;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.impl.RsaDecrypter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class RsaDecrypterTest {
	
	URL jweUrl = this.getClass().getResource("/jwe/encryptedJwe");
	
	@Before
	public void setUp(){
	}
	
	@After
	public void tearDown(){
	}
	
	@Test
	public void decryptTest() throws JsonIOException, JsonSyntaxException, IOException {
		
		JsonParser parser = new JsonParser();
		String jweString = parser.parse(new BufferedReader(new InputStreamReader(jweUrl.openStream()))).toString();
		
		RsaDecrypter rsaDecrypter = new RsaDecrypter();
		Jwe jwe = rsaDecrypter.decrypt(jweString);
		
		assertEquals(jwe.getCiphertext().toString(), "Now is the time for all good men to come to the aid of their country.");
		
	}

}
