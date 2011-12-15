package org.mitre.openid.connect.model;

import java.util.Collection;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * Indicator that login to a site should be automatically granted 
 * without user interaction.
 * @author jricher
 *
 */
public class WhitelistedSite {

    // unique id
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    // who added this site to the whitelist (should be an admin)
	private UserInfo userInfo;
	
	// which OAuth2 client is this tied to
	private ClientDetails clientDetails;
	
	// what scopes be allowed by default
	// this should include all information for what data to access
	private Collection<String> allowedScopes;
}
