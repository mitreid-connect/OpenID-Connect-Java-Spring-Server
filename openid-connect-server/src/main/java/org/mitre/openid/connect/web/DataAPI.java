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
package org.mitre.openid.connect.web;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.service.MITREidDataService;
import org.mitre.openid.connect.service.impl.MITREidDataService_1_0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
@RequestMapping("/api/data")
@PreAuthorize("hasRole('ROLE_ADMIN')") // you need to be an admin to even think about this -- this is a potentially dangerous API!!
public class DataAPI {
	
	private static Logger logger = LoggerFactory.getLogger(DataAPI.class);

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Autowired
	private ConfigurationPropertiesBean config;
	
	private MITREidDataService dataService_1_0 = new MITREidDataService_1_0();
	
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json")
	public String importData(Reader in, Model m) throws IOException {
		
		JsonReader reader = new JsonReader(in);
		
		reader.beginObject();
		
		while (reader.hasNext()) {
			JsonToken tok = reader.peek();
			switch (tok) {
				case NAME:
					String name = reader.nextName();
					if (name.equals(MITREidDataService.MITREID_CONNECT_1_0)) {
						// this will consume the beginObject and endObject of the configuration value
						dataService_1_0.importData(reader);
					} else {
						// consume the next bit silently for now
						logger.debug("Skipping value for " + name); // TODO: write these out?
						reader.skipValue();
					}
					break;
				case END_OBJECT:
					reader.endObject();
					break;
				case END_DOCUMENT:
					break;
			}
		}		
		
		return "httpCodeView"; 
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public void exportData(HttpServletResponse resp, Principal prin) throws IOException {

		resp.setContentType("application/json");
		
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
			dataService_1_0.exportData(writer);
			
			writer.endObject(); // end root
			writer.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
