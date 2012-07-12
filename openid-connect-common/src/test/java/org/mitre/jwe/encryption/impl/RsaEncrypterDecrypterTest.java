package org.mitre.jwe.encryption.impl;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.After;
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
	URL jwePlaintextUrl = this.getClass().getResource("/jwe/jwePlaintext");
	URL jweEncryptedUrl = this.getClass().getResource("/jwe/encryptedJwe");
	String jweEncryptedUrlString = jweEncryptedUrl.toString();
	File jweEncryptedFile = new File(jweEncryptedUrlString);
	
	@Before
	public void setUp(){
	}
	
	@After
	public void tearDown(){
	}
	
	@Test
	public void encryptDecryptTest() throws JsonIOException, JsonSyntaxException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		
		JsonParser parser = new JsonParser();
		JsonObject jweHeaderObject = parser.parse(new BufferedReader(new InputStreamReader(jweHeaderUrl.openStream()))).getAsJsonObject();
		String jwePlaintextString = parser.parse(new BufferedReader(new InputStreamReader(jwePlaintextUrl.openStream()))).toString();
		
		Jwe jwe = new Jwe(new JweHeader(jweHeaderObject), null, jwePlaintextString.getBytes(), null);
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair pair = keyGen.generateKeyPair();
		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();
		//encrypt
		RsaEncrypter rsaEncrypter = new RsaEncrypter();
		jwe = rsaEncrypter.encryptAndSign(jwe, publicKey);
		//put encrypted jwe in text file to then be decrypted
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:/Users/derryberry/projects/OpenID-Connect-Java-Spring-Server-2/openid-connect-common/target/test-classes/jwe/encryptedJwe")));
		out.println(jwe.toString());
		out.close();
		
		String jweEncryptedString = parser.parse(new BufferedReader(new InputStreamReader(jweEncryptedUrl.openStream()))).toString();
		jweEncryptedString = jweEncryptedString.replaceAll("^\"|\"$", "");
		
		assertEquals(jwe.toString(), jweEncryptedString);
		//decrypt
		RsaDecrypter rsaDecrypter = new RsaDecrypter();
		String encryptedJweString = jwe.toString();
		jwe = rsaDecrypter.decrypt(encryptedJweString, privateKey);
		
		assertEquals(new String(jwe.getCiphertext()), jwePlaintextString);
		assertEquals(jwe.getHeader().getAlgorithm(), "RSA1_5");
		assertEquals(jwe.getHeader().getEncryptionMethod(), "A128CBC");
		assertEquals(jwe.getHeader().getIntegrity(), "HS256");
		assertEquals(jwe.getHeader().getInitializationVector(), "AxY8DCtDaGlsbGljb3RoZQ");
		
	}

}
