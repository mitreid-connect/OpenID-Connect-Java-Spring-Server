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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import cz.muni.ics.openid.connect.model.WhitelistedSite;
import cz.muni.ics.openid.connect.repository.WhitelistedSiteRepository;
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
public class TestDefaultWhitelistedSiteService {

	@Mock
	private WhitelistedSiteRepository repository;

	@InjectMocks
	private DefaultWhitelistedSiteService service;

	@Before
	public void prepare() {
		Mockito.reset(repository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void saveNew_notNullId() {

		WhitelistedSite site = Mockito.mock(WhitelistedSite.class);
		Mockito.when(site.getId()).thenReturn(12345L); // arbitrary long value

		service.saveNew(site);
	}

	@Test
	public void saveNew_success() {
		WhitelistedSite site = Mockito.mock(WhitelistedSite.class);
		Mockito.when(site.getId()).thenReturn(null);

		service.saveNew(site);

		Mockito.verify(repository).save(site);
	}

	@Test
	public void update_nullSites() {

		WhitelistedSite oldSite = Mockito.mock(WhitelistedSite.class);
		WhitelistedSite newSite = Mockito.mock(WhitelistedSite.class);

		// old client null
		try {
			service.update(null, newSite);
			fail("Old site input is null. Expected a IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}

		// new client null
		try {
			service.update(oldSite, null);
			fail("New site input is null. Expected a IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}

		// both clients null
		try {
			service.update(null, null);
			fail("Both site inputs are null. Expected a IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertThat(e, is(notNullValue()));
		}
	}

	@Test
	public void update_success() {

		WhitelistedSite oldSite = Mockito.mock(WhitelistedSite.class);
		WhitelistedSite newSite = Mockito.mock(WhitelistedSite.class);

		service.update(oldSite, newSite);

		Mockito.verify(repository).update(oldSite, newSite);
	}
}
