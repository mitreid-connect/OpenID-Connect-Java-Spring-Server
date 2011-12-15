package org.mitre.openid.connect.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("token")
public class TokenEndpoint {

	//Corresponds to spring security Authentication Filter class
	
	// handle sending back a token and an id token for a code
	
	// fall through to SSOA code if no id token?
	
}
