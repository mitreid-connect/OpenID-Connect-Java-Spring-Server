package org.mitre.openid.connect.filter;

import org.mitre.openid.connect.web.sessionstate.SessionStateManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to check session state on each request.
 *
 * Checks if the session state has changed (e.g. the session has timed out) and
 * writes a new session state cookie of needed.
 *
 * @author jsinger
 */

@Component("sessionStateManagementFilter")
public class SessionStateManagementFilter extends OncePerRequestFilter {

	// the session state management service to be used
	private final SessionStateManagementService sessionStateManagementService;

	/**
	 * Constructor for the filter
	 *
	 * @param sessionStateManagementService the session state management service to be used
	 */
	@Autowired
	public SessionStateManagementFilter(SessionStateManagementService sessionStateManagementService) {
		this.sessionStateManagementService = sessionStateManagementService;
	}

	/**
	 * This {@code doFilterInternal} implementation checks if the session state is changed
	 * and writes a new session state cookie if needed.
	 *
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		if (sessionStateManagementService.isSessionStateChanged(request)) {
			// Session state changed, write a new session state cookie, try to restore the session value
			sessionStateManagementService.writeSessionStateCookie(request, response, request.getSession(false), false);
		}
		filterChain.doFilter(request, response);
	}

}
