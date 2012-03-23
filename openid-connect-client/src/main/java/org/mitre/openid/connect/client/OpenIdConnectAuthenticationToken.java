package org.mitre.openid.connect.client;

import java.util.ArrayList;
import java.util.Collection;

import org.mitre.openid.connect.model.IdToken;
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

	private final Object principle;
	private final IdToken idToken;
	private final String userId;

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
	
	/**
	 * @param principle
	 * @param authorities
	 * @param userId
	 * @param idToken
	 */
	public OpenIdConnectAuthenticationToken(Object principle,
			Collection<? extends GrantedAuthority> authorities,
			String userId, IdToken idToken) {

		super(authorities);
		
		this.principle = principle;
		this.userId = userId;
		this.idToken = idToken;
		
		setAuthenticated(true);		
	}

	/**
	 * @param idToken
	 * @param userId
	 */
	public OpenIdConnectAuthenticationToken(String userId, IdToken idToken) {
		
		super(new ArrayList<GrantedAuthority>(0));

		this.principle = userId;
		this.userId = userId;
		this.idToken = idToken;
		
		setAuthenticated(false);
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return null;
	}

	public IdToken getIdToken() {
		return idToken;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return principle;
	}

	public String getUserId() {
		return userId;
	}
}
