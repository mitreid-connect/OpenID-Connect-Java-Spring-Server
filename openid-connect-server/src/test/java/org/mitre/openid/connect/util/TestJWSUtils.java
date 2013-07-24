package org.mitre.openid.connect.util;


import net.minidev.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;

@RunWith(MockitoJUnitRunner.class)
public class TestJWSUtils {
	
	@Before
	public void prepare() {
		
	}
	
	@Test
	public void compute_hs256_at_hash() {
		JWTClaimsSet jwt = new JWTClaimsSet();
		jwt.setType("JWT");
		jwt.setClaim("alg", "HS256");
		JSONObject jwtObj = jwt.toJSONObject();
		String jwtString = jwtObj.toJSONString();
		byte[] jwtBytes = jwtString.getBytes();
		
		Base64URL signedJwt = JWSUtils.getHash(JWSAlgorithm.HS256, jwtBytes);
		
		
	}
	
	
	
}
