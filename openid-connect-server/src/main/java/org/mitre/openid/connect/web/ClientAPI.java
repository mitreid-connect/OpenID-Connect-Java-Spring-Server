/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
import java.security.Principal;
import java.util.Collection;

import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;
import org.mitre.jose.JWSAlgorithmEmbed;
import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/api/clients")
@PreAuthorize("hasRole('ROLE_ADMIN')")
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

    /**
     * Get a list of all clients
     * @param modelAndView
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ModelAndView apiGetAllClients(ModelAndView modelAndView) {

        Collection<ClientDetailsEntity> clients = clientService.getAllClients();
        modelAndView.addObject("entity", clients);
        modelAndView.setViewName("clientEntityView");

        return modelAndView;
    }

    /**
     * Create a new client
     * @param json
     * @param m
     * @param principal
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public String apiAddClient(@RequestBody String jsonString, Model m, Principal principal) {

    	JsonObject json = parser.parse(jsonString).getAsJsonObject();

    	ClientDetailsEntity client = gson.fromJson(json, ClientDetailsEntity.class);
        
        // if they leave the client secret empty, force it to be generated
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

        return "clientEntityView";
    }

    /**
     * Update an existing client
     * @param id
     * @param jsonString
     * @param m
     * @param principal
     * @return
     */
    @RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public String apiUpdateClient(@PathVariable("id") Long id, @RequestBody String jsonString, Model m, Principal principal) {
    	
    	// TODO: sanity check if the thing really is a JSON object
    	JsonObject json = parser.parse(jsonString).getAsJsonObject();

    	// parse the client passed in (from JSON) and fetch the old client from the store
        ClientDetailsEntity client = gson.fromJson(json, ClientDetailsEntity.class);
        ClientDetailsEntity oldClient = clientService.getClientById(id);
        
        if (oldClient == null) {
        	throw new ClientNotFoundException();
        }
        
        // if they leave the client secret empty, force it to be generated
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

        return "clientEntityView";
    }

    /**
     * Delete a client
     * @param id
     * @param modelAndView
     * @return
     */
    @RequestMapping(value="/{id}", method=RequestMethod.DELETE)
    public String apiDeleteClient(@PathVariable("id") Long id, ModelAndView modelAndView) {

        ClientDetailsEntity client = clientService.getClientById(id);
        
		if (client == null) {
			modelAndView.getModelMap().put("code", HttpStatus.NOT_FOUND);
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
    public ModelAndView apiShowClient(@PathVariable("id") Long id, ModelAndView modelAndView) {
        ClientDetailsEntity client = clientService.getClientById(id);
        if (client == null) {
            throw new ClientNotFoundException("Could not find client: " + id);
        }

        modelAndView.addObject("entity", client);
        modelAndView.setViewName("clientEntityView");

        return modelAndView;
    }
}
