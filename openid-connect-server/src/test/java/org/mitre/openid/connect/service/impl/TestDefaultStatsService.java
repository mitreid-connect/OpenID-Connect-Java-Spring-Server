/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.repository.StatsRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

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

	@Mock
	private StatsRepository statsRepository;

	@InjectMocks
	private DefaultStatsService service = new DefaultStatsService();

	/**
	 * Prepares a collection of ApprovedSite mocks to be returned from the approvedSiteService
	 * and a collection of ClientDetailEntity mocks to be returned from the clientService.
	 */
	@Before
	public void prepare() {

		Mockito.reset(statsRepository);

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

		Vector<Object[]> apList = new Vector<>(ImmutableList.of(
				new Object[] { ap1.getClientId(), ap1.getUserId() },
				new Object[] { ap2.getClientId(), ap2.getUserId() },
				new Object[] { ap3.getClientId(), ap3.getUserId() },
				new Object[] { ap4.getClientId(), ap4.getUserId() }
				));
		Vector<Object[]> apCount = new Vector<Object[]> (ImmutableList.of(
				new Object[] { clientId1, 2L },
				new Object[] { clientId2, 1L },
				new Object[] { clientId3, 1L }
				));
		Vector<Object[]> clientList = new Vector<>(ImmutableList.of(
				new Object[] { 1L, clientId1 },
				new Object[] { 2L, clientId2 },
				new Object[] { 3L, clientId3 },
				new Object[] { 4L, clientId4 }
				));

		Mockito.when(statsRepository.getAllApprovedSitesClientIdAndUserId()).thenReturn(apList);
		Mockito.when(statsRepository.getAllApprovedSitesClientIdCount()).thenReturn(apCount);
		Mockito.when(statsRepository.getAllClientIds()).thenReturn(clientList);
	}

	@Test
	public void calculateSummaryStats_empty() {

		Mockito.when(statsRepository.getAllApprovedSitesClientIdAndUserId()).thenReturn(new Vector<Object[]>());

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
	public void calculateByClientId_empty() {

		Mockito.when(statsRepository.getAllApprovedSitesClientIdCount()).thenReturn(new Vector<Object[]>());

		Map<Long, Integer> stats = service.getByClientId();

		assertThat(stats.get(1L), is(0));
		assertThat(stats.get(2L), is(0));
		assertThat(stats.get(3L), is(0));
		assertThat(stats.get(4L), is(0));
	}

	@Test
	public void calculateByClientId() {

		Map<Long, Integer> stats = service.getByClientId();

		assertThat(stats.get(1L), is(2));
		assertThat(stats.get(2L), is(1));
		assertThat(stats.get(3L), is(1));
		assertThat(stats.get(4L), is(0));
	}

	@Test
	public void countForClientId() {

		assertThat(service.getCountForClientId(1L), is(2));
		assertThat(service.getCountForClientId(2L), is(1));
		assertThat(service.getCountForClientId(3L), is(1));
		assertThat(service.getCountForClientId(4L), is(0));
	}

	@Test
	public void cacheAndReset() {

		Map<String, Integer> stats = service.getSummaryStats();

		assertThat(stats.get("approvalCount"), is(4));
		assertThat(stats.get("userCount"), is(2));
		assertThat(stats.get("clientCount"), is(3));

		Vector<Object[]> newList = new Vector<Object[]>(ImmutableList.of(
				new Object[] { ap1.getClientId(), ap1.getUserId() },
				new Object[] { ap2.getClientId(), ap2.getUserId() },
				new Object[] { ap3.getClientId(), ap3.getUserId() },
				new Object[] { ap4.getClientId(), ap4.getUserId() },
				new Object[] { ap5.getClientId(), ap5.getUserId() },
				new Object[] { ap6.getClientId(), ap6.getUserId() }
				));

		Mockito.when(statsRepository.getAllApprovedSitesClientIdAndUserId()).thenReturn(newList);
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
