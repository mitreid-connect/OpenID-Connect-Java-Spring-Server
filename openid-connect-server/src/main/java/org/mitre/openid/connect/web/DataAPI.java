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

import java.io.IOException;
import java.io.Reader;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.service.MITREidDataService;
import org.mitre.openid.connect.service.impl.MITREidDataService_1_3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * API endpoint for importing and exporting the current state of a server.
 * Includes all tokens, grants, whitelists, blacklists, and clients.
 *
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + DataAPI.URL)
@PreAuthorize("hasRole('ROLE_ADMIN')") // you need to be an admin to even think about this -- this is a potentially dangerous API!!
public class DataAPI {

	public static final String URL = RootController.API_URL + "/data";

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(DataAPI.class);

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Autowired
	private ConfigurationPropertiesBean config;

	@Autowired
	private List<MITREidDataService> importers;

	private List<String> supportedVersions = ImmutableList.of(
			MITREidDataService.MITREID_CONNECT_1_0,
			MITREidDataService.MITREID_CONNECT_1_1,
			MITREidDataService.MITREID_CONNECT_1_2,
			MITREidDataService.MITREID_CONNECT_1_3);

	@Autowired
	private MITREidDataService_1_3 exporter;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String importData(Reader in, Model m) throws IOException {

		JsonReader reader = new JsonReader(in);

		reader.beginObject();

		while (reader.hasNext()) {
			JsonToken tok = reader.peek();
			switch (tok) {
				case NAME:
					String name = reader.nextName();

					if (supportedVersions.contains(name)) {
						// we're working with a known data version tag
						for (MITREidDataService dataService : importers) {
							// dispatch to the correct service
							if (dataService.supportsVersion(name)) {
								dataService.importData(reader);
								break;
							}
						}
					} else {
						// consume the next bit silently for now
						logger.debug("Skipping value for " + name); // TODO: write these out?
						reader.skipValue();
					}
					break;
				case END_OBJECT:
					break;
				case END_DOCUMENT:
					break;
			}
		}

		reader.endObject();

		return "httpCodeView";
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void exportData(HttpServletResponse resp, Principal prin) throws IOException {

		resp.setContentType(MediaType.APPLICATION_JSON_VALUE);

		// this writer puts things out onto the wire
		JsonWriter writer = new JsonWriter(resp.getWriter());
		writer.setIndent("  ");

		try {

			writer.beginObject();

			writer.name("exported-at");
			writer.value(dateFormat.format(new Date()));

			writer.name("exported-from");
			writer.value(config.getIssuer());

			writer.name("exported-by");
			writer.value(prin.getName());

			// delegate to the service to do the actual export
			exporter.exportData(writer);

			writer.endObject(); // end root
			writer.close();

		} catch (IOException e) {
			logger.error("Unable to export data", e);
		}
	}

}