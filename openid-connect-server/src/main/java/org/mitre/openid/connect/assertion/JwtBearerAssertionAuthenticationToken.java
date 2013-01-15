/**
 * 
 */
package org.mitre.openid.connect.assertion;

import java.util.Collection;

import org.mitre.jwt.model.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author jricher
 *
 */
public class JwtBearerAssertionAuthenticationToken extends AbstractAuthenticationToken {

	private String clientId;
	private Jwt jwt;
	
	public JwtBearerAssertionAuthenticationToken(String clientId, Jwt jwt) {
	    super(null);
	    this.clientId = clientId;
	    this.jwt = jwt;
	    setAuthenticated(false);
    }
	
	public JwtBearerAssertionAuthenticationToken(String clientId, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
	    super(authorities);
	    this.clientId = clientId;
	    this.jwt = jwt;
	    setAuthenticated(true);
    }

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return jwt;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		return clientId;
	}

	/**
     * @return the clientId
     */
    public String getClientId() {
    	return clientId;
    }

	/**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
    	this.clientId = clientId;
    }

	/**
     * @return the jwt
     */
    public Jwt getJwt() {
    	return jwt;
    }

	/**
     * @param jwt the jwt to set
     */
    public void setJwt(Jwt jwt) {
    	this.jwt = jwt;
    }

	/**
	 * Clear out the JWT that this token holds.
     */
    @Override
    public void eraseCredentials() {
	    super.eraseCredentials();
	    setJwt(null);
    }
	
	

}
