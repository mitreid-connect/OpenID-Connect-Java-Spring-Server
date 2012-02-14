package org.mitre.jwt;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.HmacSigner;
import org.mitre.jwt.signer.impl.PlaintextSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.KeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"})
public class JwtTest {
	
//	@Autowired
//	KeyStore keystore;
	
	@Test
	public void testToStringPlaintext() {
		Jwt jwt = new Jwt();
		jwt.getHeader().setAlgorithm("none");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);

		// sign it with a blank signature
		JwtSigner signer = new PlaintextSigner();
		signer.sign(jwt);
		
		/*
		 * Expected string based on the following structures, serialized exactly as follows and base64 encoded:
		 * 
		 * header: {"alg":"none"}
		 * claims: {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
		 */
		String expected = "eyJhbGciOiJub25lIn0.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";
		
		String actual = jwt.toString();
			
		assertThat(actual, equalTo(expected));
		
	}

	@Test
	public void testGenerateHmacSignature() {
		Jwt jwt = new Jwt();
		jwt.getHeader().setType("JWT");
		jwt.getHeader().setAlgorithm("HS256");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);

		// sign it
		byte[] key = null;
        try {
	        key = "secret".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        JwtSigner signer = new HmacSigner(key);

		signer.sign(jwt);

		/*
		 * Expected string based on the following strucutres, serialized exactly as follows and base64 encoded:
		 * 
		 * header: {"typ":"JWT","alg":"HS256"}
		 * claims: {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
		 * 
		 * Expected signature: iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E
		 *
		 */
		String signature = "p-63Jzz7mgi3H4hvW6MFB7lmPRZjhsL666MYkmpX33Y";
		String expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ." + signature;
		
		String actual = jwt.toString();

		assertThat(actual, equalTo(expected));
		assertThat(jwt.getSignature(), equalTo(signature));
		
	}
	
//	@Test
//	public void testGenerateRsaSignature() {
//		Jwt jwt = new Jwt();
//		jwt.getHeader().setType("JWT");
//		jwt.getHeader().setAlgorithm("RS256");
//		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
//		jwt.getClaims().setIssuer("joe");
//		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);
//
//		// sign it
//		System.out.println("keystore PROVIDER::" + keystore.getProvider());
//        
//        JwtSigner signer = new RsaSigner(RsaSigner.Algorithm.DEFAULT, keystore, "test");
//
//		signer.sign(jwt);
//
//		System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
//		System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
//		System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
//		System.out.println(jwt.getSignature());
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");		
//
////		String signature = "p-63Jzz7mgi3H4hvW6MFB7lmPRZjhsL666MYkmpX33Y";
////		String expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ." + signature;
////		
////		String actual = jwt.toString();
////
////		assertThat(actual, equalTo(expected));
////		assertThat(jwt.getSignature(), equalTo(signature));
//		
//	}	
	
	@Test
	public void testValidateHmacSignature() {
		// sign it
		byte[] key = null;
        try {
	        key = "secret".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		
		JwtSigner signer = new HmacSigner(key);

		/*
		 * Token string based on the following strucutres, serialized exactly as follows and base64 encoded:
		 * 
		 * header: {"typ":"JWT","alg":"HS256"}
		 * claims: {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
		 * 
		 * Expected signature: iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E
		 *
		 */
		String jwtString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E";
		
		boolean valid = signer.verify(jwtString);
		
		assertThat(valid, equalTo(Boolean.TRUE));
		
	}
	
	@Test
	public void testParse() {
		String source = "eyJhbGciOiJub25lIn0.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";
		
		
		Jwt jwt = Jwt.parse(source);
		
		assertThat(jwt.getHeader().getAlgorithm(), equalTo(PlaintextSigner.PLAINTEXT));
		assertThat(jwt.getClaims().getIssuer(), equalTo("joe"));
		assertThat(jwt.getClaims().getExpiration(), equalTo(new Date(1300819380L * 1000L)));
		assertThat((Boolean)jwt.getClaims().getClaim("http://example.com/is_root"), equalTo(Boolean.TRUE));
		
	}

}
