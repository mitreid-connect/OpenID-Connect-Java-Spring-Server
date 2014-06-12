/**
 * 
 */
package org.mitre.oauth2.service.impl;

import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.stereotype.Component;

/**
 * @author jricher
 *
 */
@Component("blacklistAwareRedirectResolver")
public class BlacklistAwareRedirectResolver extends DefaultRedirectResolver {

	@Autowired
	private BlacklistedSiteService blacklistService;
	
	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.endpoint.RedirectResolver#resolveRedirect(java.lang.String, org.springframework.security.oauth2.provider.ClientDetails)
	 */
	@Override
	public String resolveRedirect(String requestedRedirect, ClientDetails client) throws OAuth2Exception {
		String redirect = super.resolveRedirect(requestedRedirect, client);
		if (blacklistService.isBlacklisted(redirect)) {
			// don't let it go through
			throw new InvalidRequestException("The supplied redirect_uri is not allowed on this server.");
		} else {
			// not blacklisted, passed the parent test, we're fine
			return redirect;
		}
	}

}
