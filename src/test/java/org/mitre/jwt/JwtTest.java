package org.mitre.jwt;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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
		 * Expected string based on the following structures, serialized exactly as folows and base64 encoded:
		 * 
		 * header: {"alg":"none"}
		 * claims: {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
		 */
		String expected = "eyJhbGciOiJub25lIn0.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";
		
		String actual = jwt.toString();
			
		assertThat(actual, equalTo(expected));
		
	}

	@Test
	public void testHmacSignature() {
		Jwt jwt = new Jwt();
		jwt.getHeader().setType("JWT");
		jwt.getHeader().setAlgorithm("HS256");
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);

		// sign it
		byte[] key = "secret".getBytes();
		
		JwtSigner signer = new Hmac256Signer(key);

		signer.sign(jwt);
		                   
		String expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.vQqHHhblAtGiFs7q7nPt9Q";
		
		String actual = jwt.toString();
				
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
