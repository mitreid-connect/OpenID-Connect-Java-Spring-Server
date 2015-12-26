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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author jricher
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

    protected boolean redirectMatches(String requestedRedirect, String redirectUri) {

        // otherwise do the prefix-match from the library
        try {
            URL req = new URL(null, requestedRedirect, new NullURLStreamHandler());
            URL reg = new URL(null, redirectUri, new NullURLStreamHandler());

            if (reg.getProtocol().equals(req.getProtocol()) && hostMatches(reg.getHost(), req.getHost())) {
                return StringUtils.cleanPath(req.getPath()).startsWith(StringUtils.cleanPath(reg.getPath()));
            }
        } catch (MalformedURLException e) {
        }
        return requestedRedirect.equals(redirectUri);
    }

    /**
     * Default implementation that extends URLStreamHandler base class to prevent an error
     * when instanciating an URL object with a scheme different from http, gopher, etc.
     */
    private class NullURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return null;
        }
    }

}
