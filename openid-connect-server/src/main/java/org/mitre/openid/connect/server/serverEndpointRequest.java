package org.mitre.openid.connect.server;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class serverEndpointRequest {
	
	@Autowired 
	JwtSigningAndValidationService jwtService;
	
	@RequestMapping("/serverEndpoint")
	public void getRequest() {
		
	}

}
