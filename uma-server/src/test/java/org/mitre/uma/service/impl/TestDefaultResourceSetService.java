/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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

package org.mitre.uma.service.impl;

import static org.mockito.Matchers.any;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.repository.ResourceSetRepository;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * @author jricher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultResourceSetService {

	@Mock
	private ResourceSetRepository repository;

	@InjectMocks
	private DefaultResourceSetService resourceSetService;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		when(repository.save(any(ResourceSet.class))).then(AdditionalAnswers.returnsFirstArg());

	}

	/**
	 * Test method for {@link org.mitre.uma.service.impl.DefaultResourceSetService#saveNew(org.mitre.uma.model.ResourceSet)}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSaveNew_hasId() {

		ResourceSet rs = new ResourceSet();
		rs.setId(1L);

		resourceSetService.saveNew(rs);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdate_nullId() {
		ResourceSet rs = new ResourceSet();
		rs.setId(1L);

		ResourceSet rs2 = new ResourceSet();

		resourceSetService.update(rs, rs2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdate_nullId2() {
		ResourceSet rs = new ResourceSet();

		ResourceSet rs2 = new ResourceSet();
		rs2.setId(1L);

		resourceSetService.update(rs, rs2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdate_mismatchedIds() {
		ResourceSet rs = new ResourceSet();
		rs.setId(1L);

		ResourceSet rs2 = new ResourceSet();
		rs2.setId(2L);

		resourceSetService.update(rs, rs2);

	}

}
