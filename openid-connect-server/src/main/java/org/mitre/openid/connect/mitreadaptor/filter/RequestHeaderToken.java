package org.mitre.openid.connect.mitreadaptor.filter;

import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class RequestHeaderToken extends AbstractAuthenticationToken {

	private Object principal;
	
	
	/**
     * 
     */
    private static final long serialVersionUID = -8598928454566827917L;

    public RequestHeaderToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
    	super(authorities);
    	
    	this.principal = principal;
    	
    }
    
	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return principal;
	}

}