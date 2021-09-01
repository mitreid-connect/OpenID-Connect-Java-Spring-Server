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
package org.mitre.openid.connect.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultApprovedSiteService {

	private ApprovedSite site1;
	private ApprovedSite site2;
	private ApprovedSite site3;

	private ClientDetailsEntity client;
	private final String clientId = "client";

	@Mock
	private ApprovedSiteRepository repository;

	@Mock
	private OAuth2TokenRepository tokenRepository;

	@InjectMocks
	private ApprovedSiteService service = new DefaultApprovedSiteService();


	/**
	 * Initialize the service and repository mock. Initialize a client and
	 * several ApprovedSite objects for use in unit tests.
	 */
	@Before
	public void prepare() {

		client = new ClientDetailsEntity();
		client.setClientId(clientId);

		site1 = new ApprovedSite();
		site1.setId(1L);
		site1.setUserId("user1");
		site1.setClientId("other");

		site2 = new ApprovedSite();
		site2.setId(2L);
		site2.setUserId("user1");
		site2.setClientId(clientId);

		site3 = new ApprovedSite();
		site3.setId(3L);
		site3.setUserId("user2");
		site3.setClientId(clientId);

		Mockito.reset(repository);

	}

	/**
	 * Test clearing approved sites for a client that has 2 stored approved sites.
	 * Ensure that the repository's remove() method is called twice.
	 */
	@Test
	public void clearApprovedSitesForClient_success() {
		Set<ApprovedSite> setToReturn = Sets.newHashSet(site2, site3);
		Mockito.when(repository.getByClientId(client.getClientId())).thenReturn(setToReturn);
		List<OAuth2AccessTokenEntity> tokens = ImmutableList.of();
		Mockito.when(tokenRepository.getAccessTokensForApprovedSite(any(ApprovedSite.class))).thenReturn(tokens);

		service.clearApprovedSitesForClient(client);

		Mockito.verify(repository, times(2)).remove(any(ApprovedSite.class));
	}

	/**
	 * Test clearing approved sites for a client that doesn't have any stored approved
	 * sites. Ensure that the repository's remove() method is never called in this case.
	 */
	@Test
	@Rollback
	public void clearApprovedSitesForClient_null() {
		String otherId = "a different id";
		client.setClientId(otherId);
		service.clearApprovedSitesForClient(client);
		Mockito.verify(repository, never()).remove(any(ApprovedSite.class));
	}


}
