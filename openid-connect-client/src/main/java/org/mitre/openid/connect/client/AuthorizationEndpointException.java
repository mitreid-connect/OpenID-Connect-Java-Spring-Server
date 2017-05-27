/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
 *******************************************************************************/
package org.mitre.openid.connect.client;

import org.springframework.security.authentication.AuthenticationServiceException;

public class AuthorizationEndpointException extends AuthenticationServiceException {

	private static final long serialVersionUID = 6953119789654778380L;

	private String error;

	private String errorDescription;

	private String errorURI;

	public AuthorizationEndpointException(String error, String errorDescription, String errorURI) {
		super("Error from Authorization Endpoint: " + error + " " + errorDescription + " " + errorURI);
		this.error = error;
		this.errorDescription = errorDescription;
		this.errorURI = errorURI;
	}

	public String getError() {
		return error;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public String getErrorURI() {
		return errorURI;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AuthorizationEndpointException [error=" + error + ", errorDescription=" + errorDescription + ", errorURI=" + errorURI + "]";
	}
}
