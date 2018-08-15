package org.mitre.openid.connect.filter;

import org.mitre.openid.connect.session.SessionStateManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component("sessionStateManagementFilter")
public class SessionStateManagementFilter extends OncePerRequestFilter {

	private final SessionStateManagementService sessionStateManagementService;

	@Autowired
	public SessionStateManagementFilter(SessionStateManagementService sessionStateManagementService) {
		this.sessionStateManagementService = sessionStateManagementService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


		HttpSession session = request.getSession(false);
		if (sessionStateManagementService.isSessionStateChanged(request, session)) {
			sessionStateManagementService.processSessionStateCookie(request, response, session);
		}
		filterChain.doFilter(request, response);
	}

}
