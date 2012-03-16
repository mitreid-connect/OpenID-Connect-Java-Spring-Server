package org.mitre.util;

import javax.servlet.http.HttpServletRequest;

/**
 * A collection of utility methods.
 * 
 */
public class Utility {

	/**
	 * Returns the base URL from a HttpServletRequest
	 * 
	 * @param request
	 * @return
	 */
	public static String findBaseUrl(HttpServletRequest request) {
		String issuer = String.format("%s://%s%s", request.getScheme(),
				request.getServerName(), request.getContextPath());

		if ((request.getScheme().equals("http") && request.getServerPort() != 80)
				|| (request.getScheme().equals("https") && request
						.getServerPort() != 443)) {
			// nonstandard port, need to include it
			issuer = String.format("%s://%s:%d%s", request.getScheme(),
					request.getServerName(), request.getServerPort(),
					request.getContextPath());
		}
		return issuer;
	}
}
