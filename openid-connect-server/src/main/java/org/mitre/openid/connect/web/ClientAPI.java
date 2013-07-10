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

import java.lang.reflect.Type;
import java.util.Collection;

import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;
import org.mitre.jose.JWSAlgorithmEmbed;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/api/clients")
@PreAuthorize("hasRole('ROLE_USER')")
public class ClientAPI {

	@Autowired
	private ClientDetailsEntityService clientService;
	private JsonParser parser = new JsonParser();

	private Gson gson = new GsonBuilder()
	.serializeNulls()
	.registerTypeAdapter(JWSAlgorithmEmbed.class, new JsonDeserializer<JWSAlgorithmEmbed>() {
		@Override
		public JWSAlgorithmEmbed deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive()) {
				return JWSAlgorithmEmbed.getForAlgorithmName(json.getAsString());
			} else {
				return null;
			}
		}
	})
	.registerTypeAdapter(JWEAlgorithmEmbed.class, new JsonDeserializer<JWEAlgorithmEmbed>() {
		@Override
		public JWEAlgorithmEmbed deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive()) {
				return JWEAlgorithmEmbed.getForAlgorithmName(json.getAsString());
			} else {
				return null;
			}
		}
	})
	.registerTypeAdapter(JWEEncryptionMethodEmbed.class, new JsonDeserializer<JWEEncryptionMethodEmbed>() {
		@Override
		public JWEEncryptionMethodEmbed deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive()) {
				return JWEEncryptionMethodEmbed.getForAlgorithmName(json.getAsString());
			} else {
				return null;
			}
		}
	})
	.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
	.create();

	private static Logger logger = LoggerFactory.getLogger(ClientAPI.class);

	/**
	 * Get a list of all clients
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public String apiGetAllClients(Model model, Authentication auth) {

		Collection<ClientDetailsEntity> clients = clientService.getAllClients();
		model.addAttribute("entity", clients);

		if (isAdmin(auth)) {
			return "clientEntityViewAdmins";
		} else {
			return "clientEntityViewUsers";
		}
	}

	/**
	 * Create a new client
	 * @param json
	 * @param m
	 * @param principal
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public String apiAddClient(@RequestBody String jsonString, Model m, Authentication auth) {

		JsonObject json = null;
		ClientDetailsEntity client = null;

		try {
			json = parser.parse(jsonString).getAsJsonObject();
			client = gson.fromJson(json, ClientDetailsEntity.class);
		}
		catch (JsonSyntaxException e) {
			logger.error("apiAddClient failed due to JsonSyntaxException", e);
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			m.addAttribute("errorMessage", "Could not save new client. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return "jsonErrorView";
		} catch (IllegalStateException e) {
			logger.error("apiAddClient failed due to IllegalStateException", e);
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			m.addAttribute("errorMessage", "Could not save new client. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return "jsonErrorView";
		}

		// if they leave the client identifier empty, force it to be generated
		if (Strings.isNullOrEmpty(client.getClientId())) {
			client = clientService.generateClientId(client);
		}

		// if they've asked for us to generate a client secret, do so here
		if (json.has("generateClientSecret") && json.get("generateClientSecret").getAsBoolean()) {
			client = clientService.generateClientSecret(client);
		}

		// set owners as current logged in user
		//client.setOwner(principal.getName());
		//TODO: owner has been replaced by a list of contacts, which should be styled as email addresses.
		client.setDynamicallyRegistered(false);

		ClientDetailsEntity newClient = clientService.saveNewClient(client);
		m.addAttribute("entity", newClient);

		if (isAdmin(auth)) {
			return "clientEntityViewAdmins";
		} else {
			return "clientEntityViewUsers";
		}
	}

	/**
	 * Update an existing client
	 * @param id
	 * @param jsonString
	 * @param m
	 * @param principal
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public String apiUpdateClient(@PathVariable("id") Long id, @RequestBody String jsonString, Model m, Authentication auth) {

		JsonObject json = null;
		ClientDetailsEntity client = null;

		try {
			// parse the client passed in (from JSON) and fetch the old client from the store
			json = parser.parse(jsonString).getAsJsonObject();
			client = gson.fromJson(json, ClientDetailsEntity.class);
		}
		catch (JsonSyntaxException e) {
			logger.error("apiUpdateClient failed due to JsonSyntaxException", e);
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			m.addAttribute("errorMessage", "Could not update client. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return "jsonErrorView";
		} catch (IllegalStateException e) {
			logger.error("apiUpdateClient failed due to IllegalStateException", e);
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			m.addAttribute("errorMessage", "Could not update client. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return "jsonErrorView";
		}

		ClientDetailsEntity oldClient = clientService.getClientById(id);

		if (oldClient == null) {
			logger.error("apiUpdateClient failed; client with id " + id + " could not be found.");
			m.addAttribute("code", HttpStatus.NOT_FOUND);
			m.addAttribute("errorMessage", "Could not update client. The requested client with id " + id + "could not be found.");
			return "jsonErrorView";
		}

		// if they leave the client identifier empty, force it to be generated
		if (Strings.isNullOrEmpty(client.getClientId())) {
			client = clientService.generateClientId(client);
		}

		// if they've asked for us to generate a client secret, do so here
		if (json.has("generateClientSecret") && json.get("generateClientSecret").getAsBoolean()) {
			client = clientService.generateClientSecret(client);
		}

		// set owners as current logged in user
		// client.setOwner(principal.getName());
		//TODO: owner has been replaced by a list of contacts, which should be styled as email addresses.

		ClientDetailsEntity newClient = clientService.updateClient(oldClient, client);
		m.addAttribute("entity", newClient);

		if (isAdmin(auth)) {
			return "clientEntityViewAdmins";
		} else {
			return "clientEntityViewUsers";
		}
	}

	/**
	 * Delete a client
	 * @param id
	 * @param modelAndView
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public String apiDeleteClient(@PathVariable("id") Long id, ModelAndView modelAndView) {

		ClientDetailsEntity client = clientService.getClientById(id);

		if (client == null) {
			logger.error("apiDeleteClient failed; client with id " + id + " could not be found.");
			modelAndView.getModelMap().put("code", HttpStatus.NOT_FOUND);
			modelAndView.getModelMap().put("errorMessage", "Could not delete client. The requested client with id " + id + "could not be found.");
			return "jsonErrorView";
		} else {
			modelAndView.getModelMap().put("code", HttpStatus.OK);
			clientService.deleteClient(client);
		}

		return "httpCodeView";
	}


	/**
	 * Get an individual client
	 * @param id
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces = "application/json")
	public String apiShowClient(@PathVariable("id") Long id, Model model, Authentication auth) {

		ClientDetailsEntity client = clientService.getClientById(id);

		if (client == null) {
			logger.error("apiShowClient failed; client with id " + id + " could not be found.");
			model.addAttribute("code", HttpStatus.NOT_FOUND);
			model.addAttribute("errorMessage", "The requested client with id " + id + "could not be found.");
			return "jsonErrorView";
		}

		model.addAttribute("entity", client);

		if (isAdmin(auth)) {
			return "clientEntityViewAdmins";
		} else {
			return "clientEntityViewUsers";
		}
	}

	/**
	 * Check to see if the given auth object has ROLE_ADMIN assigned to it or not
	 * @param auth
	 * @return
	 */
	private boolean isAdmin(Authentication auth) {
		for (GrantedAuthority grantedAuthority : auth.getAuthorities()) {
			if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
				return true;
			}
		}
		return false;
	}
}
