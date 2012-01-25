package org.mitre.openid.connect.web;

import org.mitre.openid.connect.model.IdToken;
import org.mitre.openid.connect.model.IdTokenClaims;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CheckIDEndpoint {

	
	
	@RequestMapping("/checkid")
	public ModelAndView checkID(@RequestParam("id_token") String tokenString, ModelAndView mav) {
		
		IdToken token = IdToken.parse(tokenString);
		
		
		
		
		return new ModelAndView("jsonIdTokenView", "checkId", token);
	}
	
}
