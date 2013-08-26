package org.mitre.jwt.signer.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.Use;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

/**
 * Key information and test data are taken from Mike Jones's JWS RFC draft.
 * draft-ietf-jose-json-web-signature-14#appendix-A.2
 * 
 * @author wkim
 *
 */
public class TestDefaultJwtSigningAndValidationService {
	
	private DefaultJwtSigningAndValidationService service;
	
	Map<String, Object> payloadMap;
	
	JWTClaimsSet claims;
	
	private String RSAkid = "rsa321";
	
	private RSAKey key = new RSAKey(new Base64URL("ofgWCuLjybRlzo0tZWJjNiuSfb4p4fAkd_wWJcyQoTbji9k0l8W26mPddx" +
           "HmfHQp-Vaw-4qPCJrcS2mJPMEzP1Pt0Bm4d4QlL-yRT-SFd2lZS-pCgNMs" +
           "D1W_YpRPEwOWvG6b32690r2jZ47soMZo9wGzjb_7OMg0LOL-bSf63kpaSH" +
           "SXndS5z5rexMdbBYUsLA9e-KXBdQOS-UTo7WTBEMa2R2CapHg665xsmtdV" +
           "MTBQY4uDZlxvb3qCo5ZwKh9kG4LT6_I5IhlJH7aGhyxXFvUK-DWNmoudF8" +
           "NAco9_h9iaGNj8q2ethFkMLs91kzk2PAcDTW9gb54h4FRWyuXpoQ"), 
           new Base64URL("AQAB"), 
           new Base64URL("Eq5xpGnNCivDflJsRQBXHx1hdR1k6Ulwe2JZD50LpXyWPEAeP88vLNO97I" +
           "jlA7_GQ5sLKMgvfTeXZx9SE-7YwVol2NXOoAJe46sui395IW_GO-pWJ1O0" +
           "BkTGoVEn2bKVRUCgu-GjBVaYLU6f3l9kJfFNS3E0QbVdxzubSu3Mkqzjkn" +
           "439X0M_V51gfpRLI9JYanrC4D4qAdGcopV_0ZHHzQlBjudU2QvXt4ehNYT" +
           "CBr6XCLQUShb1juUO1ZdiYoFaFQT5Tw8bGUl_x_jTj3ccPDVZFD9pIuhLh" +
           "BOneufuBiB4cS98l2SR_RQyGWSeWjnczT0QU91p1DhOVRuOopznQ"), 
           Use.SIGNATURE, JWSAlgorithm.RS256, RSAkid, null, null, null);

	@Before
	public void prepare() throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		Map<String, JWK> keyMap = new HashMap<String, JWK>();
		keyMap.put(RSAkid, key);
		
		service = new DefaultJwtSigningAndValidationService(keyMap);
		service.setDefaultSignerKeyId(RSAkid);
		
		/*
	     {"iss":"joe",
	      "exp":1300819380,
	      "http://example.com/is_root":true}
	     */
		payloadMap = new LinkedHashMap<String, Object>();
		payloadMap.put("iss", "joe");
		payloadMap.put("exp", new Date(1300819380L * 1000L));
		payloadMap.put("http://example.com/is_root", true);

		claims = new JWTClaimsSet();
		claims.setAllClaims(payloadMap);
	}
	
	/*
	 * The following expected values from the draft does not work with our implementation because
	 * the JWTClaimsSet will not maintain an ordered set of map entries.
	 */
	
//	@Test
//	public void sign_RS256() {
//		
//		String headerPart = "eyJhbGciOiJSUzI1NiJ9";
//		String payloadPart = "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFt" +
//     "cGxlLmNvbS9pc19yb290Ijp0cnVlfQ";
//		String expectedSignature = "cC4hiUPoj9Eetdgtv3hF80EGrhuB__dzERat0XF9g2VtQgr9PJbu3XOiZj5RZmh7" +
//     "AAuHIm4Bh-0Qc_lF5YKt_O8W2Fp5jujGbds9uJdbF9CUAr7t1dnZcAcQjbKBYNX4" +
//     "BAynRFdiuB--f_nZLgrnbyTyWzO75vRK5h6xBArLIARNPvkSjtQBMHlb1L07Qe7K" +
//     "0GarZRmB_eSN9383LcOLn6_dO--xi12jzDwusC-eOkHWEsqtFZESc6BfI7noOPqv" +
//     "hJ1phCnvWh6IeYI2w9QOYEUipUTI8np6LbgGY9Fs98rqVt5AXLIhWkWywlVmtVrB" +
//     "p0igcN_IoypGlUPQGe77Rw";

//		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
//		
//		assertEquals("header encoding not the same", headerPart, jwt.getHeader().toBase64URL().toString()); // sanity check to make sure the JWT has stuff in it
//		assertEquals("payload encoding not the same", payloadPart, jwt.getPayload().toBase64URL().toString());
//		assertThat(jwt.getSignature(), nullValue());
//		
//		service.signJwt(jwt);
//		
//		assertEquals(expectedSignature, jwt.getSignature().toString());
//	}

	@Test(expected = IllegalStateException.class)
	public void verify_notSignedYet() {
		
		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		
		service.validateSignature(jwt);
	}
	
	@Test
	public void signThenVerify_RS256() {
		
		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		
		service.signJwt(jwt);
		
		assertTrue(service.validateSignature(jwt));
	}
	
	@Test
	public void sign_plain() throws ParseException {
		
		PlainJWT jwt = new PlainJWT(claims);
		
		assertEquals(new Base64URL(""), PlainJWT.split(jwt.serialize())[2]); // no signature to start with
		
		service.signJwt(jwt);
		
		assertEquals(new Base64URL(""), PlainJWT.split(jwt.serialize())[2]); // no signature to end with
	}
	
	@Test
	public void verify_plain() {
		
		service.setDefaultSignerKeyId("none");
		
		PlainJWT jwt = new PlainJWT(claims);
		
		assertTrue(service.validateSignature(jwt));
		
		service.signJwt(jwt);
		
		assertTrue(service.validateSignature(jwt));
	}
}
