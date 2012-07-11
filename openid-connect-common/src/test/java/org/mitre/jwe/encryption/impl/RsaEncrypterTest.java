package org.mitre.jwe.encryption.impl;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwe.model.JweHeader;
import org.mitre.jwt.encryption.impl.RsaEncrypter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class RsaEncrypterTest {
	
	URL jweHeaderUrl = this.getClass().getResource("/jwe/jweHeader");
	URL jwePlaintextUrl = this.getClass().getResource("/jwe/jwePlaintext");
	
	@Before
	public void setUp(){
	}
	
	@After
	public void tearDown(){
	}
	
	@Test
	public void encryptTest() throws JsonIOException, JsonSyntaxException, IOException {
		
		JsonParser parser = new JsonParser();
		JsonObject jweHeaderObject = parser.parse(new BufferedReader(new InputStreamReader(jweHeaderUrl.openStream()))).getAsJsonObject();
		String jwePlaintextString = parser.parse(new BufferedReader(new InputStreamReader(jwePlaintextUrl.openStream()))).toString();
		
		Jwe jwe = new Jwe(new JweHeader(jweHeaderObject), null, jwePlaintextString.getBytes(), null);
		
		RsaEncrypter rsaEncrypter = new RsaEncrypter();
		try {
			jwe = rsaEncrypter.encryptAndSign(jwe);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(jwe.toString(), "yooooo");
		
	}

}
