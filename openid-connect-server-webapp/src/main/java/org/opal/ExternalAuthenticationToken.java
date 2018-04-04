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
	private Boolean externalAuthentication = false;
	
	
	public Boolean isExternalAuthentication() {
		return this.externalAuthentication;
	}

}
