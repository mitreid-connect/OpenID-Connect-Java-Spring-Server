/**
 * 
 */
package org.mitre.oauth2.introspectingfilter;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
public interface IntrospectionAuthorityGranter {

	public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse);
	
}
