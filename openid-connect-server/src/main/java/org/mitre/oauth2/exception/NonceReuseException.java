package org.mitre.oauth2.exception;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

public class NonceReuseException extends OAuth2Exception {

	private static final long serialVersionUID = 1L;

	public NonceReuseException(String clientId, String nonce) {
		super("Client " + clientId + " attempted to use reuse nonce " + nonce);
	}

}
