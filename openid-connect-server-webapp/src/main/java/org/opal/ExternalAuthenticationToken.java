package org.opal;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class ExternalAuthenticationToken extends UsernamePasswordAuthenticationToken{
	public ExternalAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
		this.externalAuthentication = true;
	}


	private static final long serialVersionUID = 1L;
	private Boolean externalAuthentication;
	private String code;
	private String state;
	private String issuer;
	private String clientId;
	
	public Boolean isExternalAuthentication() {
		return this.externalAuthentication;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	

}
