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
package org.mitre.openid.connect.client.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.RegisteredClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author jricher
 *
 */
public class JsonFileRegisteredClientService implements RegisteredClientService {

	private static Logger logger = LoggerFactory.getLogger(JsonFileRegisteredClientService.class);

	private Gson gson = new GsonBuilder()
	.registerTypeAdapter(RegisteredClient.class, new JsonSerializer<RegisteredClient>() {
		@Override
		public JsonElement serialize(RegisteredClient src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("token", src.getRegistrationAccessToken());
			obj.addProperty("uri", src.getRegistrationClientUri());
			if (src.getClientIdIssuedAt() != null) {
				obj.addProperty("issued", src.getClientIdIssuedAt().getTime());
			}
			if (src.getClientSecretExpiresAt() != null) {
				obj.addProperty("expires", src.getClientSecretExpiresAt().getTime());
			}
			return obj;
		}
	})
	.registerTypeAdapter(RegisteredClient.class, new JsonDeserializer<RegisteredClient>() {
		@Override
		public RegisteredClient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonObject()) {
				JsonObject src = json.getAsJsonObject();
				RegisteredClient rc = new RegisteredClient();
				rc.setRegistrationAccessToken(src.get("token").getAsString());
				rc.setRegistrationClientUri(src.get("uri").getAsString());
				if (src.has("issued") && !src.get("issued").isJsonNull()) {
					rc.setClientIdIssuedAt(new Date(src.get("issued").getAsLong()));
				}
				if (src.has("expires") && !src.get("expires").isJsonNull()) {
					rc.setClientSecretExpiresAt(new Date(src.get("expires").getAsLong()));
				}
				return rc;
			} else {
				return null;
			}
		}
	})
	.create();

	private File file;

	private Map<String, RegisteredClient> clients = new HashMap<String, RegisteredClient>();

	public JsonFileRegisteredClientService(String filename) {
		this.file = new File(filename);
		load();
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.RegisteredClientService#getByIssuer(java.lang.String)
	 */
	@Override
	public RegisteredClient getByIssuer(String issuer) {
		return clients.get(issuer);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.RegisteredClientService#save(java.lang.String, org.mitre.oauth2.model.RegisteredClient)
	 */
	@Override
	public void save(String issuer, RegisteredClient client) {
		clients.put(issuer, client);
		write();
	}

	/**
	 * Sync the map of clients out to disk.
	 */
	private void write() {
		try {
			if (!file.exists()) {
				// create a new file
				logger.info("Creating saved clients list in " + file);
				file.createNewFile();
			}
			FileWriter out = new FileWriter(file);

			gson.toJson(clients, new TypeToken<Map<String, RegisteredClient>>(){}.getType(), out);

			out.close();

		} catch (FileNotFoundException e) {
			logger.error("Could not write to output file", e);
		} catch (IOException e) {
			logger.error("Could not write to output file", e);
		}
	}

	/**
	 * Load the map in from disk.
	 */
	private void load() {
		try {
			if (!file.exists()) {
				logger.info("No sved clients file found in " + file);
				return;
			}
			FileReader in = new FileReader(file);

			clients = gson.fromJson(in, new TypeToken<Map<String, RegisteredClient>>(){}.getType());

			in.close();

		} catch (FileNotFoundException e) {
			logger.error("Could not read from input file", e);
		} catch (IOException e) {
			logger.error("Could not read from input file", e);
		}
	}

}
