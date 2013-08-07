/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.jwt.encryption.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.Use;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;

/**
 * @author wkim
 *
 */
public class TestDefaultJwtEncryptionAndDecryptionService {

	private String issuer = "www.example.net";
	private String subject = "example_user";
	
	private JWTClaimsSet claimsSet = new JWTClaimsSet();
	
	// Example keys taken from Mike Jones's draft-ietf-jose-json-web-encryption-14 appendix examples
	private String RSAkid = "rsa321";
	private JWK RSAjwk = new RSAKey(new Base64URL("oahUIoWw0K0usKNuOR6H4wkf4oBUXHTxRvgb48E-BVvxkeDNjbC4he8rUW" +
			"cJoZmds2h7M70imEVhRU5djINXtqllXI4DFqcI1DgjT9LewND8MW2Krf3S" +
			"psk_ZkoFnilakGygTwpZ3uesH-PFABNIUYpOiN15dsQRkgr0vEhxN92i2a" +
			"sbOenSZeyaxziK72UwxrrKoExv6kc5twXTq4h-QChLOln0_mtUZwfsRaMS" +
			"tPs6mS6XrgxnxbWhojf663tuEQueGC-FCMfra36C9knDFGzKsNa7LZK2dj" +
			"YgyD3JR_MB_4NUJW_TqOQtwHYbxevoJArm-L5StowjzGy-_bq6Gw"), // n
			new Base64URL("AQAB"), // e
			new Base64URL("kLdtIj6GbDks_ApCSTYQtelcNttlKiOyPzMrXHeI-yk1F7-kpDxY4-WY5N" +
			"WV5KntaEeXS1j82E375xxhWMHXyvjYecPT9fpwR_M9gV8n9Hrh2anTpTD9" +
			"3Dt62ypW3yDsJzBnTnrYu1iwWRgBKrEYY46qAZIrA2xAwnm2X7uGR1hghk" +
			"qDp0Vqj3kbSCz1XyfCs6_LehBwtxHIyh8Ripy40p24moOAbgxVw3rxT_vl" +
			"t3UVe4WO3JkJOzlpUf-KTVI2Ptgm-dARxTEtE-id-4OJr0h-K-VFs3VSnd" +
			"VTIznSxfyrj8ILL6MG_Uv8YAu7VILSB3lOW085-4qE3DzgrTjgyQ"), // d
			Use.ENCRYPTION, JWEAlgorithm.RSA_OAEP, RSAkid);
	
	// AES key wrap not yet tested
//	private String AESkid = "aes123";
//	private JWK AESjwk = new OctetSequenceKey(new Base64URL("GawgguFyGrWKav7AX4VKUg"), Use.ENCRYPTION, JWEAlgorithm.A128KW, AESkid);
//	
//	private Map<String, JWK> keys = new ImmutableMap.Builder<String, JWK>().
//			put(RSAkid, RSAjwk).put(AESkid, AESjwk).build();
	
	private Map<String, JWK> keys = new ImmutableMap.Builder<String, JWK>().
			put(RSAkid, RSAjwk).build();
	
	private DefaultJwtEncryptionAndDecryptionService service;
	
	@Before
	public void prepare() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
		
		service = new DefaultJwtEncryptionAndDecryptionService(keys);
		
		claimsSet.setIssuer(issuer);
		claimsSet.setSubject(subject);
	}
	
	@Test
	public void encryptThenDecrypt_RSA() throws ParseException {
		
		service.setDefaultDecryptionKeyId(RSAkid);
		service.setDefaultEncryptionKeyId(RSAkid);
		
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM);
		
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet);
		
		service.encryptJwt(jwt);
		// TODO test intermediate crypto parts?
		service.decryptJwt(jwt);
		
		ReadOnlyJWTClaimsSet resultClaims = jwt.getJWTClaimsSet();
		
		assertEquals(claimsSet.getIssuer(), resultClaims.getIssuer());
		assertEquals(claimsSet.getSubject(), resultClaims.getSubject());
	}

}
