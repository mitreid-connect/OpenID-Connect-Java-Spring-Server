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
/**
 * 
 */
package org.mitre.jose;

import org.junit.Test;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

import static org.junit.Assert.assertEquals;

/**
 * 
 * These tests make sure that the algorithm name processing
 * is functional on the three embedded JOSE classes.
 * 
 * @author jricher
 *
 */
public class JOSEEmbedTest {

	@Test
	public void testJWSAlgorithmEmbed() {
		JWSAlgorithmEmbed a = new JWSAlgorithmEmbed(JWSAlgorithm.HS256);

		assertEquals(JWSAlgorithm.HS256, a.getAlgorithm());
		assertEquals("HS256", a.getAlgorithmName());
	}

	@Test
	public void testJWSAlgorithmEmbedGetForAlgoirthmName() {
		JWSAlgorithmEmbed a = JWSAlgorithmEmbed.getForAlgorithmName("RS256");

		assertEquals(JWSAlgorithm.RS256, a.getAlgorithm());
		assertEquals("RS256", a.getAlgorithmName());
	}

	@Test
	public void testJWEAlgorithmEmbed() {
		JWEAlgorithmEmbed a = new JWEAlgorithmEmbed(JWEAlgorithm.A128KW);

		assertEquals(JWEAlgorithm.A128KW, a.getAlgorithm());
		assertEquals("A128KW", a.getAlgorithmName());
	}

	@Test
	public void testJWEAlgorithmEmbedGetForAlgoirthmName() {
		JWEAlgorithmEmbed a = JWEAlgorithmEmbed.getForAlgorithmName("RSA1_5");

		assertEquals(JWEAlgorithm.RSA1_5, a.getAlgorithm());
		assertEquals("RSA1_5", a.getAlgorithmName());
	}

	@Test
	public void testJWEEncryptionMethodEmbed() {
		JWEEncryptionMethodEmbed a = new JWEEncryptionMethodEmbed(EncryptionMethod.A128CBC_HS256);

		assertEquals(EncryptionMethod.A128CBC_HS256, a.getAlgorithm());
		assertEquals("A128CBC-HS256", a.getAlgorithmName());
	}

	@Test
	public void testJWEEncryptionMethodEmbedGetForAlgoirthmName() {
		JWEEncryptionMethodEmbed a = JWEEncryptionMethodEmbed.getForAlgorithmName("A256GCM");

		assertEquals(EncryptionMethod.A256GCM, a.getAlgorithm());
		assertEquals("A256GCM", a.getAlgorithmName());
	}

}
