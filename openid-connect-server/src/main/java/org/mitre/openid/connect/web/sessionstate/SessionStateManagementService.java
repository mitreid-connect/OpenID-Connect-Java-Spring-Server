package org.mitre.openid.connect.web.sessionstate;

import com.nimbusds.jose.util.Base64URL;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.filter.SessionStateManagementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Service bean for support of OpenID Connect Session Management 1.0  as defined
 * in http://openid.net/specs/openid-connect-session-1_0.html.
 *
 * @author jsinger
 */

@Component("sessionStateManagementService")
public class SessionStateManagementService implements LogoutHandler {

	/**
	 * Logger for this class
	 */
	private static final Logger sessionStateLogger = LoggerFactory.getLogger(SessionStateManagementFilter.class);

	// Session state parameter for the authorize endpoint
	public static final String SESSION_STATE_PARAM = "session_state";

	// internal session state attribute name
	public static final String SESSION_STATE_ATTRIBUTE = "__SESSION_STATE";

	// internal request attribute to avoid multiple writes of the cookie
	private static final String COOKIE_STATE_WRITTEN = "__STATE_ALREADY_WRITTEN";

	// reference to the configuration
	private final ConfigurationPropertiesBean config;

	// Path for the cookie, extracted from the issuer url
	private String cookiePath;

	/**
	 * Create an instance of the session state management service.
	 *
	 * Parses the issuer set in the Configuration Bean to define the path for the
	 * Session State Cookie.
	 *
	 * @param config The OpenID Configuration Bean
	 */
	@Autowired
	public SessionStateManagementService(ConfigurationPropertiesBean config) {
		this.config = config;
		// extract the cookie path from the issuer
		try {
			URI uri = new URI(config.getIssuer());
			cookiePath = uri.getPath();
		} catch (URISyntaxException e) {
			sessionStateLogger.warn("Could not determine cookie path, issuer uri malformed", e);
			cookiePath = "/";
		}
	}

	/**
	 * Get the session state management enabled flag of the configuration.
	 *
	 * @return The enabled flag of the configuration
	 */
	public boolean isEnabled() {
		return config.isSessionStateEnabled();
	}

	/**
	 * Get current session state value from the session.
	 *
	 * @param request The current http request
	 * @return The session state or null if no session or the session state attribut is not found
	 */
	public String getSessionState(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			Object sessionState = session.getAttribute(SESSION_STATE_ATTRIBUTE);
			return sessionState == null ? null : sessionState.toString();
		}
		return null;

	}

	public String getCookieSessionState(HttpServletRequest request) {
		Cookie sessionStateCookie = WebUtils.getCookie(request, config.getSessionStateCookieName());
		return sessionStateCookie == null ? null : sessionStateCookie.getValue();
	}

	/**
	 * Determine if the session state is changed
	 *
	 * Compares the session state from the session and from the cookie
	 * to determine if that state is changed.
	 *
	 * Always returns false if session state management is disabled by configuration.
	 *
	 * @param request The current http request
	 * @return true if the state has changed, false if not
	 */
	public boolean isSessionStateChanged(HttpServletRequest request) {
		if (!isEnabled()) return false;
		// get the session state cookie
		String sessionStateCookie = getCookieSessionState(request);
		// get the stored session state value
		String sessionState = getSessionState(request);
		if (sessionStateCookie != null) {
			// return false if session state is equal
			return !sessionStateCookie.equals(sessionState);
		}
		// return true if cookie is not found but session state is stored in session
		return sessionState != null;
	}

	/**
	 * Write the session state cookie.
	 *
	 * Creates a new session state value and writes it in the session as well as to
	 * the session state cookie if a valid session is found and the cookie has not
	 * been written before.
	 * Deletes the cookie if no session is found.
	 *  @param request  The current http request
	 * @param response  The current http response
 * @param session The current session or null to delete the cookie 
	 */

	public void writeSessionStateCookie(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		writeSessionStateCookie(request, response, session, true);
	}

	/**
	 * Write the session state cookie.
	 *
	 * Creates a new session state value and writes it in the session as well as to
	 * the session state cookie if a valid session is found and the cookie has not
	 * been written before.
	 * Deletes the cookie if no session is found.
	 *
	 * @param request  The current http request
	 * @param response  The current http response
	 * @param session The current session or null to delete the cookie
	 */

	public void writeSessionStateCookie(HttpServletRequest request, HttpServletResponse response, HttpSession session, boolean createNew) {
		if (isEnabled() && (request.getAttribute(COOKIE_STATE_WRITTEN) == null)) {
			// Create the session state cookie
			// Note: the cookie must be readable by JavaScript
			Cookie sessionStateCookie = new Cookie(config.getSessionStateCookieName(), "");
			sessionStateCookie.setSecure(request.isSecure());
			sessionStateCookie.setPath(cookiePath);
			if (session != null) {
				Object sessionState = session.getAttribute(SESSION_STATE_ATTRIBUTE);
				if (createNew || sessionState == null) {
					// create a new session state value
					sessionState = UUID.randomUUID().toString();
					// store in session
					session.setAttribute(SESSION_STATE_ATTRIBUTE, sessionState.toString());
				}
				// set new session state value in the cookie
				sessionStateCookie.setValue(sessionState.toString());
				sessionStateLogger.debug("Set new session state {}", sessionState);
			} else {
				// make sure the session state cookie is deleted
				sessionStateCookie.setMaxAge(0);
				sessionStateLogger.debug("Delete session state cookie");

			}
			response.addCookie(sessionStateCookie);
			request.setAttribute(COOKIE_STATE_WRITTEN, true);
		}
	}

	/**
	 * Build the session state return param.
	 *
	 * Builds the session state return param as defined in
	 * http://openid.net/specs/openid-connect-session-1_0.html#OPiframe section 4.2
	 * from the current session state value, the client id and the origin url.
	 *
	 * @param sessionState The current session state value
	 * @param clientId The client id
	 * @param url The url to use for the origin
	 * @return The session state param
	 * @throws NoSuchAlgorithmException If the java version does not support SHA-256 message digest.
	 */

	public String buildSessionStateParam(String sessionState, String clientId, String url) throws NoSuchAlgorithmException {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[32];
		random.nextBytes(salt);
		String saltString = Base64URL.encode(salt).toString();
		String sessionStateString = clientId + " " + getOrigin(url) + " " + sessionState + " " + saltString;
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		String hash = Base64URL.encode(digest.digest(sessionStateString.getBytes(StandardCharsets.US_ASCII))).toString();
		return hash + "." + saltString;
	}

	/**
	 * Returns the "Origin" of a URL as defined in RFC 6454
	 *
	 * @see "https://www.rfc-editor.org/info/rfc6454"
	 * @param uri The uri string to parse
	 * @return The origin part of the uri string
	 */
	private String getOrigin(String uri) {
		int e = uri.indexOf('/', 8);
		return e != -1 ? uri.substring(0, uri.indexOf('/', 8)) : uri;
	}

	/**
	 * Logout handler implementation.
	 *
	 * Writes a new cookie on logout. Convenient method to use the class as Logout
	 * Handler when configuring programmatically.
	 *
	 * @param request The current http request
	 * @param response The current http response
	 * @param authentication the current principal details
	 */
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		writeSessionStateCookie(request, response, request.getSession(false));
	}
}
