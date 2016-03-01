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
/**
 *
 */
package org.mitre.openid.connect.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.mitre.openid.connect.repository.StatsRepository;
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
	StatsRepository statsRepository;

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

	private Supplier<Map<Long, Integer>> byClientIdCache = createByClientIdCache();

	private Supplier<Map<Long, Integer>> createByClientIdCache() {
		return Suppliers.memoizeWithExpiration(new Supplier<Map<Long, Integer>>() {
			@Override
			public Map<Long, Integer> get() {
				return computeByClientId();
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
		Vector<Object[]> result = statsRepository.getAllApprovedSitesClientIdAndUserId();

		// process to find number of unique users and sites
		Set<String> userIds = new HashSet<String>();
		Set<String> clientIds = new HashSet<String>();
		for (Object[] approvedSite : result) {
			clientIds.add((String) approvedSite[0]);
			userIds.add((String) approvedSite[1]);
		}

		Map<String, Integer> e = new HashMap<String, Integer>();

		e.put("approvalCount", result.size());
		e.put("userCount", userIds.size());
		e.put("clientCount", clientIds.size());
		return e;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.StatsService#calculateByClientId()
	 */
	@Override
	public Map<Long, Integer> getByClientId() {
		return byClientIdCache.get();
	}

	private Map<Long, Integer> computeByClientId() {
		Map<Long, Integer> counts = getEmptyClientCountMap();
		Map<String, Long> clientIdSurrogateKeyMap = getClientIdSurrogateKeyMap();

		// get all approved sites
		Vector<Object[]> result = statsRepository.getAllApprovedSitesClientIdCount();

		for(Object[] row: result) {
			String clientId = (String) row[0];
			Long id = clientIdSurrogateKeyMap.get(clientId);
			counts.put(id, ((Long) row[1]).intValue());
		}

		return counts;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.StatsService#countForClientId(java.lang.String)
	 */
	@Override
	public Integer getCountForClientId(Long id) {
		Map<Long, Integer> counts = getByClientId();
		return counts.get(id);

	}

	/**
	 * Create a new map of all client ids set to zero
	 * @return
	 */
	private Map<Long, Integer> getEmptyClientCountMap() {
		Map<Long, Integer> counts = new HashMap<>();
		Vector<Object[]> result = statsRepository.getAllClientIds();
		for (Object[] client : result) {
			counts.put((Long) client[0], 0);
		}
		return counts;
	}

	/**
	 * Create a new map mapping clientId with its surrogate key.
	 * @return
	 */
	private Map<String, Long> getClientIdSurrogateKeyMap() {
		Map<String, Long> retMap = new HashMap<>();
		Vector<Object[]> result = statsRepository.getAllClientIds();
		for (Object[] client : result) {
			retMap.put((String) client[1], (Long) client[0]);
		}
		return retMap;
	}

	/**
	 * Reset both stats caches on a trigger (before the timer runs out). Resets the timers.
	 */
	@Override
	public void resetCache() {
		summaryCache = createSummaryCache();
		byClientIdCache = createByClientIdCache();
	}

}
