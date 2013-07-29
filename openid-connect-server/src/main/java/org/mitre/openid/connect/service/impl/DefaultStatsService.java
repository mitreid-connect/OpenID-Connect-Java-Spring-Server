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
/**
 * 
 */
package org.mitre.openid.connect.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * @author jricher
 *
 */
@Service
public class DefaultStatsService implements StatsService {

	@Autowired
	private ApprovedSiteService approvedSiteService;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Override
	public Map<String, Integer> calculateSummaryStats() {
		// get all approved sites
		Collection<ApprovedSite> allSites = approvedSiteService.getAll();

		// process to find number of unique users and sites
		Set<String> userIds = new HashSet<String>();
		Set<String> clientIds = new HashSet<String>();
		for (ApprovedSite approvedSite : allSites) {
			userIds.add(approvedSite.getUserId());
			clientIds.add(approvedSite.getClientId());
		}

		Map<String, Integer> e = new HashMap<String, Integer>();

		e.put("approvalCount", allSites.size());
		e.put("userCount", userIds.size());
		e.put("clientCount", clientIds.size());
		return e;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.StatsService#calculateByClientId()
	 */
	@Override
	public Map<Long, Integer> calculateByClientId() {
		// get all approved sites
		Collection<ApprovedSite> allSites = approvedSiteService.getAll();

		Multiset<String> clientIds = HashMultiset.create();
		for (ApprovedSite approvedSite : allSites) {
			clientIds.add(approvedSite.getClientId());
		}

		Map<Long, Integer> counts = getEmptyClientCountMap();
		for (String clientId : clientIds) {
			ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
			counts.put(client.getId(), clientIds.count(clientId));
		}

		return counts;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.StatsService#countForClientId(java.lang.String)
	 */
	@Override
	public Integer countForClientId(Long id) {

		Map<Long, Integer> counts = calculateByClientId();
		return counts.get(id);

	}

	/**
	 * Create a new map of all client ids set to zero
	 * @return
	 */
	private Map<Long, Integer> getEmptyClientCountMap() {
		Map<Long, Integer> counts = new HashMap<Long, Integer>();
		Collection<ClientDetailsEntity> clients = clientService.getAllClients();
		for (ClientDetailsEntity client : clients) {
			counts.put(client.getId(), 0);
		}

		return counts;
	}

}
