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

import static org.junit.Assert.fail;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.Use;
import com.nimbusds.jose.util.Base64URL;

/**
 * @author wkim
 *
 */
public class TestDefaultJwtEncryptionAndDecryptionService {

	private String kid = "abc123";
	
	private JWK jwk = new OctetSequenceKey(new Base64URL("GawgguFyGrWKav7AX4VKUg"), Use.ENCRYPTION, JWEAlgorithm.A128KW, kid);
	
	private Map<String, JWK> keys = new ImmutableMap.Builder<String, JWK>().put(kid, jwk).build();
	
	private DefaultJwtEncryptionAndDecryptionService service;
	
	@Before
	public void prepare() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
		
		service = new DefaultJwtEncryptionAndDecryptionService(keys);
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
