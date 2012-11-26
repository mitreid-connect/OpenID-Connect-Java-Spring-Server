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
/**
 * 
 */
package org.mitre.oauth2.web;

import java.util.HashMap;
import java.util.Map;

import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author jricher
 *
 */
@Controller
@SessionAttributes("authorizationRequest")
public class OAuthConfirmationController {

	@Autowired
	private ClientDetailsEntityService clientService;
	
	public OAuthConfirmationController() {
		
	}
	
	public OAuthConfirmationController(ClientDetailsEntityService clientService) {
		this.clientService = clientService;
	}
	
	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping("/oauth/confirm_access")
	public ModelAndView confimAccess(Map<String, Object> model, @ModelAttribute("authorizationRequest") AuthorizationRequest clientAuth) {

		//AuthorizationRequest clientAuth = (AuthorizationRequest) model.remove("authorizationRequest");


		ClientDetails client = clientService.loadClientByClientId(clientAuth.getClientId());
		
		if (client == null) {
			throw new ClientNotFoundException("Client not found: " + clientAuth.getClientId());
		}

		model.put("auth_request", clientAuth);
		model.put("client", client);

		String redirect_uri = clientAuth.getAuthorizationParameters().get("redirect_uri");
		
        model.put("redirect_uri", redirect_uri);
	    
        Map<String, Boolean> scopes = new HashMap<String, Boolean>();
        for (String scope : clientAuth.getScope()) {
	        scopes.put(scope, Boolean.TRUE);
        }

        model.put("scopes", scopes);
        
        return new ModelAndView("oauth/approve", model);
	}

	/**
     * @return the clientService
     */
    public ClientDetailsEntityService getClientService() {
    	return clientService;
    }

	/**
     * @param clientService the clientService to set
     */
    public void setClientService(ClientDetailsEntityService clientService) {
    	this.clientService = clientService;
    }
	
	
}
