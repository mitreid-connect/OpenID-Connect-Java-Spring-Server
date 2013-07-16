/**
 * 
 */
package org.mitre.oauth2.introspectingfilter;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.google.gson.JsonObject;

/**
 * 
 * Grants the same set of authorities no matter what's passed in.
 * 
 * @author jricher
 *
 */
public class SimpleIntrospectionAuthorityGranter implements IntrospectionAuthorityGranter {

	private List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_API");
	
	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionAuthorityGranter#getAuthorities(net.minidev.json.JSONObject)
	 */
	@Override
	public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
		return authorities;
	}

	/**
	 * @return the authorities
	 */
	public List<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * @param authorities the authorities to set
	 */
	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

}
