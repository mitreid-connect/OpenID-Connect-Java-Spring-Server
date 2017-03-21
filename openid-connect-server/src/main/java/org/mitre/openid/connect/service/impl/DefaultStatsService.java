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
/**
 *
 */
package org.mitre.openid.connect.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.ClientStat;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * @author jricher
 *
 */
@Service
public class DefaultStatsService implements StatsService {

	@Autowired
	private ApprovedSiteService approvedSiteService;

	// stats cache
	private Supplier<Map<String, Integer>> summaryCache = createSummaryCache();

	private Supplier<Map<String, Integer>> createSummaryCache() {
		return Suppliers.memoizeWithExpiration(new Supplier<Map<String, Integer>>() {
			@Override
			public Map<String, Integer> get() {
				return computeSummaryStats();
			}

		}, 10, TimeUnit.MINUTES);
	}

	@Override
	public Map<String, Integer> getSummaryStats() {
		return summaryCache.get();
	}

	// do the actual computation
	private Map<String, Integer> computeSummaryStats() {
		// get all approved sites
		Collection<ApprovedSite> allSites = approvedSiteService.getAll();

		// process to find number of unique users and sites
		Set<String> userIds = new HashSet<>();
		Set<String> clientIds = new HashSet<>();
		for (ApprovedSite approvedSite : allSites) {
			userIds.add(approvedSite.getUserId());
			clientIds.add(approvedSite.getClientId());
		}

		Map<String, Integer> e = new HashMap<>();

		e.put("approvalCount", allSites.size());
		e.put("userCount", userIds.size());
		e.put("clientCount", clientIds.size());
		return e;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.StatsService#countForClientId(java.lang.String)
	 */
	@Override
	public ClientStat getCountForClientId(String clientId) {

		Collection<ApprovedSite> approvedSites = approvedSiteService.getByClientId(clientId);

		ClientStat stat = new ClientStat();
		stat.setApprovedSiteCount(approvedSites.size());

		return stat;
	}

	/**
	 * Reset both stats caches on a trigger (before the timer runs out). Resets the timers.
	 */
	@Override
	public void resetCache() {
		summaryCache = createSummaryCache();
	}

}
