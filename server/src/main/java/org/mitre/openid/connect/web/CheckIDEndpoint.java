package org.mitre.openid.connect.web;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.exception.ExpiredTokenException;
import org.mitre.openid.connect.exception.InvalidJwtIssuerException;
import org.mitre.openid.connect.exception.InvalidJwtSignatureException;
import org.mitre.openid.connect.model.IdToken;
import org.mitre.openid.connect.model.IdTokenClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CheckIDEndpoint {

	@Autowired
	JwtSigningAndValidationService jwtSignerService;
	
	
	@RequestMapping("/checkid")
	public ModelAndView checkID(@RequestParam("id_token") String tokenString, ModelAndView mav) {
		
		if (!jwtSignerService.validateSignature(tokenString)) {
			// can't validate 
			throw new InvalidJwtSignatureException(); // TODO: attach a view to this exception
		}
		
		// it's a valid signature, parse the token
		IdToken token = IdToken.parse(tokenString);
		
		// check the expiration
		if (jwtSignerService.isJwtExpired(token)) {
			// token has expired
			throw new ExpiredTokenException(); // TODO create a view for this exception
		}
		
		// check the issuer (sanity check)
		if (!jwtSignerService.validateIssuedJwt(token)) {
			throw new InvalidJwtIssuerException(); // TODO: create a view for this exception
		}
		
		return new ModelAndView("jsonIdTokenView", "checkId", token); // TODO: create a view for this
	}
	
}
