package org.mitre.openid.connect.web;

import java.security.Principal;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

// 
// TODO: make this into a controller again, use the forward: or redirect: mechanism to send to auth endpoint
//

//@Controller("requestObjectAuthorzationEndpoint")
public class RequestObjectAuthorizationEndpoint {
	
	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private AuthorizationEndpoint authorizationEndpoint;
	
	@RequestMapping(value = "/oauth/authorize", params = "request")
	public ModelAndView authorizeRequestObject(Map<String, Object> model, @RequestParam("request") String jwtString, 
			@RequestParam Map<String, String> parameters, SessionStatus sessionStatus, Principal principal) {
		
		/*
		 * 
		 * SEE Processing code in ConnectAuthorizationRequestManager.processRequestObject
		 *
		 */ 

		return null;
		
	}
	
}
