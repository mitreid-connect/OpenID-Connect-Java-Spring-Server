/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
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
package org.mitre.openid.connect.web;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.web.AuthenticationUtilities;
import org.mitre.openid.connect.model.CachedImage;
import org.mitre.openid.connect.service.ClientLogoLoadingService;
import org.mitre.openid.connect.view.ClientEntityViewForAdmins;
import org.mitre.openid.connect.view.ClientEntityViewForUsers;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/" + ClientAPI.URL)
@PreAuthorize("hasRole('ROLE_USER')")
public class ClientAPI {

	public static final String URL = RootController.API_URL + "/clients";

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private ClientLogoLoadingService clientLogoLoadingService;

	private JsonParser parser = new JsonParser();

	private Gson gson = new GsonBuilder()
	.serializeNulls()
	.registerTypeAdapter(JWSAlgorithm.class, new JsonDeserializer<Algorithm>() {
		@Override
		public JWSAlgorithm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive()) {
				return JWSAlgorithm.parse(json.getAsString());
			} else {
				return null;
			}
		}
	})
	.registerTypeAdapter(JWEAlgorithm.class, new JsonDeserializer<Algorithm>() {
		@Override
		public JWEAlgorithm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive()) {
				return JWEAlgorithm.parse(json.getAsString());
			} else {
				return null;
			}
		}
	})
	.registerTypeAdapter(EncryptionMethod.class, new JsonDeserializer<Algorithm>() {
		@Override
		public EncryptionMethod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive()) {
				return EncryptionMethod.parse(json.getAsString());
			} else {
				return null;
			}
		}
	})
	.registerTypeAdapter(JWKSet.class, new JsonDeserializer<JWKSet>() {
		@Override
		public JWKSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonObject()) {
				try {
					return JWKSet.parse(json.toString());
				} catch (ParseException e) {
					return null;
				}
			} else {
				return null;
			}
		}
	})
	.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
	.create();

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ClientAPI.class);

	/**
	 * Get a list of all clients
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiGetAllClients(Model model, Authentication auth) {

		Collection<ClientDetailsEntity> clients = clientService.getAllClients();
		model.addAttribute(JsonEntityView.ENTITY, clients);

		if (AuthenticationUtilities.isAdmin(auth)) {
			return ClientEntityViewForAdmins.VIEWNAME;
		} else {
			return ClientEntityViewForUsers.VIEWNAME;
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
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiAddClient(@RequestBody String jsonString, Model m, Authentication auth) {

		JsonObject json = null;
		ClientDetailsEntity client = null;

		try {
			json = parser.parse(jsonString).getAsJsonObject();
			client = gson.fromJson(json, ClientDetailsEntity.class);
		}
		catch (JsonSyntaxException e) {
			logger.error("apiAddClient failed due to JsonSyntaxException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not save new client. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			logger.error("apiAddClient failed due to IllegalStateException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not save new client. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		}

		// if they leave the client identifier empty, force it to be generated
		if (Strings.isNullOrEmpty(client.getClientId())) {
			client = clientService.generateClientId(client);
		}

		if (client.getTokenEndpointAuthMethod() == null ||
				client.getTokenEndpointAuthMethod().equals(AuthMethod.NONE)) {
			// we shouldn't have a secret for this client

			client.setClientSecret(null);

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_BASIC)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_POST)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT)) {

			// if they've asked for us to generate a client secret (or they left it blank but require one), do so here
			if (json.has("generateClientSecret") && json.get("generateClientSecret").getAsBoolean()
					|| Strings.isNullOrEmpty(client.getClientSecret())) {
				client = clientService.generateClientSecret(client);
			}

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY)) {

			if (Strings.isNullOrEmpty(client.getJwksUri()) && client.getJwks() == null) {
				logger.error("tried to create client with private key auth but no private key");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Can not create a client with private key authentication without registering a key via the JWK Set URI or JWK Set Value.");
				return JsonErrorView.VIEWNAME;
			}

			// otherwise we shouldn't have a secret for this client
			client.setClientSecret(null);

		} else {

			logger.error("unknown auth method");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unknown auth method requested");
			return JsonErrorView.VIEWNAME;


		}

		client.setDynamicallyRegistered(false);

		try {
			ClientDetailsEntity newClient = clientService.saveNewClient(client);
			m.addAttribute(JsonEntityView.ENTITY, newClient);

			if (AuthenticationUtilities.isAdmin(auth)) {
				return ClientEntityViewForAdmins.VIEWNAME;
			} else {
				return ClientEntityViewForUsers.VIEWNAME;
			}
		} catch (IllegalArgumentException e) {
			logger.error("Unable to save client: {}", e.getMessage());
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client: " + e.getMessage());
			return JsonErrorView.VIEWNAME;
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
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			logger.error("apiUpdateClient failed due to IllegalStateException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		}

		ClientDetailsEntity oldClient = clientService.getClientById(id);

		if (oldClient == null) {
			logger.error("apiUpdateClient failed; client with id " + id + " could not be found.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The requested client with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		}

		// if they leave the client identifier empty, force it to be generated
		if (Strings.isNullOrEmpty(client.getClientId())) {
			client = clientService.generateClientId(client);
		}

		if (client.getTokenEndpointAuthMethod() == null ||
				client.getTokenEndpointAuthMethod().equals(AuthMethod.NONE)) {
			// we shouldn't have a secret for this client

			client.setClientSecret(null);

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_BASIC)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_POST)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT)) {

			// if they've asked for us to generate a client secret (or they left it blank but require one), do so here
			if (json.has("generateClientSecret") && json.get("generateClientSecret").getAsBoolean()
					|| Strings.isNullOrEmpty(client.getClientSecret())) {
				client = clientService.generateClientSecret(client);
			}

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY)) {

			if (Strings.isNullOrEmpty(client.getJwksUri()) && client.getJwks() == null) {
				logger.error("tried to create client with private key auth but no private key");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Can not create a client with private key authentication without registering a key via the JWK Set URI or JWK Set Value.");
				return JsonErrorView.VIEWNAME;
			}

			// otherwise we shouldn't have a secret for this client
			client.setClientSecret(null);

		} else {

			logger.error("unknown auth method");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unknown auth method requested");
			return JsonErrorView.VIEWNAME;


		}

		try {
			ClientDetailsEntity newClient = clientService.updateClient(oldClient, client);
			m.addAttribute(JsonEntityView.ENTITY, newClient);

			if (AuthenticationUtilities.isAdmin(auth)) {
				return ClientEntityViewForAdmins.VIEWNAME;
			} else {
				return ClientEntityViewForUsers.VIEWNAME;
			}
		} catch (IllegalArgumentException e) {
			logger.error("Unable to save client: {}", e.getMessage());
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client: " + e.getMessage());
			return JsonErrorView.VIEWNAME;
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
			modelAndView.getModelMap().put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			modelAndView.getModelMap().put(JsonErrorView.ERROR_MESSAGE, "Could not delete client. The requested client with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {
			modelAndView.getModelMap().put(HttpCodeView.CODE, HttpStatus.OK);
			clientService.deleteClient(client);
		}

		return HttpCodeView.VIEWNAME;
	}


	/**
	 * Get an individual client
	 * @param id
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiShowClient(@PathVariable("id") Long id, Model model, Authentication auth) {

		ClientDetailsEntity client = clientService.getClientById(id);

		if (client == null) {
			logger.error("apiShowClient failed; client with id " + id + " could not be found.");
			model.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			model.addAttribute(JsonErrorView.ERROR_MESSAGE, "The requested client with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}

		model.addAttribute(JsonEntityView.ENTITY, client);

		if (AuthenticationUtilities.isAdmin(auth)) {
			return ClientEntityViewForAdmins.VIEWNAME;
		} else {
			return ClientEntityViewForUsers.VIEWNAME;
		}
	}
	
	/**
	 * Get the logo image for a client
	 * @param id
	 */
	 @RequestMapping(value = "/{id}/logo", method=RequestMethod.GET, produces = { MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
	 public ResponseEntity<byte[]> getClientLogo(@PathVariable("id") Long id, Model model) {
		 
			ClientDetailsEntity client = clientService.getClientById(id);

			if (client == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else if (Strings.isNullOrEmpty(client.getLogoUri())) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				// get the image from cache
				CachedImage image = clientLogoLoadingService.getLogo(client);
				
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType(image.getContentType()));
				headers.setContentLength(image.getLength());
				
				return new ResponseEntity<>(image.getData(), headers, HttpStatus.OK);
			}
	 }

}
