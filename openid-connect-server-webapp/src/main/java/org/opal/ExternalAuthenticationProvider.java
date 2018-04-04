package org.opal;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ExternalAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		ExternalAuthenticationToken auth = (ExternalAuthenticationToken)authentication;

        // TODO add logic to check token and verify
		
		ExternalAuthenticationToken a1 = new ExternalAuthenticationToken(auth.getPrincipal(),
				auth.getCredentials(), auth.getAuthorities());
		return null;
        //return a1;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		if ( authentication.isAssignableFrom( ExternalAuthenticationToken.class)){
			return true;
		}
		return false;
	}

}
