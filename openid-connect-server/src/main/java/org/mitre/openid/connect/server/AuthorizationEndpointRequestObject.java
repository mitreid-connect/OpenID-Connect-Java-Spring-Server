package org.mitre.openid.connect.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.springframework.beans.factory.InitializingBean;
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
import org.springframework.security.oauth2.provider.approval.DefaultUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.endpoint.AbstractEndpoint;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.stereotype.Controller;
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

@Controller
@SessionAttributes(types = AuthorizationRequest.class)
@RequestMapping(value = "/oauth/authorize")
public class AuthorizationEndpointRequestObject extends AbstractEndpoint implements InitializingBean {
	
	private RedirectResolver redirectResolver = new DefaultRedirectResolver();
	
	private ClientDetailsService clientDetailsService;
	
	private UserApprovalHandler userApprovalHandler = new DefaultUserApprovalHandler();
	
	private AuthorizationCodeServices authorizationCodeServices = new InMemoryAuthorizationCodeServices();
	
	private String userApprovalPage = "forward:/oauth/confirm_access";
	
	@RequestMapping(params = "response_type")
	public ModelAndView getRequest(Map<String, Object> model, @RequestParam("request") String jwtString, 
			@RequestParam Map<String, String> parameters, SessionStatus sessionStatus, Principal principal) {
		
		Jwt jwt = Jwt.parse(jwtString);
		JwtClaims claims = jwt.getClaims();
		
		// Manually initialize auth request instead of using @ModelAttribute
		// to make sure it comes from request instead of the session
		
		Map<String, String> jwtRequest = new HashMap<String, String>();
		
		jwtRequest.put("jwt", jwtString);
		
		AuthorizationRequest authorizationRequest = new AuthorizationRequest(jwtRequest);

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
				if (claims.getClaim("response_type").equals("token")) {
					return getImplicitGrantResponse(authorizationRequest);
				}
				if (claims.getClaim("response_type").equals("code")) {
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
	
	@RequestMapping(method = RequestMethod.POST, params = AuthorizationRequest.USER_OAUTH_APPROVAL)
	public View approveOrDeny(@RequestParam Map<String, String> approvalParameters,
			@ModelAttribute AuthorizationRequest authorizationRequest, SessionStatus sessionStatus, Principal principal) {
		
		String jwtString = authorizationRequest.getApprovalParameters().get("jwt");
		Jwt jwt = Jwt.parse(jwtString);

		if (jwt.getClaims().getClaim("client_id").toString() == null) {
			sessionStatus.setComplete();
			throw new InvalidClientException("A client_id must be supplied.");
		}

		if (!(principal instanceof Authentication)) {
			sessionStatus.setComplete();
			throw new InsufficientAuthenticationException(
					"User must be authenticated with Spring Security before authorizing an access token.");
		}

		try {
			Set<String> responseTypes = authorizationRequest.getResponseTypes();

			authorizationRequest = authorizationRequest.addApprovalParameters(approvalParameters);
			authorizationRequest = resolveRedirectUriAndCheckApproval(authorizationRequest, (Authentication) principal);

			if (!authorizationRequest.isApproved()) {
				return new RedirectView(getUnsuccessfulRedirect(authorizationRequest,
						new UserDeniedAuthorizationException("User denied access"), responseTypes.contains("token")),
						false);
			}

			if (responseTypes.contains("token")) {
				return getImplicitGrantResponse(authorizationRequest).getView();
			}

			return getAuthorizationCodeResponse(authorizationRequest, (Authentication) principal);
		}
		finally {
			sessionStatus.setComplete();
		}

	}
	
	//change to use jwt rather than authRequest
	private AuthorizationRequest resolveRedirectUriAndCheckApproval(AuthorizationRequest authorizationRequest, 
			Authentication authentication) throws OAuth2Exception {
		
		String jwtString = authorizationRequest.getApprovalParameters().get("jwt");
		Jwt jwt = Jwt.parse(jwtString);
		
		String requestedRedirect = redirectResolver.resolveRedirect(jwt.getClaims().getClaim("redirect_uri").toString(),
				clientDetailsService.loadClientByClientId(jwt.getClaims().getClaim("client_id").toString()));
		authorizationRequest = authorizationRequest.resolveRedirectUri(requestedRedirect);

		boolean approved = authorizationRequest.isApproved();
		if (!approved) {
			approved = userApprovalHandler.isApproved(authorizationRequest, authentication);
			authorizationRequest = authorizationRequest.approved(approved);
		}

		return authorizationRequest;

	}
	
	//change to use a jwt rather than authRequest
	private ModelAndView getImplicitGrantResponse(AuthorizationRequest authorizationRequest) {
		
		String jwtString = authorizationRequest.getApprovalParameters().get("jwt");
		Jwt jwt = Jwt.parse(jwtString);
		
		try {
			OAuth2AccessToken accessToken = getTokenGranter().grant("implicit",
					authorizationRequest.getAuthorizationParameters(), jwt.getClaims().getClaimAsString("client_id").toString(),
					authorizationRequest.getScope());
			if (accessToken == null) {
				throw new UnsupportedGrantTypeException("Unsupported grant type: implicit");
			}
			return new ModelAndView(new RedirectView(appendAccessToken(authorizationRequest, accessToken), false));
		}
		catch (OAuth2Exception e) {
			return new ModelAndView(new RedirectView(getUnsuccessfulRedirect(authorizationRequest, e, true), false));
		}
	}
	
	private String appendAccessToken(AuthorizationRequest authorizationRequest, OAuth2AccessToken accessToken) {
		
		String jwtString = authorizationRequest.getApprovalParameters().get("jwt");
		Jwt jwt = Jwt.parse(jwtString);
		
		String requestedRedirect = jwt.getClaims().getClaim("redirect_uri").toString();
		if (accessToken == null) {
			throw new InvalidGrantException("An implicit grant could not be made");
		}
		StringBuilder url = new StringBuilder(requestedRedirect);
		if (requestedRedirect.contains("#")) {
			url.append("&");
		}
		else {
			url.append("#");
		}
		url.append("access_token=" + accessToken.getValue());
		url.append("&token_type=" + accessToken.getTokenType());
		String state = authorizationRequest.getState();
		if (state != null) {
			url.append("&state=" + state);
		}
		Date expiration = accessToken.getExpiration();
		if (expiration != null) {
			long expires_in = (expiration.getTime() - System.currentTimeMillis()) / 1000;
			url.append("&expires_in=" + expires_in);
		}
		Map<String, Object> additionalInformation = accessToken.getAdditionalInformation();
		for (String key : additionalInformation.keySet()) {
			Object value = additionalInformation.get(key);
			if (value != null && ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
				url.append("&" + key + "=" + value);
			}
		}
		// Do not include the refresh token (even if there is one)
		return url.toString();
	}
	
	private View getAuthorizationCodeResponse(AuthorizationRequest authorizationRequest, Authentication authUser) {
		try {
			return new RedirectView(getSuccessfulRedirect(authorizationRequest,
					generateCode(authorizationRequest, authUser)), false);
		}
		catch (OAuth2Exception e) {
			return new RedirectView(getUnsuccessfulRedirect(authorizationRequest, e, false), false);
		}
	}
	
	private String generateCode(AuthorizationRequest authorizationRequest, Authentication authentication)
			throws AuthenticationException {

		try {

			AuthorizationRequestHolder combinedAuth = new AuthorizationRequestHolder(authorizationRequest,
					authentication);
			String code = authorizationCodeServices.createAuthorizationCode(combinedAuth);

			return code;

		}
		catch (OAuth2Exception e) {

			if (authorizationRequest.getState() != null) {
				e.addAdditionalInformation("state", authorizationRequest.getState());
			}

			throw e;

		}
	}
	
	private String getUnsuccessfulRedirect(AuthorizationRequest authorizationRequest, OAuth2Exception failure,
			boolean fragment) {
		
		String jwtString = authorizationRequest.getApprovalParameters().get("jwt");
		Jwt jwt = Jwt.parse(jwtString);

		// TODO: allow custom failure handling?
		if (authorizationRequest == null || jwt.getClaims().getClaim("redirect_uri").toString() == null) {
			// we have no redirect for the user. very sad.
			throw new UnapprovedClientAuthenticationException("Authorization failure, and no redirect URI.", failure);
		}

		String redirectUri = jwt.getClaims().getClaim("redirect_uri").toString();

		// extract existing fragments if any
		String[] fragments = redirectUri.split("#");

		StringBuilder url = new StringBuilder(fragment ? redirectUri : fragments[0]);

		char separator = fragment ? '#' : '?';
		if (redirectUri.indexOf(separator) < 0) {
			url.append(separator);
		}
		else {
			url.append('&');
		}
		url.append("error=").append(failure.getOAuth2ErrorCode());
		try {

			url.append("&error_description=").append(URLEncoder.encode(failure.getMessage(), "UTF-8"));

			if (authorizationRequest.getState() != null) {
				url.append('&').append("state=").append(authorizationRequest.getState());
			}

			if (failure.getAdditionalInformation() != null) {
				for (Map.Entry<String, String> additionalInfo : failure.getAdditionalInformation().entrySet()) {
					url.append('&').append(additionalInfo.getKey()).append('=')
							.append(URLEncoder.encode(additionalInfo.getValue(), "UTF-8"));
				}
			}

		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}

		if (!fragment && fragments.length > 1) {
			url.append("#" + fragments[1]);
		}

		return url.toString();

	}
	
	private ModelAndView getUserApprovalPageResponse(Map<String, Object> model,
			AuthorizationRequest authorizationRequest) {
		logger.debug("Loading user approval page: " + userApprovalPage);
		// In case of a redirect we might want the request parameters to be included
		model.putAll(authorizationRequest.getAuthorizationParameters());
		return new ModelAndView(userApprovalPage, model);
	}
	
	private String getSuccessfulRedirect(AuthorizationRequest authorizationRequest, String authorizationCode) {
		
		String jwtString = authorizationRequest.getApprovalParameters().get("jwt");
		Jwt jwt = Jwt.parse(jwtString);

		if (authorizationCode == null) {
			throw new IllegalStateException("No authorization code found in the current request scope.");
		}

		String requestedRedirect = jwt.getClaims().getClaim("redirect_uri").toString();
		String[] fragments = requestedRedirect.split("#");
		String state = authorizationRequest.getState();

		StringBuilder url = new StringBuilder(fragments[0]);
		if (requestedRedirect.indexOf('?') < 0) {
			url.append('?');
		}
		else {
			url.append('&');
		}
		url.append("code=").append(authorizationCode);

		if (state != null) {
			url.append("&state=").append(state);
		}

		if (fragments.length > 1) {
			url.append("#" + fragments[1]);
		}

		return url.toString();
	}
}
