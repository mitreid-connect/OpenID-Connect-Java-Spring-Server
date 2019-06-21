package org.mitre.openid.connect.web.sessionstate;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor to append the session state parameter to an
 * successful authorization response.
 *
 * Should be applied to the authorization url only.
 *
 * @author jsinger
 */
public class SessionStateAuthorizationInterceptor extends HandlerInterceptorAdapter {

	// The session state management service to use
	private final SessionStateManagementService sessionStateManagementService;

	/**
	 * Constructor for the session state management interceptor
	 *
	 * @param sessionStateManagementService The session state management service to use
	 */
	@Autowired
	public SessionStateAuthorizationInterceptor(SessionStateManagementService sessionStateManagementService) {
		this.sessionStateManagementService = sessionStateManagementService;
	}

	/**
	 * Add session state parameter to successful authorization response.
	 *
	 * Checks if the authorization request has generated a successful
	 * response containing the "code" parameter in the redirect url and
	 * adds the session state parameter accordingly.
	 *
	 * @param request The http request
	 * @param response The http response
	 * @param handler The servlet handler
	 * @param modelAndView The returned ModelAndView
	 * @throws Exception if any exception occurs
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		// Apply only on redirects
		if (sessionStateManagementService.isEnabled() && modelAndView != null && (modelAndView.getView() instanceof RedirectView)) {
			// Parse the redirect  url
			RedirectView view = (RedirectView) modelAndView.getView();
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(view.getUrl());
			MultiValueMap<String, String> parameters = uriComponentsBuilder.build().getQueryParams();
			String fragment = Strings.nullToEmpty(uriComponentsBuilder.build().getFragment());
			// Only if a code or an access token is returned
			if (parameters.containsKey("code") ||fragment.contains("access_token")) {
				// get the current session state value
				String sessionState = sessionStateManagementService.getSessionState(request);
				if (sessionState != null) {
					// build the hash
					String stateParam = sessionStateManagementService.buildSessionStateParam(sessionState, request.getParameter(OAuth2Utils.CLIENT_ID), view.getUrl());
					if (parameters.containsKey("code")) {
						// and append to the query string or ...
						uriComponentsBuilder.queryParam(SessionStateManagementService.SESSION_STATE_PARAM, stateParam);
					} else {
						// the fragment.
						uriComponentsBuilder.fragment(fragment + "&" + SessionStateManagementService.SESSION_STATE_PARAM + "=" + stateParam);
					}
					// set the new url as redirect url
					view.setUrl(uriComponentsBuilder.build().toUriString());
				}
			}
		}
		super.postHandle(request, response, handler, modelAndView);
	}


}
