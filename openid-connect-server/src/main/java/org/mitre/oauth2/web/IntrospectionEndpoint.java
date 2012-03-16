package org.mitre.oauth2.web;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IntrospectionEndpoint {

	@Autowired
	OAuth2TokenEntityService tokenServices;
	
	public IntrospectionEndpoint() {
		
	}
	
	public IntrospectionEndpoint(OAuth2TokenEntityService tokenServices) {
		this.tokenServices = tokenServices;
	}
	
	// TODO
	@RequestMapping("/oauth/verify")
	public ModelAndView verify(@RequestParam("token") String tokenValue, 
			ModelAndView modelAndView) {
		OAuth2AccessTokenEntity token = tokenServices.getAccessToken(tokenValue);
		
		if (token == null) {
			// if it's not a valid token, we'll print a 404
			modelAndView.setViewName("tokenNotFound");
		} else {
			// if it's a valid token, we'll print out the scope and expiration
			modelAndView.setViewName("tokenIntrospection");
			modelAndView.addObject("entity", token);
		}
		
		return modelAndView;
	}
	
}
