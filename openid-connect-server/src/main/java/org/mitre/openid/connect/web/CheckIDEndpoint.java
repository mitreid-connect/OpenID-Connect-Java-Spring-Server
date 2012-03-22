package org.mitre.openid.connect.web;

import javax.servlet.http.HttpServletRequest;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.exception.ExpiredTokenException;
import org.mitre.openid.connect.exception.InvalidJwtIssuerException;
import org.mitre.openid.connect.exception.InvalidJwtSignatureException;
import org.mitre.openid.connect.model.IdToken;
import org.mitre.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CheckIDEndpoint {

	@Autowired
	JwtSigningAndValidationService jwtSignerService;
	
	@Autowired
	private ConfigurationPropertiesBean configBean;
	
	@RequestMapping("/checkid")
	public ModelAndView checkID(@RequestParam("id_token") String tokenString, ModelAndView mav, HttpServletRequest request) {
		
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
		if (!jwtSignerService.validateIssuedJwt(token, configBean.getIssuer())) {
			throw new InvalidJwtIssuerException(); // TODO: create a view for this exception
		}
		
		return new ModelAndView("jsonIdTokenView", "checkId", token); // TODO: create a view for this
	}

	public JwtSigningAndValidationService getJwtSignerService() {
		return jwtSignerService;
	}

	public void setJwtSignerService(JwtSigningAndValidationService jwtSignerService) {
		this.jwtSignerService = jwtSignerService;
	}

	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}
	
}
