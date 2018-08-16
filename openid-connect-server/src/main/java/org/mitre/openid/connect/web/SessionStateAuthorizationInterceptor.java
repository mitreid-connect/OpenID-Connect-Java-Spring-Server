package org.mitre.openid.connect.web;

import org.mitre.openid.connect.session.SessionStateManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionStateAuthorizationInterceptor extends HandlerInterceptorAdapter {

	private final SessionStateManagementService sessionStateManagementService;

	@Autowired
	public SessionStateAuthorizationInterceptor(SessionStateManagementService sessionStateManagementService) {
		this.sessionStateManagementService = sessionStateManagementService;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		// Only on redirects...
		if (sessionStateManagementService.isEnabled() && modelAndView != null && (modelAndView.getView() instanceof RedirectView)) {
			// Parse the returned url
			RedirectView view = (RedirectView) modelAndView.getView();
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(view.getUrl());
			MultiValueMap<String, String> parameters = uriComponentsBuilder.build().getQueryParams();
			// Only if a code or token is returned
			if (parameters.containsKey("code") || parameters.containsKey("access_token")) {
				// get the current session state value
				String sessionState = sessionStateManagementService.getSessionState(request);
				if (sessionState != null) {
					// and append it to the query parameters
					String stateParam = sessionStateManagementService.buildSessionStateParam(sessionState, request.getParameter(OAuth2Utils.CLIENT_ID), view.getUrl());
					uriComponentsBuilder.queryParam(SessionStateManagementService.SESSION_STATE_PARAM, stateParam);
					view.setUrl(uriComponentsBuilder.build().toUriString());
				}
			}
		}
		super.postHandle(request, response, handler, modelAndView);
	}


}
