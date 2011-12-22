package org.mitre.jwt;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class JwtTest {

	@Test
	public void testToStringPlaintext() {
		Jwt jwt = new Jwt();
		jwt.getHeader().setAlgorithm("none");
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
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
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);

		// sign it
		byte[] key = null;
        try {
	        key = "secret".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		
		JwtSigner signer = new Hmac256Signer(key);

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
		String signature = "iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E";
		String expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E";
		
		String actual = jwt.toString();

		assertThat(actual, equalTo(expected));
		assertThat(jwt.getSignature(), equalTo(signature));
		
	}
	
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
		
		JwtSigner signer = new Hmac256Signer(key);

		String jwtString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E";
		
		boolean valid = signer.verify(jwtString);
		
		assertThat(valid, equalTo(Boolean.TRUE));
		
	}
	
	@Test
	public void testParse() {
		String source = "eyJhbGciOiJub25lIn0.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";
		
		
		Jwt jwt = Jwt.parse(source);
		
		assertThat(jwt.getHeader().getAlgorithm(), equalTo(AbstractJwtSigner.PLAINTEXT));
		assertThat(jwt.getClaims().getIssuer(), equalTo("joe"));
		assertThat(jwt.getClaims().getExpiration(), equalTo(new Date(1300819380L * 1000L)));
		assertThat((Boolean)jwt.getClaims().getClaim("http://example.com/is_root"), equalTo(Boolean.TRUE));
		
	}

}
