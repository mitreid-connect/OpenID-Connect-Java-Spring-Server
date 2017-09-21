/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

/**
 * @author wkim
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultStatsService {

	// Test fixtures:
	// Currently tests 4 approved sites with a total of 2 users and 3 clients for those sites.
	// There is an extra client in the system to make sure the stats only count for approved sites.

	private String userId1 = "batman";
	private String userId2 = "alfred";

	private String clientId1 = "bar";
	private String clientId2 = "pawnshop";
	private String clientId3 = "pizzastore";
	private String clientId4 = "gasstation";

	private ApprovedSite ap1 = Mockito.mock(ApprovedSite.class);
	private ApprovedSite ap2 = Mockito.mock(ApprovedSite.class);
	private ApprovedSite ap3 = Mockito.mock(ApprovedSite.class);
	private ApprovedSite ap4 = Mockito.mock(ApprovedSite.class);
	private ApprovedSite ap5 = Mockito.mock(ApprovedSite.class);
	private ApprovedSite ap6 = Mockito.mock(ApprovedSite.class);

	private ClientDetailsEntity client1 = Mockito.mock(ClientDetailsEntity.class);
	private ClientDetailsEntity client2 = Mockito.mock(ClientDetailsEntity.class);
	private ClientDetailsEntity client3 = Mockito.mock(ClientDetailsEntity.class);
	private ClientDetailsEntity client4 = Mockito.mock(ClientDetailsEntity.class);

	@Mock
	private ApprovedSiteService approvedSiteService;

	@InjectMocks
	private DefaultStatsService service = new DefaultStatsService();

	/**
	 * Prepares a collection of ApprovedSite mocks to be returned from the approvedSiteService
	 * and a collection of ClientDetailEntity mocks to be returned from the clientService.
	 */
	@Before
	public void prepare() {

		Mockito.reset(approvedSiteService);

		Mockito.when(ap1.getUserId()).thenReturn(userId1);
		Mockito.when(ap1.getClientId()).thenReturn(clientId1);

		Mockito.when(ap2.getUserId()).thenReturn(userId1);
		Mockito.when(ap2.getClientId()).thenReturn(clientId1);

		Mockito.when(ap3.getUserId()).thenReturn(userId2);
		Mockito.when(ap3.getClientId()).thenReturn(clientId2);

		Mockito.when(ap4.getUserId()).thenReturn(userId2);
		Mockito.when(ap4.getClientId()).thenReturn(clientId3);

		Mockito.when(ap5.getUserId()).thenReturn(userId2);
		Mockito.when(ap5.getClientId()).thenReturn(clientId1);

		Mockito.when(ap6.getUserId()).thenReturn(userId1);
		Mockito.when(ap6.getClientId()).thenReturn(clientId4);

		Mockito.when(approvedSiteService.getAll()).thenReturn(Sets.newHashSet(ap1, ap2, ap3, ap4));

		Mockito.when(client1.getId()).thenReturn(1L);
		Mockito.when(client2.getId()).thenReturn(2L);
		Mockito.when(client3.getId()).thenReturn(3L);
		Mockito.when(client4.getId()).thenReturn(4L);

		Mockito.when(approvedSiteService.getByClientId(clientId1)).thenReturn(Sets.newHashSet(ap1, ap2));
		Mockito.when(approvedSiteService.getByClientId(clientId2)).thenReturn(Sets.newHashSet(ap3));
		Mockito.when(approvedSiteService.getByClientId(clientId3)).thenReturn(Sets.newHashSet(ap4));
		Mockito.when(approvedSiteService.getByClientId(clientId4)).thenReturn(Sets.newHashSet());
	}

	@Test
	public void calculateSummaryStats_empty() {

		Mockito.when(approvedSiteService.getAll()).thenReturn(new HashSet<ApprovedSite>());

		Map<String, Integer> stats = service.getSummaryStats();

		assertThat(stats.get("approvalCount"), is(0));
		assertThat(stats.get("userCount"), is(0));
		assertThat(stats.get("clientCount"), is(0));
	}

	@Test
	public void calculateSummaryStats() {
		Map<String, Integer> stats = service.getSummaryStats();

		assertThat(stats.get("approvalCount"), is(4));
		assertThat(stats.get("userCount"), is(2));
		assertThat(stats.get("clientCount"), is(3));
	}

	@Test
	public void countForClientId() {
		// stats for ap1..ap4
		assertThat(service.getCountForClientId(clientId1).getApprovedSiteCount(), is(2));
		assertThat(service.getCountForClientId(clientId2).getApprovedSiteCount(), is(1));
		assertThat(service.getCountForClientId(clientId3).getApprovedSiteCount(), is(1));
		assertThat(service.getCountForClientId(clientId4).getApprovedSiteCount(), is(0));
	}

	@Test
	public void cacheAndReset() {

		Map<String, Integer> stats = service.getSummaryStats();

		assertThat(stats.get("approvalCount"), is(4));
		assertThat(stats.get("userCount"), is(2));
		assertThat(stats.get("clientCount"), is(3));

		Mockito.when(approvedSiteService.getAll()).thenReturn(Sets.newHashSet(ap1, ap2, ap3, ap4, ap5, ap6));

		Map<String, Integer> stats2 = service.getSummaryStats();

		// cache should remain the same due to memoized functions
		assertThat(stats2.get("approvalCount"), is(4));
		assertThat(stats2.get("userCount"), is(2));
		assertThat(stats2.get("clientCount"), is(3));

		// reset the cache and make sure the count goes up
		service.resetCache();

		Map<String, Integer> stats3 = service.getSummaryStats();

		assertThat(stats3.get("approvalCount"), is(6));
		assertThat(stats3.get("userCount"), is(2));
		assertThat(stats3.get("clientCount"), is(4));

	}
}
