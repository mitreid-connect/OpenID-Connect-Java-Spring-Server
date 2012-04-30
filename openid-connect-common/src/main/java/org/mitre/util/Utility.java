/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
