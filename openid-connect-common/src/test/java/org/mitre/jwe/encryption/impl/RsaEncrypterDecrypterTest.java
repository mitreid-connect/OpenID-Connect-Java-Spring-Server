package org.mitre.jwe.encryption.impl;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwe.model.JweHeader;
import org.mitre.jwt.encryption.impl.RsaDecrypter;
import org.mitre.jwt.encryption.impl.RsaEncrypter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class RsaEncrypterDecrypterTest {
	
	URL jweHeaderUrl = this.getClass().getResource("/jwe/jweHeader");
	String jwePlaintextString = new String("Why couldn't the bike move? It was two tired.");
	
	@Before
	public void setUp() throws NoSuchAlgorithmException{
		
		Assume.assumeTrue(Cipher.getMaxAllowedKeyLength("AES") > 128); // if we're capped at 128 bits then we can't run these tests
		
	}
	
	@After
	public void tearDown(){
	}
	
	@Test
	public void encryptDecryptTest() throws JsonIOException, JsonSyntaxException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {

		// 
		
		//read in header and plaintext from files
		JsonParser parser = new JsonParser();
		JsonObject jweHeaderObject = parser.parse(new BufferedReader(new InputStreamReader(jweHeaderUrl.openStream()))).getAsJsonObject();
		//generate key pair. this will be passed in from the user
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(4096);
		KeyPair pair = keyGen.generateKeyPair();
		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();
		//create jwe based on header and plaintext
		Jwe jwe = new Jwe(new JweHeader(jweHeaderObject), null, jwePlaintextString.getBytes(), null);
		//encrypt
		RsaEncrypter rsaEncrypter = new RsaEncrypter();
		jwe = rsaEncrypter.encryptAndSign(jwe, publicKey);

		//decrypt
		RsaDecrypter rsaDecrypter = new RsaDecrypter();
		String encryptedJweString = jwe.toString();
		jwe = rsaDecrypter.decrypt(encryptedJweString, privateKey);
		
		String jweDecryptedCleartext = new String(jwe.getCiphertext());
		//test ALL THE THINGS
		assertEquals(jweDecryptedCleartext, jwePlaintextString);	
		assertEquals(jwe.getHeader().getAlgorithm(), jweHeaderObject.get("alg").getAsString());
		assertEquals(jwe.getHeader().getEncryptionMethod(), jweHeaderObject.get("enc").getAsString());
		assertEquals(jwe.getHeader().getIntegrity(), jweHeaderObject.get("int").getAsString());
		assertEquals(jwe.getHeader().getInitializationVector(), jweHeaderObject.get("iv").getAsString());
		
	}

	// TODO: add independent unit test for encryption and decryption
	
}
