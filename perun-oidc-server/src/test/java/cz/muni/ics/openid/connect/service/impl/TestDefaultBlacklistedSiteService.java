/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
package cz.muni.ics.openid.connect.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

import com.google.common.collect.Sets;
import cz.muni.ics.openid.connect.model.BlacklistedSite;
import cz.muni.ics.openid.connect.repository.BlacklistedSiteRepository;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultBlacklistedSiteService {

	private BlacklistedSite site1;
	private BlacklistedSite site2;

	private String uri1 = "black1";
	private String uri2 = "black2";
	private String uri3 = "not-black";

	private Set<BlacklistedSite> blackListedSitesSet;

	@Mock
	private BlacklistedSiteRepository mockRepository;

	@InjectMocks
	private DefaultBlacklistedSiteService service = new DefaultBlacklistedSiteService();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void prepare() throws Exception {

		site1 = new BlacklistedSite();
		site2 = new BlacklistedSite();

		site1.setUri(uri1);
		site2.setUri(uri2);

		blackListedSitesSet = Sets.newHashSet(site1, site2);
	}

	/**
	 * Test finding blacklisted sites from the repository.
	 */
	@Test
	public void isBlacklisted_yes() {

		Mockito.when(mockRepository.getAll()).thenReturn(blackListedSitesSet);

		assertTrue(service.isBlacklisted(uri1));
		assertTrue(service.isBlacklisted(uri2));

		Mockito.verify(mockRepository, times(2)).getAll();
	}

	/**
	 * Tests for finding a site that is not blacklisted in the repository.
	 */
	@Test
	public void isBlacklisted_no() {

		Mockito.when(mockRepository.getAll()).thenReturn(blackListedSitesSet);

		assertFalse(service.isBlacklisted(uri3));

		Mockito.verify(mockRepository).getAll();
	}

}
