/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mitre.jose.keystore.JWKSetKeyStore;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;


/**
 * @author wkim
 * @author tsitkov
 *
 */

public class TestDefaultJwtEncryptionAndDecryptionService {

	private String plainText = "The true sign of intelligence is not knowledge but imagination.";

	private String issuer = "www.example.net";
	private String subject = "example_user";
	private JWTClaimsSet claimsSet = new JWTClaimsSet();

	// Example data taken from Mike Jones's draft-ietf-jose-json-web-encryption-14 appendix examples
	private String compactSerializedJwe = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ." +
			"OKOawDo13gRp2ojaHV7LFpZcgV7T6DVZKTyKOMTYUmKoTCVJRgckCL9kiMT03JGe" +
			"ipsEdY3mx_etLbbWSrFr05kLzcSr4qKAq7YN7e9jwQRb23nfa6c9d-StnImGyFDb" +
			"Sv04uVuxIp5Zms1gNxKKK2Da14B8S4rzVRltdYwam_lDp5XnZAYpQdb76FdIKLaV" +
			"mqgfwX7XWRxv2322i-vDxRfqNzo_tETKzpVLzfiwQyeyPGLBIO56YJ7eObdv0je8" +
			"1860ppamavo35UgoRdbYaBcoh9QcfylQr66oc6vFWXRcZ_ZT2LawVCWTIy3brGPi" +
			"6UklfCpIMfIjf7iGdXKHzg." +
			"48V1_ALb6US04U3b." +
			"5eym8TW_c8SuK0ltJ3rpYIzOeDQz7TALvtu6UG9oMo4vpzs9tX_EFShS8iB7j6ji" +
			"SdiwkIr3ajwQzaBtQD_A." +
			"XFBoMYUZodetZdvTiFvSkQ";

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
					KeyUse.ENCRYPTION, null, JWEAlgorithm.RSA_OAEP, RSAkid, null, null, null);

	private String RSAkid_2 = "rsa3210";
	private JWK RSAjwk_2 = new RSAKey(
			new Base64URL("oahUIoWw0K0usKNuOR6H4wkf4oBUXHTxRvgb48E-BVvxkeDNjbC4he8rUW" +
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
					KeyUse.ENCRYPTION, null, JWEAlgorithm.RSA1_5, RSAkid_2, null, null, null);

	private String AESkid = "aes123";
	private JWK AESjwk = new OctetSequenceKey( new Base64URL("GawgguFyGrWKav7AX4VKUg"),
			KeyUse.ENCRYPTION, null, JWEAlgorithm.A128KW,
			AESkid, null, null, null);
	
	
	private Map<String, JWK> keys = new ImmutableMap.Builder<String, JWK>()
			.put(RSAkid, RSAjwk)
			.build();
	private Map<String, JWK> keys_2 = new ImmutableMap.Builder<String, JWK>()
			.put(RSAkid, RSAjwk)
			.put(RSAkid_2, RSAjwk_2)
			.build();
	private Map<String, JWK> keys_3 = new ImmutableMap.Builder<String, JWK>()
			.put(AESkid, AESjwk)
			.build();
	private Map<String, JWK> keys_4= new ImmutableMap.Builder<String, JWK>()
			.put(RSAkid, RSAjwk)
			.put(RSAkid_2, RSAjwk_2)
			.put(AESkid, AESjwk)
			.build();
	

	private List<JWK> keys_list = new LinkedList<JWK>();

	private DefaultJwtEncryptionAndDecryptionService service;
	private DefaultJwtEncryptionAndDecryptionService service_2;
	private DefaultJwtEncryptionAndDecryptionService service_3;
	private DefaultJwtEncryptionAndDecryptionService service_4;
	private DefaultJwtEncryptionAndDecryptionService service_ks;			
	

	@Before
	public void prepare() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {

		service = new DefaultJwtEncryptionAndDecryptionService(keys);
		service_2 = new DefaultJwtEncryptionAndDecryptionService(keys_2);
		service_3 = new DefaultJwtEncryptionAndDecryptionService(keys_3);
		service_4 = new DefaultJwtEncryptionAndDecryptionService(keys_4);

		claimsSet.setIssuer(issuer);
		claimsSet.setSubject(subject);
		
		// Key Store
		
		keys_list.add(RSAjwk);
		keys_list.add(AESjwk);
		JWKSet jwkSet = new JWKSet(keys_list);
		JWKSetKeyStore keyStore = new JWKSetKeyStore(jwkSet);
		
		service_ks = new DefaultJwtEncryptionAndDecryptionService(keyStore);
	}


	@Test
	public void decrypt_RSA() throws ParseException {
		
		service.setDefaultDecryptionKeyId(RSAkid);
		service.setDefaultEncryptionKeyId(RSAkid);

		JWEObject jwt = JWEObject.parse(compactSerializedJwe);

		assertThat(jwt.getPayload(), nullValue()); // observe..nothing is there

		service.decryptJwt(jwt);
		String result = jwt.getPayload().toString(); // and voila! decrypto-magic

		assertEquals(plainText, result);
	}


	@Test
	public void encryptThenDecrypt_RSA() throws ParseException {

		service.setDefaultDecryptionKeyId(RSAkid);
		service.setDefaultEncryptionKeyId(RSAkid);

		assertEquals(RSAkid,service.getDefaultEncryptionKeyId());
		assertEquals(RSAkid,service.getDefaultDecryptionKeyId());

		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM);

		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet);

		service.encryptJwt(jwt);
		String serialized = jwt.serialize();

		EncryptedJWT encryptedJwt = EncryptedJWT.parse(serialized);
		assertThat(encryptedJwt.getJWTClaimsSet(), nullValue());
		service.decryptJwt(encryptedJwt);

		ReadOnlyJWTClaimsSet resultClaims = encryptedJwt.getJWTClaimsSet();

		assertEquals(claimsSet.getIssuer(), resultClaims.getIssuer());
		assertEquals(claimsSet.getSubject(), resultClaims.getSubject());
	}


	// The same as encryptThenDecrypt_RSA() but relies on the key from the map
	@Test
	public void encryptThenDecrypt_nullID() throws ParseException { 

		service.setDefaultDecryptionKeyId(null);
		service.setDefaultEncryptionKeyId(null);

		assertEquals(RSAkid,service.getDefaultEncryptionKeyId());
		assertEquals(RSAkid,service.getDefaultDecryptionKeyId());

		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM);

		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet);

		service.encryptJwt(jwt);
		String serialized = jwt.serialize();

		EncryptedJWT encryptedJwt = EncryptedJWT.parse(serialized);
		assertThat(encryptedJwt.getJWTClaimsSet(), nullValue());
		service.decryptJwt(encryptedJwt);

		ReadOnlyJWTClaimsSet resultClaims = encryptedJwt.getJWTClaimsSet();

		assertEquals(claimsSet.getIssuer(), resultClaims.getIssuer());
		assertEquals(claimsSet.getSubject(), resultClaims.getSubject());
	}


	@Test(expected=IllegalStateException.class)
	public void encrypt_nullID_oneKey() {

		service_2.setDefaultEncryptionKeyId(null);
		assertEquals(null, service_2.getDefaultEncryptionKeyId());

		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM);

		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet);

		service_2.encryptJwt(jwt);
		assertEquals(null, service_2.getDefaultEncryptionKeyId());
	}

		
	@Test(expected=IllegalStateException.class)
	public void decrypt_nullID() throws ParseException {

		service_2.setDefaultEncryptionKeyId(RSAkid);
		service_2.setDefaultDecryptionKeyId(null);

		assertEquals(RSAkid, service_2.getDefaultEncryptionKeyId());
		assertEquals(null, service_2.getDefaultDecryptionKeyId());

		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM);

		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet);
		service_2.encryptJwt(jwt);
		String serialized = jwt.serialize();

		EncryptedJWT encryptedJwt = EncryptedJWT.parse(serialized);
		assertThat(encryptedJwt.getJWTClaimsSet(), nullValue());

		assertEquals(null, service_2.getDefaultDecryptionKeyId());
		service_2.decryptJwt(encryptedJwt);
	}


	@Test
	public void setThenGetDefAlg() throws ParseException {

		service.setDefaultAlgorithm(JWEAlgorithm.A128KW);
		assertEquals(JWEAlgorithm.A128KW, service.getDefaultAlgorithm());

		service.setDefaultAlgorithm(JWEAlgorithm.RSA_OAEP);
		assertEquals(JWEAlgorithm.RSA_OAEP, service.getDefaultAlgorithm());
	}


	@Test
	public void getAllPubKeys() throws ParseException { 

		Map<String,JWK> keys2check = service_2.getAllPublicKeys();
		assertEquals(
			JSONObjectUtils.getString(RSAjwk.toPublicJWK().toJSONObject(), "e"),
			JSONObjectUtils.getString(keys2check.get(RSAkid).toJSONObject(), "e")
			);
		assertEquals(
			JSONObjectUtils.getString(RSAjwk_2.toPublicJWK().toJSONObject(), "e"),
			JSONObjectUtils.getString(keys2check.get(RSAkid_2).toJSONObject(), "e")
			);	

		assertTrue(service_3.getAllPublicKeys().isEmpty());
	}
	
	
	@Test
	public void getAllCryptoAlgsSupported() throws ParseException { 
		
		assertTrue(service_4.getAllEncryptionAlgsSupported().contains(JWEAlgorithm.RSA_OAEP));
		assertTrue(service_4.getAllEncryptionAlgsSupported().contains(JWEAlgorithm.RSA1_5));
		assertTrue(service_4.getAllEncryptionAlgsSupported().contains(JWEAlgorithm.DIR));
		assertTrue(service_4.getAllEncryptionEncsSupported().contains(EncryptionMethod.A128CBC_HS256));
		assertTrue(service_4.getAllEncryptionEncsSupported().contains(EncryptionMethod.A128GCM));
		assertTrue(service_4.getAllEncryptionEncsSupported().contains(EncryptionMethod.A192CBC_HS384));
		assertTrue(service_4.getAllEncryptionEncsSupported().contains(EncryptionMethod.A192GCM));
		assertTrue(service_4.getAllEncryptionEncsSupported().contains(EncryptionMethod.A256GCM));
		assertTrue(service_4.getAllEncryptionEncsSupported().contains(EncryptionMethod.A256CBC_HS512));

		assertTrue(service_ks.getAllEncryptionAlgsSupported().contains(JWEAlgorithm.RSA_OAEP));
		assertTrue(service_ks.getAllEncryptionAlgsSupported().contains(JWEAlgorithm.RSA1_5));
		assertTrue(service_ks.getAllEncryptionAlgsSupported().contains(JWEAlgorithm.DIR));
		assertTrue(service_ks.getAllEncryptionEncsSupported().contains(EncryptionMethod.A128CBC_HS256));
		assertTrue(service_ks.getAllEncryptionEncsSupported().contains(EncryptionMethod.A128GCM));
		assertTrue(service_ks.getAllEncryptionEncsSupported().contains(EncryptionMethod.A192CBC_HS384));
		assertTrue(service_ks.getAllEncryptionEncsSupported().contains(EncryptionMethod.A192GCM));
		assertTrue(service_ks.getAllEncryptionEncsSupported().contains(EncryptionMethod.A256GCM));
		assertTrue(service_ks.getAllEncryptionEncsSupported().contains(EncryptionMethod.A256CBC_HS512));		
	}
	
	
	@Test
	public void getDefaultCryptoKeyId() throws ParseException {
		
		// Test set/getDefaultEn/DecryptionKeyId

		assertEquals(null, service_4.getDefaultEncryptionKeyId());
		assertEquals(null, service_4.getDefaultDecryptionKeyId());		
		service_4.setDefaultEncryptionKeyId(RSAkid);
		service_4.setDefaultDecryptionKeyId(AESkid);
		assertEquals(RSAkid, service_4.getDefaultEncryptionKeyId());
		assertEquals(AESkid, service_4.getDefaultDecryptionKeyId());
		
		assertEquals(null, service_ks.getDefaultEncryptionKeyId());
		assertEquals(null, service_ks.getDefaultDecryptionKeyId());		
		service_ks.setDefaultEncryptionKeyId(RSAkid);
		service_ks.setDefaultDecryptionKeyId(AESkid);
		assertEquals( RSAkid, service_ks.getDefaultEncryptionKeyId()) ;
		assertEquals(AESkid, service_ks.getDefaultDecryptionKeyId());
	}
}
