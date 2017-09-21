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
package org.mitre.openid.connect.web;

import java.util.Map;

import org.mitre.openid.connect.model.ClientStat;
import org.mitre.openid.connect.service.StatsService;
import org.mitre.openid.connect.view.JsonEntityView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/" + StatsAPI.URL)
public class StatsAPI {

	public static final String URL = RootController.API_URL + "/stats";

	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(StatsAPI.class);

	@Autowired
	private StatsService statsService;

	@RequestMapping(value = "summary", produces = MediaType.APPLICATION_JSON_VALUE)
	public String statsSummary(ModelMap m) {

		Map<String, Integer> e = statsService.getSummaryStats();

		m.put(JsonEntityView.ENTITY, e);

		return JsonEntityView.VIEWNAME;

	}

	//	@PreAuthorize("hasRole('ROLE_USER')")
	//	@RequestMapping(value = "byclientid", produces = MediaType.APPLICATION_JSON_VALUE)
	//	public String statsByClient(ModelMap m) {
	//		Map<Long, Integer> e = statsService.getByClientId();
	//
	//		m.put(JsonEntityView.ENTITY, e);
	//
	//		return JsonEntityView.VIEWNAME;
	//	}
	//
	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = "byclientid/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String statsByClientId(@PathVariable("id") String clientId, ModelMap m) {
		ClientStat e = statsService.getCountForClientId(clientId);

		m.put(JsonEntityView.ENTITY, e);

		return JsonEntityView.VIEWNAME;
	}

}
