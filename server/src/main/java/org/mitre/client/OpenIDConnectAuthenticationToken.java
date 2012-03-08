package org.mitre.client;

import java.util.ArrayList;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * 
 * 
 * @author nemonik
 *
 */
public class OpenIdConnectAuthenticationToken extends
		AbstractAuthenticationToken {

	private final String userId;
	
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	/**
	 * @param userId
	 */
	public OpenIdConnectAuthenticationToken(String userId) {
		super(new ArrayList<GrantedAuthority>(0));
		this.userId = userId;
		setAuthenticated(true);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserId() {
		return userId;
	}
	
}
