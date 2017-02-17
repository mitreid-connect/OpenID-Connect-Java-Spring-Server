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
}
