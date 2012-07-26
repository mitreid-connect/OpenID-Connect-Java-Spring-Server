package org.mitre.openid.connect.server;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes(types = AuthorizationRequest.class)
@RequestMapping(value = "/oauth/authorize")
public class AuthorizationEndpointRequestObject {
	
	@RequestMapping(params = "response_type")
	public void getRequest(@RequestParam("request") String jwtString, @RequestParam Map<String, String> parameters, 
			SessionStatus sessionStatus, Principal principal) {
		
		Jwt jwt = Jwt.parse(jwtString);
		JwtClaims claims = jwt.getClaims();
		
		// Manually initialize auth request instead of using @ModelAttribute
		// to make sure it comes from request instead of the session
		AuthorizationRequest authorizationRequest = new AuthorizationRequest(parameters);

		if (claims.getClaim("client_id") == null) {
			sessionStatus.setComplete();
			throw new InvalidClientException("A client_id must be supplied.");
		}

		if (!(principal instanceof Authentication) || !((Authentication) principal).isAuthenticated()) {
			sessionStatus.setComplete();
			throw new InsufficientAuthenticationException(
					"User must be authenticated with Spring Security before authorization can be completed.");
		}

		if (!claims.getClaim("response_type").equals("token") && !claims.getClaim("response_type").equals("code")) {
			throw new UnsupportedGrantTypeException("Unsupported response types: " + claims.getClaim("response_type"));
		}

		try {

			authorizationRequest = resolveRedirectUriAndCheckApproval(authorizationRequest, (Authentication) principal);
			if (authorizationRequest.isApproved()) {
				if (responseTypes.contains("token")) {
					return getImplicitGrantResponse(authorizationRequest);
				}
				if (responseTypes.contains("code")) {
					return new ModelAndView(getAuthorizationCodeResponse(authorizationRequest,
							(Authentication) principal));
				}
			}

			// Place auth request into the model so that it is stored in the session
			// for approveOrDeny to use. That way we make sure that auth request comes from the session,
			// so any auth request parameters passed to approveOrDeny will be ignored and retrieved from the session.
			model.put("authorizationRequest", authorizationRequest);

			return getUserApprovalPageResponse(model, authorizationRequest);

		}
		catch (RuntimeException e) {
			sessionStatus.setComplete();
			throw e;
		}
		
	
	}
}
