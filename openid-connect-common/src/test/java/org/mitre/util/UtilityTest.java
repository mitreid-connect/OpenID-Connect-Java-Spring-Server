/**
 * 
 */
package org.mitre.util;

import static org.junit.Assert.assertEquals;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwk.model.Jwk;
import org.mitre.jwk.model.Rsa;
import org.mitre.jwk.model.EC;
import org.mitre.key.fetch.KeyFetcher;
import org.mitre.util.Utility;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.codec.binary.*;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.jce.provider.JCEECPublicKey;

/**
 * @author DERRYBERRY
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class UtilityTest {
	
	URL url = this.getClass().getResource("/jwk/jwkSuccess");
	URL certUrl = this.getClass().getResource("/x509/x509Cert");
	URL rsaUrl = this.getClass().getResource("/jwk/rsaOnly");
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
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
	public void testRetrieveJwk() throws Exception {
		
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(new BufferedReader(new InputStreamReader(url.openStream()))).getAsJsonObject();
		JsonArray getArray = json.getAsJsonArray("jwk");
		
		List<Jwk> list = KeyFetcher.retrieveJwk();

		for(int i = 0; i < list.size(); i++){
			
			Jwk jwk = list.get(i);
			JsonObject object = getArray.get(i).getAsJsonObject();
			
			assertEquals(object.get("alg").getAsString(), jwk.getAlg());
			if(object.get("kid") != null){
				assertEquals(object.get("kid").getAsString(), jwk.getKid());
			}
			if(object.get("use") != null){
				assertEquals(object.get("use").getAsString(), jwk.getUse());
			}
			
			if(jwk instanceof Rsa){			
				assertEquals(object.get("mod").getAsString(), ((Rsa) jwk).getMod());
				assertEquals(object.get("exp").getAsString(), ((Rsa) jwk).getExp());
			}
			else {
				assertEquals(object.get("crv").getAsString(), ((EC) jwk).getCrv());
				assertEquals(object.get("x").getAsString(), ((EC) jwk).getX());
				assertEquals(object.get("y").getAsString(), ((EC) jwk).getY());
			}
		}
	}
	
	@Test
	public void testMakeRsa() throws Exception{
		
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(new BufferedReader(new InputStreamReader(url.openStream()))).getAsJsonObject();
		JsonArray getArray = json.getAsJsonArray("jwk");
		
		List<Jwk> list = KeyFetcher.retrieveJwk();
		
		for(int i = 0; i < list.size(); i++){
			Jwk jwk = list.get(i);
			JsonObject object = getArray.get(i).getAsJsonObject();
			
			if(jwk instanceof Rsa){

				RSAPublicKey key = ((RSAPublicKey) ((Rsa) jwk).getKey());
				
				byte[] mod = Base64.decodeBase64(object.get("mod").getAsString());
				BigInteger modInt = new BigInteger(mod);
				assertEquals(modInt, key.getModulus());
				
				byte[] exp = Base64.decodeBase64(object.get("exp").getAsString());
				BigInteger expInt = new BigInteger(exp);
				assertEquals(expInt, key.getPublicExponent());
			}
		}	
	}
	
	@Test
	public void testRetriveX509Key() throws Exception {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate x509 = (X509Certificate) factory.generateCertificate(certUrl.openStream());
		Key key = KeyFetcher.retrieveX509Key();
		assertEquals(x509.getPublicKey(), key);
		assertEquals("RSA", key.getAlgorithm());
		assertEquals("X.509", key.getFormat());
	}
	
	public void testRetriveJwkKey() throws Exception {
		Key key = KeyFetcher.retrieveJwkKey();
		
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(new BufferedReader(new InputStreamReader(rsaUrl.openStream()))).getAsJsonObject();
		JsonArray getArray = json.getAsJsonArray("jwk");
		JsonObject object = getArray.get(0).getAsJsonObject();
			
		byte[] modulusByte = Base64.decodeBase64(object.get("mod").getAsString());
		BigInteger modulus = new BigInteger(modulusByte);
		byte[] exponentByte = Base64.decodeBase64(object.get("exp").getAsString());
		BigInteger exponent = new BigInteger(exponentByte);
				
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PublicKey pub = factory.generatePublic(spec);
		
		assertEquals(pub, key);
	}
	
	//@Test
	//public void testMakeEC() throws Exception{
		
		/*JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(new BufferedReader(new InputStreamReader(url.openStream()))).getAsJsonObject();
		JsonArray getArray = json.getAsJsonArray("jwk");
		
		List<Jwk> list = Utility.retrieveJwk(url);
		
		for(int i = 0; i < list.size(); i++){
			Jwk jwk = list.get(i);
			JsonObject object = getArray.get(i).getAsJsonObject();
			
			if(jwk instanceof EC){
				
				ECPublicKey key = ((ECPublicKey) ((EC) jwk).getKey());
				
				byte[] xArray = Base64.decodeBase64(object.get("x").getAsString());
				BigInteger xInt = new BigInteger(xArray);
				byte[] yArray = Base64.decodeBase64(object.get("y").getAsString());
				BigInteger yInt = new BigInteger(yArray);
				
				String curveName = object.get("crv").getAsString();
				ECNamedCurveParameterSpec curveSpec = ECNamedCurveTable.getParameterSpec(curveName);
				ECCurve crv = curveSpec.getCurve();
				BigInteger a = crv.getA().toBigInteger();
				BigInteger b = crv.getB().toBigInteger();
				int fieldSize = crv.getFieldSize();
				BigInteger orderOfGen = curveSpec.getH();
				int cofactor = Math.abs(curveSpec.getN().intValue());
				
				assertEquals(a, key.getParams().getCurve().getA());
				assertEquals(b, key.getParams().getCurve().getB());
				assertEquals(fieldSize, key.getParams().getCurve().getField());
				assertEquals(orderOfGen, key.getParams().getOrder());
				assertEquals(cofactor, key.getParams().getCofactor());
				assertEquals(xInt, key.getW().getAffineX());
				assertEquals(yInt, key.getW().getAffineY());
			}
		}*/
		//fail("method not implemented");
	//}
	
	
}