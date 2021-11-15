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

package cz.muni.ics.oauth2.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.service.BlacklistedSiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;

/**
 * @author jricher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBlacklistAwareRedirectResolver {

	@Mock
	private BlacklistedSiteService blacklistService;

	@Mock
	private ClientDetailsEntity client;

	@Mock
	private ConfigurationPropertiesBean config;

	@InjectMocks
	private BlacklistAwareRedirectResolver resolver;

	private String blacklistedUri = "https://evil.example.com/";

	private String goodUri = "https://good.example.com/";

	private String pathUri = "https://good.example.com/with/path";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
		when(blacklistService.isBlacklisted(blacklistedUri)).thenReturn(true);

		when(client.getAuthorizedGrantTypes()).thenReturn(ImmutableSet.of("authorization_code"));
		when(client.getRegisteredRedirectUri()).thenReturn(ImmutableSet.of(goodUri, blacklistedUri));
	}

	@Test
	public void testResolveRedirect_safe() {

		// default uses prefix matching, the first one should work fine

		String res1 = resolver.resolveRedirect(goodUri, client);

		assertThat(res1, is(equalTo(goodUri)));
		
		// set the resolver to non-strict and test the path-based redirect resolution
		
		resolver.setStrictMatch(false);

		String res2 = resolver.resolveRedirect(pathUri, client);

		assertThat(res2, is(equalTo(pathUri)));


	}

	@Test(expected = InvalidRequestException.class)
	public void testResolveRedirect_blacklisted() {

		// this should fail with an error
		resolver.resolveRedirect(blacklistedUri, client);

	}

	@Test
	public void testRedirectMatches_default() {

		// this is not an exact match
		boolean res1 = resolver.redirectMatches(pathUri, goodUri, ClientDetailsEntity.AppType.WEB);

		assertThat(res1, is(false));

		// this is an exact match
		boolean res2 = resolver.redirectMatches(goodUri, goodUri, ClientDetailsEntity.AppType.WEB);

		assertThat(res2, is(true));

	}

	@Test
	public void testRedirectMatches_nonstrict() {

		// set the resolver to non-strict match mode
		resolver.setStrictMatch(false);
		
		// this is not an exact match (but that's OK)
		boolean res1 = resolver.redirectMatches(pathUri, goodUri, ClientDetailsEntity.AppType.WEB);

		assertThat(res1, is(true));

		// this is an exact match
		boolean res2 = resolver.redirectMatches(goodUri, goodUri, ClientDetailsEntity.AppType.WEB);

		assertThat(res2, is(true));

	}

	@Test
	public void testHeartMode() {
		// this is not an exact match
		boolean res1 = resolver.redirectMatches(pathUri, goodUri, ClientDetailsEntity.AppType.WEB);

		assertThat(res1, is(false));

		// this is an exact match
		boolean res2 = resolver.redirectMatches(goodUri, goodUri, ClientDetailsEntity.AppType.WEB);

		assertThat(res2, is(true));
	}

}
