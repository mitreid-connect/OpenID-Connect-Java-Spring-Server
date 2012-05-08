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

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/api/clients")
public class ClientAPI {

    @Autowired
    private ClientDetailsEntityService clientService;

    /**
     * constructor
     */
    public ClientAPI() {

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("")
    public ModelAndView apiGetAllClients(ModelAndView modelAndView) {

        Collection<ClientDetailsEntity> clients = clientService.getAllClients();
        modelAndView.addObject("entity", clients);
        modelAndView.setViewName("jsonClientView");

        return modelAndView;
    }

/*
    */
/**
     *
     * @param modelAndView
     * @param clientId
     * @param clientSecret
     * @param scope
     * @param grantTypes
     * @param redirectUri
     * @param authorities
     * @param name
     * @param description
     * @param allowRefresh
     * @param accessTokenTimeout
     * @param refreshTokenTimeout
     * @param owner
     * @return
     *//*

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/add")
    public ModelAndView apiAddClient(ModelAndView modelAndView,
                                     @RequestParam String clientId, @RequestParam String clientSecret,
                                     @RequestParam String scope, // space delimited
                                     @RequestParam String grantTypes, // space delimited
                                     @RequestParam(required = false) String redirectUri,
                                     @RequestParam String authorities, // space delimited
                                     @RequestParam(required = false) String name,
                                     @RequestParam(required = false) String description,
                                     @RequestParam(required = false, defaultValue = "false") boolean allowRefresh,
                                     @RequestParam(required = false) Long accessTokenTimeout,
                                     @RequestParam(required = false) Long refreshTokenTimeout,
                                     @RequestParam(required = false) String owner
    ) {
        return null;
    }

    */
/**
     *
     * @param modelAndView
     * @param clientId
     * @return
     *//*

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/delete")
    public ModelAndView apiDeleteClient(ModelAndView modelAndView,
                                        @RequestParam String clientId) {
        return null;
    }
*/



  /*  *//**
     *
     * @param modelAndView
     * @param clientId
     * @param clientSecret
     * @param scope
     * @param grantTypes
     * @param redirectUri
     * @param authorities
     * @param name
     * @param description
     * @param allowRefresh
     * @param accessTokenTimeout
     * @param refreshTokenTimeout
     * @param owner
     * @return
     *//*
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/update")
    public ModelAndView apiUpdateClient(ModelAndView modelAndView,
                                        @RequestParam String clientId, @RequestParam String clientSecret,
                                        @RequestParam String scope, // space delimited
                                        @RequestParam String grantTypes, // space delimited
                                        @RequestParam(required = false) String redirectUri,
                                        @RequestParam String authorities, // space delimited
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false, defaultValue = "false") boolean allowRefresh,
                                        @RequestParam(required = false) Long accessTokenTimeout,
                                        @RequestParam(required = false) Long refreshTokenTimeout,
                                        @RequestParam(required = false) String owner
    ) {
        return null;
    }*/
}
