package org.mitre.openid.connect.web;

import org.mitre.openid.connect.model.IdTokenClaims;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/checkid")
public class CheckIDEndpoint {

	@RequestMapping("/")
	public ModelAndView checkID(@RequestParam("id_token") String idToken, ModelAndView mav) {
		
		IdTokenClaims token = new IdTokenClaims();
		
		//TODO: Set claims
		
		return new ModelAndView("jsonIdTokenView", "checkId", token);
	}
	
}
