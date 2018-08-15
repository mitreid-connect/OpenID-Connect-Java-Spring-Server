package org.mitre.openid.connect.web;

import org.mitre.openid.connect.session.SessionStateManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("sessionStateLogoutSuccessHandler")
public class SessionStateLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {


	private SessionStateManagementService sessionStateManagementService;

	@Autowired
	public SessionStateLogoutSuccessHandler(SessionStateManagementService sessionStateManagementService) {
		super();
		this.sessionStateManagementService = sessionStateManagementService;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		sessionStateManagementService.processSessionStateCookie(request, response, request.getSession(false));
		super.onLogoutSuccess(request, response, authentication);
	}
}
