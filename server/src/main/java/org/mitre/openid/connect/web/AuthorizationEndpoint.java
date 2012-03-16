package org.mitre.openid.connect.web;

import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class AuthorizationEndpoint {

	@Autowired
	private ClientDetailsService clientDetailsService;

	//TODO: this endpoint needs to be protected
	@RequestMapping("/oauth/confirm_access")
	public ModelAndView getAccessConfirmation(
			@ModelAttribute AuthorizationRequest clientAuth) throws Exception {
		ClientDetails client = clientDetailsService
				.loadClientByClientId(clientAuth.getClientId());
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put("auth_request", clientAuth);
		model.put("client", client);
		return new ModelAndView("oauth/approve", model);
	}

	public void setClientDetailsService(
			ClientDetailsService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}

	public ClientDetailsService getClientDetailsService() {
		return this.clientDetailsService;
	}

	/*
	 * handle "idtoken token" flow
	 */

	/*
	 * Other flows get handled outside of our endpoints by SSOA
	 */
}
