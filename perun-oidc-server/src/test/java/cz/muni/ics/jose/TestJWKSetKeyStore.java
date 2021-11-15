/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
 *******************************************************************************/
package cz.muni.ics.jose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import cz.muni.ics.jose.keystore.JWKSetKeyStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


/**
 * @author tsitkov
 *
 */

public class TestJWKSetKeyStore {

	private final String RSAkid = "rsa_1";
	private final JWK RSAjwk = new RSAKey(
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
			KeyUse.ENCRYPTION, null, JWEAlgorithm.RSA_OAEP, RSAkid, null, null, null, null, null);

	private final String RSAkid_rsa2 = "rsa_2";
	private final JWK RSAjwk_rsa2 = new RSAKey(
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
			KeyUse.ENCRYPTION, null, JWEAlgorithm.RSA1_5, RSAkid_rsa2, null, null, null, null, null);


	List<JWK> keys_list = new LinkedList<>();
	private JWKSet jwkSet;
	private final String ks_file = "ks.txt";
	private final String ks_file_badJWK = "ks_badJWK.txt";

	@Before
	public void prepare() throws IOException {

		keys_list.add(RSAjwk);
		keys_list.add(RSAjwk_rsa2);
		jwkSet = new JWKSet(keys_list);
		jwkSet.getKeys();

		byte[] jwtbyte = jwkSet.toString().getBytes();
		FileOutputStream out = new FileOutputStream(ks_file);
		out.write(jwtbyte);
		out.close();
	}

	@After
	public void cleanup() throws IOException {

		File f1 = new File(ks_file);
		if (f1.exists()) {
			f1.delete();
		}
		File f2 = new File(ks_file_badJWK);
		if (f2.exists()) {
			f2.delete();
		}
	}

	/* Constructors with no valid Resource setup */
	@Test
	public void ksConstructorTest() {

		JWKSetKeyStore ks = new JWKSetKeyStore(jwkSet);
		assertEquals(ks.getJwkSet(), jwkSet);

		JWKSetKeyStore ks_empty= new JWKSetKeyStore();
		assertNull(ks_empty.getJwkSet());

		boolean thrown = false;
		try {
			new JWKSetKeyStore(null);
		} catch (IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
	}

	/* Misformatted JWK */
	@Test(expected=IllegalArgumentException.class)
	public void ksBadJWKinput() throws IOException {

		byte[] jwtbyte = RSAjwk.toString().getBytes();
		FileOutputStream out = new FileOutputStream(ks_file_badJWK);
		out.write(jwtbyte);
		out.close();

		JWKSetKeyStore ks_badJWK = new JWKSetKeyStore();
		Resource loc = new FileSystemResource(ks_file_badJWK);
		assertTrue(loc.exists());

		ks_badJWK.setLocation(loc);
		assertEquals(loc.getFilename(), ks_file_badJWK);

		ks_badJWK = new JWKSetKeyStore(null);
	}

	/* Empty constructor with valid Resource */
	@Test
	public void ksEmptyConstructorkLoc() {

		JWKSetKeyStore ks = new JWKSetKeyStore();

		File file = new File(ks_file);

		Resource loc = new FileSystemResource(file);
		assertTrue(loc.exists());
		assertTrue(loc.isReadable());

		ks.setLocation(loc);

		assertEquals(loc.getFilename(),ks.getLocation().getFilename());
	}


	@Test
	public void ksSetJwkSet() throws IllegalArgumentException {

		JWKSetKeyStore ks = new JWKSetKeyStore();
		boolean thrown = false;
		try {
			ks.setJwkSet(null);
		} catch (IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);

		ks.setJwkSet(jwkSet);
        assertEquals(ks.getJwkSet(), jwkSet);
	}
}
