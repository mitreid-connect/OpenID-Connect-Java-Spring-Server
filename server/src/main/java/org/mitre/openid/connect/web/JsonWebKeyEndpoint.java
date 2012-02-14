package org.mitre.openid.connect.web;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class JsonWebKeyEndpoint {

	@Autowired
	JwtSigningAndValidationService jwtService;
	
	@RequestMapping("/jwk")
	public ModelAndView getJwk() {
		
		List<PublicKey> keys = jwtService.getAllPublicKeys();
		
		// TODO: check if keys are empty, return a 404 here or just an empty list?
		
		return new ModelAndView("jwkKeyList", "entity", keys);
	}
	
}
