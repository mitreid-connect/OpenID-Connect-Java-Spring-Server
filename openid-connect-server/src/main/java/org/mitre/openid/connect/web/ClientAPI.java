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

import com.google.gson.Gson;
import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/api/clients")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ClientAPI {

    @Autowired
    private ClientDetailsEntityService clientService;

    /**
     * constructor
     */
    public ClientAPI() {

    }

    @RequestMapping(method = RequestMethod.GET, headers="Accept=application/json")
    public ModelAndView apiGetAllClients(ModelAndView modelAndView) {

        Collection<ClientDetailsEntity> clients = clientService.getAllClients();
        modelAndView.addObject("entity", clients);
        modelAndView.setViewName("jsonClientView");

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public String apiAddClient(@RequestBody String json, Model m, Principal principal) {

        ClientDetailsEntity client = new Gson().fromJson(json, ClientDetailsEntity.class);
        // set owners as current logged in user
        client.setOwner(principal.getName());
        m.addAttribute("entity", clientService.saveClient(client));

        return "jsonClientView";
    }

    @RequestMapping(value="/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
    public String apiUpdateClient(@PathVariable("id") String id, @RequestBody String json, Model m, Principal principal) {

        ClientDetailsEntity client = new Gson().fromJson(json, ClientDetailsEntity.class);
        client.setClientId(id);
        // set owners as current logged in user
        client.setOwner(principal.getName());
        
        m.addAttribute("entity", clientService.saveClient(client));

        return "jsonClientView";
    }

    @RequestMapping(value="/{id}", method=RequestMethod.DELETE, headers="Accept=application/json")
    public String apiDeleteClient(@PathVariable("id") String id, ModelAndView modelAndView) {

        ClientDetailsEntity client = clientService.loadClientByClientId(id);
        clientService.deleteClient(client);

        return "jsonClientView";
    }


    @RequestMapping(value="/{id}", method=RequestMethod.GET, headers="Accept=application/json")
    @ResponseBody
    public Object apiShowClient(@PathVariable("id") Long id, ModelAndView modelAndView) {
        ClientDetailsEntity client = clientService.loadClientByClientId(id.toString());
        if (client == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        modelAndView.addObject("entity", client);
        modelAndView.setViewName("jsonClientView");

        return modelAndView;
    }
}
