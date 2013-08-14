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
package org.mitre.openid.connect.service.impl;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.model.Nonce;
import org.mitre.openid.connect.repository.NonceRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultNonceService {

	// Test value for nonce storage duration time.
	private Period nonceStorageDuration = new Period().withSeconds(1);

	private String clientId = "123abc";
	private String value1 = "n1";
	private String value2 = "n2";
	private String value3 = "n3";

	@Mock
	private NonceRepository repository;

	@InjectMocks
	private DefaultNonceService service = new DefaultNonceService();

	@Before
	public void prepare() {

		Mockito.reset(repository);

		service.setNonceStorageDuration(nonceStorageDuration);


	}

	/**
	 * Tests the correctness of created nonces.
	 */
	@Test
	public void create() {

		Date start = new Date(System.currentTimeMillis() - 100); // time skew of 100ms on either side

		Nonce nonce = service.create(clientId, value1);

		Date end = new Date(System.currentTimeMillis() + 100); // time skew of 100ms on either side

		assertEquals(clientId, nonce.getClientId());
		assertEquals(value1, nonce.getValue());
		assertTrue(nonce.getUseDate().after(start) && nonce.getUseDate().before(end)); // make sure the date is within the right range (within 100ms on either side)

		// Check expiration date.
		assertEquals(new DateTime(nonce.getUseDate()).plus(nonceStorageDuration), new DateTime(nonce.getExpireDate()));
	}

	/**
	 * Verifies that if any nonce returned by the repository already has the value being checked for,
	 * then the service method returns true.
	 * 
	 * Also checks a nonce value not returned by the repository to verify the method returns false in this case.
	 */
	@Test
	public void alreadyUsed() {

		Nonce nonce1 = service.create(clientId, value1);
		Nonce nonce2 = service.create(clientId, value2);

		Mockito.when(repository.getByClientId(clientId)).thenReturn(Sets.newHashSet(nonce1, nonce2));

		assertTrue(service.alreadyUsed(clientId, value1));
		assertTrue(service.alreadyUsed(clientId, value2));

		assertFalse(service.alreadyUsed(clientId, value3));
	}
}
