package org.mitre.openid.connect.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.DefaultUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;
import org.springframework.security.oauth2.provider.endpoint.AbstractEndpoint;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

@Controller("requestObjectAuthorzationEndpoint")
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
		Set<String> scopes = Sets.newHashSet(Splitter.on(" ").split(claims.getClaimAsString("scope")));
		
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
			if (parameters.containsKey("request_uri") == false) {
				parameters.put("request_uri", requestUri);
			}
		}

		// call out to the SECOAUTH endpoint to do the real processing
		return authorizationEndpoint.authorize(model, parameters.get("response_type"), parameters, sessionStatus, principal);
	}
	
}
