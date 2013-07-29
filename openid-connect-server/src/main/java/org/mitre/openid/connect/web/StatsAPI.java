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
package org.mitre.openid.connect.web;

import java.util.Map;

import org.mitre.openid.connect.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ROLE_USER')")
@RequestMapping("/api/stats")
public class StatsAPI {

	@Autowired
	private StatsService statsService;

	@RequestMapping(value = "summary", produces = "application/json")
	public String statsSummary(ModelMap m) {

		Map<String, Integer> e = statsService.calculateSummaryStats();

		m.put("entity", e);

		return "jsonEntityView";

	}

	@RequestMapping(value = "byclientid", produces = "application/json")
	public String statsByClient(ModelMap m) {
		Map<Long, Integer> e = statsService.calculateByClientId();

		m.put("entity", e);

		return "jsonEntityView";
	}

	@RequestMapping(value = "byclientid/{id}", produces = "application/json")
	public String statsByClientId(@PathVariable("id") Long id, ModelMap m) {
		Integer e = statsService.countForClientId(id);

		m.put("entity", e);

		return "jsonEntityView";
	}

}
