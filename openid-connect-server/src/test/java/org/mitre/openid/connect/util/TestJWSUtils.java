package org.mitre.openid.connect.util;


import static org.junit.Assert.assertEquals;
import net.minidev.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

@RunWith(MockitoJUnitRunner.class)
public class TestJWSUtils {
	
	@Mock
	OAuth2AccessTokenEntity mockToken256;
	
	@Before
	public void prepare() {
		
		JWTClaimsSet claims = new JWTClaimsSet();
		claims.setType("JWT");
		claims.setClaim("alg", "HS256");
		
		claims.setIssuer("www.example.com");
		claims.setSubject("example_user");
		
		
		Mockito.when(mockToken256.getJwt()).thenReturn(new PlainJWT(claims));
	}
	
	@Test
	public void getAccessTokenHash256() {
		
		/*
		 ascii of token = eyJhbGciOiJub25lIn0.eyJhbGciOiJIUzI1NiIsInN1YiI6ImV4YW1wbGVfdXNlciIsImlzcyI6Ind3dy5leGFtcGxlLmNvbSIsInR5cCI6IkpXVCJ9.
		 base64url of hash = EP1gXNeESRH-n57baopfTQ
		 */
		String token = mockToken256.getJwt().serialize(); // this line is here for debugging purposes
		Base64URL expectedHash = new Base64URL("EP1gXNeESRH-n57baopfTQ");
		
		Base64URL resultHash = JWSUtils.getAccessTokenHash(JWSAlgorithm.HS256, mockToken256);
		
		assertEquals(expectedHash, resultHash);
	}
	
}
