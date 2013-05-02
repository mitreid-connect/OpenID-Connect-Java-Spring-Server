/**
 * 
 */
package org.mitre.openid.connect.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * This class sets a timestamp on the current HttpSession
 * when someone successfully authenticates.
 * 
 * @author jricher
 *
 */
@Component("authenticationTimeStamper")
public class AuthenticationTimeStamper extends SavedRequestAwareAuthenticationSuccessHandler {

	private static Logger logger = LoggerFactory.getLogger(AuthenticationTimeStamper.class);

	public static final String AUTH_TIMESTAMP = "AUTH_TIMESTAMP";

	/**
	 * Set the timestamp on the session to mark when the authentication happened,
	 * useful for calculating authentication age.
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		//
		// FIXME: storing the auth time in the session doesn't actually work because we need access to it from the token endpoint when the user isn't present
		//

		Date authTimestamp = new Date();

		HttpSession session = request.getSession();

		session.setAttribute(AUTH_TIMESTAMP, authTimestamp);

		logger.info("Successful Authentication at " + authTimestamp.toString());

		super.onAuthenticationSuccess(request, response, authentication);

	}

}
