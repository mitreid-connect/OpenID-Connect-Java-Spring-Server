/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 *
 */
package org.mitre.oauth2.service.impl;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

/**
 *
 * A redirect resolver that knows how to check against the blacklisted URIs
 * for forbidden values. Can be configured to do strict string matching also.
 *
 * @author jricher
 *
 */
@Component("blacklistAwareRedirectResolver")
public class BlacklistAwareRedirectResolver extends DefaultRedirectResolver {

	@Autowired
	private BlacklistedSiteService blacklistService;

	@Autowired
	private ConfigurationPropertiesBean config;

	private boolean strictMatch = true;

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

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver#redirectMatches(java.lang.String, java.lang.String)
	 */
	@Override
	protected boolean redirectMatches(String requestedRedirect, String redirectUri) {

		if (isStrictMatch()) {
			// we're doing a strict string match for all clients
			return Strings.nullToEmpty(requestedRedirect).equals(redirectUri);
		} else {
			// otherwise do the prefix-match from the library
			return super.redirectMatches(requestedRedirect, redirectUri);
		}

	}

	/**
	 * @return the strictMatch
	 */
	public boolean isStrictMatch() {
		if (config.isHeartMode()) {
			// HEART mode enforces strict matching
			return true;
		} else {
			return strictMatch;
		}
	}

	/**
	 * Set this to true to require exact string matches for all redirect URIs. (Default is false)
	 *
	 * @param strictMatch the strictMatch to set
	 */
	public void setStrictMatch(boolean strictMatch) {
		this.strictMatch = strictMatch;
	}



}
