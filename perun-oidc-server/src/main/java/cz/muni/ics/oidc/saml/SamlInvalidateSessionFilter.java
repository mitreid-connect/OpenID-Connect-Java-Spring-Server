package cz.muni.ics.oidc.saml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class SamlInvalidateSessionFilter extends GenericFilterBean {

    private static final RequestMatcher MATCHER = new OrRequestMatcher(
            new AntPathRequestMatcher("/authorize"),
            new AntPathRequestMatcher("/device")
    );

    private final SecurityContextLogoutHandler contextLogoutHandler;
    private final List<String> internalReferrers = new ArrayList<>();

    public SamlInvalidateSessionFilter(SecurityContextLogoutHandler contextLogoutHandler) {
        this.contextLogoutHandler = contextLogoutHandler;
    }

    public SamlInvalidateSessionFilter(String idpEntityId,
                                       String oidcIssuer,
                                       String proxySpEntityId,
                                       SecurityContextLogoutHandler contextLogoutHandler,
                                       String[] internalReferrers)
    {
        if (StringUtils.hasText(idpEntityId)) {
            this.internalReferrers.add(idpEntityId);
        }
        if (StringUtils.hasText(oidcIssuer)) {
            this.internalReferrers.add(oidcIssuer);
        }
        if (StringUtils.hasText(proxySpEntityId)) {
            this.internalReferrers.add(proxySpEntityId);
        }
        this.contextLogoutHandler = contextLogoutHandler;
        if (internalReferrers != null && internalReferrers.length > 0) {
            List<String> referrers = Arrays.asList(internalReferrers);
            referrers = referrers.stream().filter(StringUtils::hasText).collect(Collectors.toList());
            if (!referrers.isEmpty()) {
                this.internalReferrers.addAll(referrers);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (MATCHER.matches(req)) {
            log.debug("INV_SESS - invalidate");
            contextLogoutHandler.logout(req, res, null);
        } else {
            log.debug("INV_SESS - skipping");
        }
        chain.doFilter(req, res);
    }

    private boolean isInternalReferer(String referer) {
        if (!StringUtils.hasText(referer)) {
            return false;
        }
        for (String internal : internalReferrers) {
            if (referer.startsWith(internal)) {
                return true;
            }
        }
        return false;
    }

}
