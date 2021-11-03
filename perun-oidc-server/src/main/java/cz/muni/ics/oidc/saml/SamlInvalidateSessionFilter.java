package cz.muni.ics.oidc.saml;

import static org.springframework.http.HttpHeaders.REFERER;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

public class SamlInvalidateSessionFilter extends GenericFilterBean {

    private static final Logger log = LoggerFactory.getLogger(SamlInvalidateSessionFilter.class);
    private final AntPathRequestMatcher matcher;

    private final String idpEntityId;
    private final String proxySpEntityId;
    private final boolean proxyEnabled;
    private final String oidcIssuer;
    private final SecurityContextLogoutHandler contextLogoutHandler;

    public SamlInvalidateSessionFilter(String pattern,
                                       String idpEntityId,
                                       String oidcIssuer,
                                       boolean proxyEnabled,
                                       String proxySpEntityId,
                                       SecurityContextLogoutHandler contextLogoutHandler)
    {
        this.matcher = new AntPathRequestMatcher(pattern);
        this.idpEntityId = idpEntityId;
        this.oidcIssuer = oidcIssuer;
        this.proxyEnabled = proxyEnabled;
        this.proxySpEntityId = proxySpEntityId;
        this.contextLogoutHandler = contextLogoutHandler;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (matcher.matches(req)) {
            String referer = req.getHeader(REFERER);
            if (!isInternalReferer(referer)) {
                log.debug("Got external referer, clear session to reauthenticate");
                contextLogoutHandler.logout(req, res, null);
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isInternalReferer(String referer) {
        if (!StringUtils.hasText(referer)) {
            // no referer, consider as internal
            return true;
        }

        boolean isInternal = referer.startsWith(oidcIssuer);
        if (!isInternal) {
            if (proxyEnabled) {
                // check if referer is PROXY (SP part)
                isInternal = referer.startsWith(proxySpEntityId);
            } else {
                // check if referer is IDP
                isInternal = referer.startsWith(idpEntityId);
            }
        }

        log.debug("Referer {} is internal: {}", referer, isInternal);
        return isInternal;
    }
}
