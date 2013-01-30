package org.mitre.openid.connect.web;

import java.security.Principal;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

//@Controller("requestObjectAuthorzationEndpoint")
public class RequestObjectAuthorizationEndpoint {
	
	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private AuthorizationEndpoint authorizationEndpoint;
	
	@RequestMapping(value = "/oauth/authorize", params = "request")
	public ModelAndView authorizeRequestObject(Map<String, Object> model, @RequestParam("request") String jwtString, 
			@RequestParam Map<String, String> parameters, SessionStatus sessionStatus, Principal principal) {
		
		Jwt jwt = Jwt.parse(jwtString);
		JwtClaims claims = jwt.getClaims();
		
		// TODO: validate JWT signature
		
		String clientId = claims.getClaimAsString("client_id");
		//Set<String> scopes = Sets.newHashSet(Splitter.on(" ").split(claims.getClaimAsString("scope")));
		
		// Manually initialize auth request instead of using @ModelAttribute
		// to make sure it comes from request instead of the session
		
		// TODO: check parameter consistency, move keys to constants
		
		/*
		 * if (in Claims):
		 * 		if (in params):
		 * 			if (equal):
		 * 				all set
		 * 			else (not equal):
		 * 				error
		 * 		else (not in params):
		 * 			add to params
		 * else (not in claims):
		 * 		we don't care
		 */
		
		String responseTypes = claims.getClaimAsString("response_type");
		if (responseTypes != null) {
			parameters.put("response_type", responseTypes);
		}
		
		if (clientId != null) {
			parameters.put("client_id", clientId);
		}
		
		if (claims.getClaimAsString("redirect_uri") != null) {
			if (parameters.containsKey("redirect_uri") == false) {
				parameters.put("redirect_uri", claims.getClaimAsString("redirect_uri"));
			}
		}
		
		String state = claims.getClaimAsString("state");
		if(state != null) {
			if (parameters.containsKey("state") == false) {
				parameters.put("state", state);
			}
		}
		
		String nonce = claims.getClaimAsString("nonce");
		if(nonce != null) {
			if (parameters.containsKey("nonce") == false) {
				parameters.put("nonce", nonce);
			}
		}
		
		String display = claims.getClaimAsString("display");
		if (display != null) {
			if (parameters.containsKey("display") == false) {
				parameters.put("display", display);
			}
		}
		
		String prompt = claims.getClaimAsString("prompt");
		if (prompt != null) {
			if (parameters.containsKey("prompt") == false) {
				parameters.put("prompt", prompt);
			}
		}
		
		String request = claims.getClaimAsString("request");
		if (request != null) {
			if (parameters.containsKey("request") == false) {
				parameters.put("request", request);
			}
		}
		
		String requestUri = claims.getClaimAsString("request_uri");
		if (requestUri != null) {
			//The spec does not allow a client to send a request parameter AND 
			//link to a hosted request object at the same time, so this is an error.
			//TODO: what error to throw?
		}

		// call out to the SECOAUTH endpoint to do the real processing
		return authorizationEndpoint.authorize(model, parameters.get("response_type"), parameters, sessionStatus, principal);
	}
	
}
