/**
 * 
 */
package org.mitre.oauth2.web;

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
@SessionAttributes(types = AuthorizationRequest.class)
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
	public ModelAndView confimAccess(@ModelAttribute AuthorizationRequest clientAuth, 
			ModelAndView modelAndView) {
		
		ClientDetails client = clientService.loadClientByClientId(clientAuth.getClientId());
		
		if (client == null) {
			throw new ClientNotFoundException("Client not found: " + clientAuth.getClientId());
		}
		
		modelAndView.addObject("auth_request", clientAuth);
	    modelAndView.addObject("client", client);
	    modelAndView.setViewName("oauth/approve");
	    
	    return modelAndView;
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
