package org.mitre.openid.connect.session;

import com.nimbusds.jose.util.Base64URL;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.filter.SessionStateManagementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component("sessionStateManagementService")
public class SessionStateManagementService {

	public static final String SESSION_STATE_PARAM = "session_state";
	/**
	 * Logger for this class
	 */
	private static final Logger sessionStateLogger = LoggerFactory.getLogger(SessionStateManagementFilter.class);

	// internal session state attribute name
	private static final String SESSION_STATE_ATTRIBUTE = "__SESSION_STATE";

	// intrenal request attribute to avoid multiple writes
	private static final String COOKIE_STATE_WRITTEN = "__STATE_ALREADY_WRITTEN";

	private final ConfigurationPropertiesBean config;

	private String cookiePath;

	@Autowired
	public SessionStateManagementService(ConfigurationPropertiesBean config) {
		this.config = config;
		try {
			URI uri = new URI(config.getIssuer());
			cookiePath = uri.getPath();
		} catch (URISyntaxException e) {
			sessionStateLogger.warn("Could not determine cookie path, issuer uri malformed", e);
			cookiePath = "/";
		}
	}

	public boolean isEnabled() {
		return config.isSessionStateEnabled();
	}

	public String getSessionState(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			Object sessionState = session.getAttribute(SESSION_STATE_ATTRIBUTE);
			return sessionState == null ? null : sessionState.toString();
		}
		return null;

	}

	public boolean isSessionStateChanged(HttpServletRequest request, HttpSession session) {
		if (!isEnabled()) return false;
		Cookie sessionStateCookie = WebUtils.getCookie(request, config.getSessionStateCookieName());
		if (sessionStateCookie != null) {
			// no current session but session state cookie, state changed!
			if (session == null) return true;
			// session state cookie found, get the stored session state value
			String sessionState = getSessionState(request);
			// if no state is stored or the value does not match the cookie value, force a change
			// Session state change detected
			return sessionState == null || !sessionStateCookie.getValue().equals(sessionState);
		} else return session != null;
	}

	public void processSessionStateCookie(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		if (isEnabled() && (request.getAttribute(COOKIE_STATE_WRITTEN) == null)) {
			// Create the session state cookie
			// Note: the cookie must be readable by JavaScript
			Cookie sessionStateCookie = new Cookie(config.getSessionStateCookieName(), "");
			sessionStateCookie.setSecure(request.isSecure());
			sessionStateCookie.setPath(cookiePath);
			if (session != null) {
				// create a new session state value
				String sessionState = UUID.randomUUID().toString();
				// store in session
				session.setAttribute(SESSION_STATE_ATTRIBUTE, sessionState);
				// set new session state value in the cookie
				sessionStateCookie.setValue(sessionState);
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

}
